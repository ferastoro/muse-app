package com.example.muse.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.muse.R;
import com.example.muse.activity.LoginActivity;
import com.example.muse.database.DatabaseHelper;
import com.example.muse.database.FavoriteDao;
import com.example.muse.databinding.FragmentUserBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private FavoriteDao favoriteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("muse_prefs", Context.MODE_PRIVATE);

        setupUserData();
        setupThemeSwitch();
        setupClickListeners();
        setupStaticStats();
    }

    private void setupUserData() {
        String name = sharedPreferences.getString("userName", "Museum Guest");
        binding.tvName.setText(name);
        
        // Dynamic email based on name
        String email = name.toLowerCase().replace(" ", ".") + "@muse.com";
        binding.tvEmail.setText(email);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFavoriteStat();
    }

    private void setupThemeSwitch() {
        String themeMode = sharedPreferences.getString("theme_mode", "system");
        binding.switchTheme.setChecked(themeMode.equals("dark"));

        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isChecked) {
                editor.putString("theme_mode", "dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                editor.putString("theme_mode", "light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            editor.apply();
        });
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> showSoonSnackbar(v));
        binding.itemNotifications.setOnClickListener(v -> showSoonSnackbar(v));

        binding.itemAbout.setOnClickListener(v -> showAboutDialog());
        binding.itemLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupStaticStats() {
        binding.tvStatKoleksi.setText("124");
        binding.tvStatKunjungan.setText("42");
    }

    private void updateFavoriteStat() {
        if (dbHelper == null) {
            dbHelper = DatabaseHelper.getInstance(requireContext());
            favoriteDao = new FavoriteDao(dbHelper);
        }

        executorService.execute(() -> {
            int count = favoriteDao.getFavoritesCount();
            mainHandler.post(() -> {
                if (binding != null) {
                    binding.tvStatFavorit.setText(String.valueOf(count));
                }
            });
        });
    }

    private void showSoonSnackbar(View view) {
        Snackbar.make(view, R.string.feature_soon, Snackbar.LENGTH_SHORT).show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.setting_logout, (dialog, which) -> {
                    // Clear session but keep theme settings
                    sharedPreferences.edit()
                            .putBoolean("isLoggedIn", false)
                            .remove("userName")
                            .apply();

                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // No need to finish fragment's activity manually here, CLEAR_TASK handles it
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
