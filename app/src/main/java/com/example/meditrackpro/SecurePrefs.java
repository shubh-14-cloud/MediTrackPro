package com.example.meditrackpro;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.GeneralSecurityException;
import java.io.IOException;

/** Every screen in this app opens an AES-256 EncryptedSharedPreferences file the same way;
 *  this just factors out that repeated construction. */
final class SecurePrefs {

    private SecurePrefs() {
    }

    static SharedPreferences get(Context context, String name) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    name,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to open encrypted prefs: " + name, e);
        }
    }
}
