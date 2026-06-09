package com.example.muse.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.muse.R;
import com.example.muse.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private static final int REQUEST_IMAGE = 1001;
    private String currentAvatarUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadExistingData();

        binding.ivAvatar.setOnClickListener(v -> {
            Intent picker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(picker, REQUEST_IMAGE);
        });

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences("muse_prefs", MODE_PRIVATE);
        // Fix: Use consistent defaults and keys
        String name = prefs.getString("user_name", "Pengguna MUSE");
        String email = prefs.getString("user_email", "muse@example.com");
        currentAvatarUri = prefs.getString("user_avatar", null);

        binding.etName.setText(name);
        binding.etEmail.setText(email);

        if (currentAvatarUri != null) {
            Glide.with(this)
                    .load(Uri.parse(currentAvatarUri))
                    .circleCrop()
                    .into(binding.ivAvatar);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                currentAvatarUri = selectedImage.toString();
                Glide.with(this)
                        .load(selectedImage)
                        .circleCrop()
                        .into(binding.ivAvatar);
            }
        }
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Nama tidak boleh kosong");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("muse_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("user_avatar", currentAvatarUri)
                .apply();

        Toast.makeText(this, "Profil disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
}
