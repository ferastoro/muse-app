package com.example.muse.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.muse.databinding.ActivityMainBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        applyTheme();
        
        super.onCreate(savedInstanceState);

        // FIX 4: Bersihkan data lama Shared Preferences dan Glide cache
        clearLegacyData();

        // Session Check
        SharedPreferences prefs = getSharedPreferences("muse_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adjustBackgroundAlpha();

        binding.btnMulai.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("muse_prefs", MODE_PRIVATE);
        String themeMode = prefs.getString("theme_mode", "system");

        switch (themeMode) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void adjustBackgroundAlpha() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.imgBg.setAlpha(0.10f);
        } else {
            // Menaikkan opacity background di Light Mode agar tidak terlalu terang/washout
            binding.imgBg.setAlpha(0.35f);
        }
    }

    private void clearLegacyData() {
        SharedPreferences prefs = getSharedPreferences("muse_prefs", MODE_PRIVATE);
        
        // Cek apakah sudah pernah di-clear setelah migrasi Harvard
        boolean migrated = prefs.getBoolean("harvard_migrated", false);

        if (!migrated) {
            // FIX 4: Clear Glide cache di background
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Glide.get(this).clearDiskCache();
            });

            prefs.edit()
                // Clear stat lama
                .remove("stat_viewed_ids")
                .remove("stat_visit_count")
                // Tandai sudah migrasi
                .putBoolean("harvard_migrated", true)
                .apply();
        }
    }
}
