package com.example.meditrackpro;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final long CLOCK_TICK_MILLIS = 60_000L;

    private TextView tvDateTime;
    private TextView tvAdherence;
    private TextView tvDoseProgress;
    private ProgressBar progressBar;
    private CardView alertCard;
    private CardView cardEmptyDoses;
    private LinearLayout layoutDoses;
    private TextView tvAlertMedName;
    private MedicineViewModel viewModel;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, CLOCK_TICK_MILLIS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDateTime = view.findViewById(R.id.tvDateTime);
        tvDateTime.setTextColor(Color.WHITE);

        tvAdherence = view.findViewById(R.id.tvAdherence);
        tvAdherence.setTextColor(Color.WHITE);

        tvDoseProgress = view.findViewById(R.id.tvDoseProgress);
        tvDoseProgress.setTextColor(Color.WHITE);

        progressBar = view.findViewById(R.id.progressDoses);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.WHITE));

        alertCard = view.findViewById(R.id.alertCard);
        cardEmptyDoses = view.findViewById(R.id.cardEmptyDoses);
        layoutDoses = view.findViewById(R.id.layoutDosesContainer);
        tvAlertMedName = view.findViewById(R.id.tvAlertMedName);

        viewModel = ((MainActivity) requireActivity()).getSharedMedicineViewModel();

        setupGreeting(view);
        setupClock();
        setupDoseObserver();
        setupAdherenceCard(view);
    }

    private void setupGreeting(View view) {
        ((TextView) view.findViewById(R.id.tvGreeting)).setText(getGreeting());
        String displayName = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                : null;
        if (displayName != null && !displayName.isEmpty()) {
            ((TextView) view.findViewById(R.id.tvUserName)).setText(displayName);
        }
    }

    private void setupClock() {
        updateClock();
        handler.postDelayed(clockTick, CLOCK_TICK_MILLIS);
    }

    private void setupDoseObserver() {
        // Observe medicines + dose logs together
        viewModel.getAllMedicines().observe(getViewLifecycleOwner(), medicines -> {
            if (medicines == null || medicines.isEmpty()) {
                alertCard.setVisibility(View.GONE);
                cardEmptyDoses.setVisibility(View.VISIBLE);
                layoutDoses.setVisibility(View.GONE);
                tvDoseProgress.setText("0/0 doses");
                tvAdherence.setText("0%");
                progressBar.setProgress(0);
                return;
            }

            alertCard.setVisibility(View.VISIBLE);
            cardEmptyDoses.setVisibility(View.GONE);
            layoutDoses.setVisibility(View.VISIBLE);

            Medicine first = medicines.get(0);
            String firstSlot = first.getTimes().split(",")[0].trim();
            tvAlertMedName.setText(first.getName() + " " + first.getDose() + " · " + firstSlot);

            viewModel.ensureDoseLogsForToday(medicines);
        });

        // Observe today's dose logs for live progress
        viewModel.todayDoseLogs().observe(getViewLifecycleOwner(), logs -> {
            List<Medicine> medicines = viewModel.getAllMedicines().getValue();
            if (medicines == null || medicines.isEmpty()) return;

            int total = logs.size();
            int taken = 0;
            for (DoseLog log : logs) {
                if (log.isTaken()) taken++;
            }
            int pct = total > 0 ? (taken * 100) / total : 0;

            tvDoseProgress.setText(taken + "/" + total + " doses");
            tvAdherence.setText(pct + "%");
            progressBar.setProgress(pct);

            // Rebuild dose cards
            layoutDoses.removeAllViews();

            Map<String, DoseLog> logMap = new HashMap<>();
            for (DoseLog log : logs) {
                logMap.put(log.getMedicineId() + "|" + log.getTimeSlot(), log);
            }

            for (Medicine med : medicines) {
                for (String rawSlot : med.getTimes().split(",")) {
                    String slot = rawSlot.trim();
                    DoseLog log = logMap.get(med.getId() + "|" + slot);
                    boolean isTaken = log != null && log.isTaken();

                    View card = getLayoutInflater().inflate(R.layout.item_dose_card, layoutDoses, false);
                    ((TextView) card.findViewById(R.id.tvDoseMedName)).setText(med.getName() + " " + med.getDose());
                    ((TextView) card.findViewById(R.id.tvDoseTime)).setText(slot);

                    TextView markBtn = card.findViewById(R.id.tvMarkTaken);
                    if (isTaken) {
                        markBtn.setText("Taken");
                        markBtn.setAlpha(0.5f);
                    } else {
                        markBtn.setText("Take");
                        markBtn.setAlpha(1.0f);
                    }
                    markBtn.setOnClickListener(v -> viewModel.markDoseTaken(med, slot));

                    layoutDoses.addView(card);
                }
            }
        });
    }

    private void setupAdherenceCard(View view) {
        view.findViewById(R.id.btnRemind).setOnClickListener(v -> {
            List<Medicine> medicines = viewModel.getAllMedicines().getValue();
            if (medicines == null || medicines.isEmpty()) return;
            Medicine first = medicines.get(0);

            String[] options = {"In 10 minutes", "In 30 minutes", "In 1 hour"};
            int[] minutes = {10, 30, 60};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Remind me about " + first.getName())
                    .setItems(options, (dialog, which) -> {
                        viewModel.scheduleSnooze(first.getName(), first.getDose(), minutes[which]);
                        Toast.makeText(
                                requireContext(),
                                "Reminder set for " + options[which].toLowerCase(Locale.getDefault()),
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault());
        tvDateTime.setText(now.format(timeFmt) + " · " + now.format(dayFmt));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(clockTick);
    }
}
