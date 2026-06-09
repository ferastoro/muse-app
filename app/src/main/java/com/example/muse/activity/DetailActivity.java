package com.example.muse.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.muse.BuildConfig;
import com.example.muse.R;
import com.example.muse.database.DatabaseHelper;
import com.example.muse.database.FavoriteDao;
import com.example.muse.databinding.ActivityDetailBinding;
import com.example.muse.model.Favorite;
import com.example.muse.model.HarvardArtwork;
import com.example.muse.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private FavoriteDao favoriteDao;
    private HarvardArtwork currentArtwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        favoriteDao = new FavoriteDao(DatabaseHelper.getInstance(this));

        int id = getIntent().getIntExtra("artwork_id", -1);
        
        if (id != -1) {
            loadArtworkDetail(id);
            updateFavoriteButtonState(id);
        } else {
            Toast.makeText(this, "ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void loadArtworkDetail(int id) {
        binding.progressBarDetail.setVisibility(View.VISIBLE);
        RetrofitClient.getClient().getArtworkDetail(id, BuildConfig.HARVARD_API_KEY)
                .enqueue(new Callback<HarvardArtwork>() {
                    @Override
                    public void onResponse(Call<HarvardArtwork> call, Response<HarvardArtwork> response) {
                        binding.progressBarDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            currentArtwork = response.body();
                            updateUI(currentArtwork);
                        } else {
                            Toast.makeText(DetailActivity.this, "Gagal memuat detail", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<HarvardArtwork> call, Throwable t) {
                        binding.progressBarDetail.setVisibility(View.GONE);
                        Toast.makeText(DetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(HarvardArtwork artwork) {
        binding.tvTitleDetail.setText(artwork.getTitle());
        binding.tvArtistDetail.setText(artwork.getArtistDisplay());
        binding.tvYearBadge.setText(artwork.getDated());
        binding.tvMediumBadge.setText(artwork.getMedium());
        binding.tvDescriptionDetail.setText(artwork.getDisplayDescription());
        
        String culture = artwork.getCulture() != null ? artwork.getCulture() : "Unknown Culture";
        String dept = artwork.getDepartment() != null ? artwork.getDepartment() : "";
        binding.tvLocation.setText(dept.isEmpty() ? culture : culture + " · " + dept);

        binding.tvAccession.setText(String.valueOf(artwork.getId()));
        binding.tvRestoration.setText(artwork.getDisplayPeriod());

        Glide.with(this)
                .load(artwork.getDisplayImage())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivArtworkDetail);
    }

    private void updateFavoriteButtonState(int id) {
        if (favoriteDao.isFavorite(id)) {
            binding.btnFavorite.setText("Hapus dari Favorit");
            binding.btnFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.dark_card)));
            binding.btnFavorite.setTextColor(getResources().getColor(R.color.white));
        } else {
            binding.btnFavorite.setText("Simpan ke Favorit");
            binding.btnFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.muse_gold)));
            binding.btnFavorite.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void toggleFavorite() {
        if (currentArtwork == null) return;

        int id = currentArtwork.getId();
        if (favoriteDao.isFavorite(id)) {
            favoriteDao.deleteFavorite(id);
            Toast.makeText(this, "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
        } else {
            Favorite favorite = new Favorite(
                    currentArtwork.getId(),
                    currentArtwork.getTitle(),
                    currentArtwork.getArtistName(),
                    currentArtwork.getDated(),
                    currentArtwork.getMedium(),
                    currentArtwork.getDisplayImage(),
                    currentArtwork.getDisplayDescription(),
                    System.currentTimeMillis()
            );
            favoriteDao.insertFavorite(favorite);
            Toast.makeText(this, "Disimpan ke favorit", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButtonState(id);
    }
}
