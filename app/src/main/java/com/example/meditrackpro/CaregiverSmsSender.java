package com.example.meditrackpro;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.regex.Pattern;

public final class CaregiverSmsSender {

    private static final String SMS_SENT_ACTION = "com.example.meditrackpro.SMS_SENT";
    private static final Pattern NON_PHONE_CHARS = Pattern.compile("[^+\\d]");

    private CaregiverSmsSender() {
    }

    public static void send(Context context, String message) {
        send(context, message, false);
    }

    /**
     * Sends an SMS to the saved caregiver phone number.
     * showFeedback — if true, registers a one-shot broadcast receiver so a Toast
     * appears in the sending app process to confirm success or report failure.
     */
    public static void send(Context context, String message, boolean showFeedback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Timber.w("CaregiverSmsSender: SEND_SMS permission not granted");
            if (showFeedback) toast(context, "SMS permission not granted. Please enable it in Settings.");
            return;
        }

        SharedPreferences prefs;
        try {
            prefs = SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS);
        } catch (Exception e) {
            Timber.e(e, "CaregiverSmsSender: failed to open prefs");
            return;
        }

        if (!prefs.getBoolean("active", false)) {
            if (showFeedback) toast(context, "No caregiver saved yet.");
            return;
        }

        String rawPhone = prefs.getString("phone", "");
        String phone = normalisePhone(rawPhone != null ? rawPhone : "");
        if (phone.trim().isEmpty()) {
            if (showFeedback) toast(context, "Caregiver phone number is missing.");
            return;
        }

        try {
            SmsManager sms = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? context.getSystemService(SmsManager.class)
                    : SmsManager.getDefault();

            PendingIntent sentIntent = null;
            if (showFeedback) {
                // Register a one-shot receiver to report success/failure back as a Toast
                String sentAction = SMS_SENT_ACTION + "_" + System.currentTimeMillis();
                String finalPhone = phone;
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context ctx, Intent i) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                toast(ctx, "SMS sent successfully to " + finalPhone);
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                toast(ctx, "SMS failed: no cellular service");
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                toast(ctx, "SMS failed: null PDU");
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                toast(ctx, "SMS failed: radio is off (Airplane mode?)");
                                break;
                            default:
                                toast(ctx, "SMS failed (error code " + getResultCode() + "). Check the number format.");
                        }
                        try {
                            ctx.unregisterReceiver(this);
                        } catch (Exception ignored) {
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, new IntentFilter(sentAction), Context.RECEIVER_NOT_EXPORTED);
                } else {
                    context.registerReceiver(receiver, new IntentFilter(sentAction));
                }
                sentIntent = PendingIntent.getBroadcast(
                        context, 0, new Intent(sentAction),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }

            ArrayList<String> parts = sms.divideMessage(message);
            ArrayList<PendingIntent> sentIntents = null;
            if (sentIntent != null) {
                sentIntents = new ArrayList<>();
                for (int i = 0; i < parts.size(); i++) sentIntents.add(sentIntent);
            }
            sms.sendMultipartTextMessage(phone, null, parts, sentIntents, null);
            Timber.d("CaregiverSmsSender: SMS sent to " + phone);
        } catch (Exception e) {
            Timber.e(e, "CaregiverSmsSender: failed to send SMS");
            if (showFeedback) toast(context, "SMS send error: " + e.getMessage());
        }
    }

    /**
     * Ensures the phone number has a country code.
     * If the number starts with 0, strips the 0 and prepends +91 (India default).
     * If it's 10 digits with no prefix, prepends +91.
     * Numbers already starting with + are left unchanged.
     */
    public static String normalisePhone(String raw) {
        String digits = NON_PHONE_CHARS.matcher(raw).replaceAll("");
        if (digits.startsWith("+")) return digits;
        if (digits.startsWith("0")) return "+91" + digits.substring(1);
        if (digits.length() == 10) return "+91" + digits;
        return digits;
    }

    private static void toast(Context context, String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show());
    }
}
