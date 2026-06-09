package com.example.muse.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.example.muse.R;
import com.example.muse.activity.EditProfileActivity;
import com.example.muse.activity.LoginActivity;
import com.example.muse.database.DatabaseHelper;
import com.example.muse.database.FavoriteDao;
import com.example.muse.databinding.FragmentUserBinding;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private FavoriteDao favoriteDao;
    
    private ExecutorService executor;
    private Handler handler;
    private boolean isFragmentActive = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentActive = true;
        executor = Executors.newFixedThreadPool(3);
        handler = new Handler(Looper.getMainLooper());
        
        sharedPreferences = requireActivity().getSharedPreferences("muse_prefs", Context.MODE_PRIVATE);

        setupThemeSwitch();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        String name = sharedPreferences.getString("user_name", "Pengguna MUSE");
        String email = sharedPreferences.getString("user_email", "muse@example.com");
        String avatarUri = sharedPreferences.getString("user_avatar", null);

        binding.tvName.setText(name);
        binding.tvEmail.setText(email);

        if (avatarUri != null) {
            Glide.with(this)
                    .load(Uri.parse(avatarUri))
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }

    private void loadStats() {
        // 1. Ambil data asli dari SharedPreferences
        Set<String> viewedIds = sharedPreferences.getStringSet("stat_viewed_ids", new HashSet<>());
        int actualKoleksi = viewedIds.size();
        int actualKarya = sharedPreferences.getInt("stat_visit_count", 0);

        // Fallback logic: Jika data asli 0, tampilkan 120 Koleksi dan 542 Karya
        final int displayKoleksi = (actualKoleksi == 0) ? 120 : actualKoleksi;
        final int displayKarya = (actualKarya == 0) ? 542 : actualKarya;

        // 2. Ambil jumlah Favorit dari Database (Background Thread)
        if (dbHelper == null) {
            dbHelper = DatabaseHelper.getInstance(requireContext());
            favoriteDao = new FavoriteDao(dbHelper);
        }

        executor.execute(() -> {
            int favCount = favoriteDao.getFavoritesCount();
            handler.post(() -> {
                if (!isFragmentActive || binding == null) return;
                
                binding.tvStatKoleksi.setText(String.valueOf(displayKoleksi));
                binding.tvStatFavorit.setText(String.valueOf(favCount));
                binding.tvStatKunjungan.setText(String.valueOf(displayKarya));
            });
        });
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
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        binding.itemLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.setting_logout, (dialog, which) -> {
                    sharedPreferences.edit()
                            .putBoolean("isLoggedIn", false)
                            .remove("user_name")
                            .remove("user_email")
                            .remove("user_avatar")
                            .apply();

                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
        if (executor != null) executor.shutdownNow();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
