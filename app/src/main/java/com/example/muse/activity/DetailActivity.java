package com.example.muse.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.muse.R;
import com.example.muse.adapter.RelatedArtworkAdapter;
import com.example.muse.database.DatabaseHelper;
import com.example.muse.database.FavoriteDao;
import com.example.muse.databinding.ActivityDetailBinding;
import com.example.muse.model.Favorite;
import com.example.muse.model.MetArtwork;
import com.example.muse.model.MetObjectsResponse;
import com.example.muse.network.RetrofitClient;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private RelatedArtworkAdapter relatedAdapter;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    private DatabaseHelper dbHelper;
    private FavoriteDao favoriteDao;
    
    private int artworkId;
    private MetArtwork currentArtwork;
    private String currentImageUrl;
    private boolean isDescriptionExpanded = false;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        dbHelper = DatabaseHelper.getInstance(this);
        favoriteDao = new FavoriteDao(dbHelper);

        Intent intent = getIntent();
        artworkId = intent.getIntExtra("artwork_id", -1);
        String initialTitle = intent.getStringExtra("title");
        currentImageUrl = intent.getStringExtra("image_url");

        setupUIInitial(initialTitle, currentImageUrl);
        setupRecyclerView();
        loadArtworkDetail();
        checkIsFavorited();

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnShare.setOnClickListener(v -> shareArtwork());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.tvReadMore.setOnClickListener(v -> toggleDescription());
        
        // Fix 7: Fullscreen zoom
        binding.ivArtworkDetail.setOnClickListener(v -> {
            if (currentImageUrl != null) showFullscreenImage(currentImageUrl);
        });
    }

    private void setupUIInitial(String title, String imageUrl) {
        binding.tvTitleDetail.setText(title);
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.ivArtworkDetail);
        }
    }

    private void checkIsFavorited() {
        executorService.execute(() -> {
            isFavorited = favoriteDao.isFavorite(artworkId);
            mainHandler.post(this::updateFavoriteButton);
        });
    }

    private void setupRecyclerView() {
        relatedAdapter = new RelatedArtworkAdapter();
        // Fix 8: Scroll fix
        binding.rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvRelated.setAdapter(relatedAdapter);
        binding.rvRelated.setNestedScrollingEnabled(false);
        binding.rvRelated.setHasFixedSize(false);

        relatedAdapter.setOnItemClickListener(artwork -> {
            Intent intent = new Intent(DetailActivity.this, DetailActivity.class);
            intent.putExtra("artwork_id", artwork.getObjectID());
            intent.putExtra("title", artwork.getTitle());
            intent.putExtra("image_url", artwork.getDisplayImage());
            startActivity(intent);
        });
    }

    private void loadArtworkDetail() {
        showLoading(true);
        executorService.execute(() -> {
            try {
                Response<MetArtwork> response = RetrofitClient.getApiService().getObjectDetail(artworkId).execute();

                mainHandler.post(() -> {
                    if (isFinishing() || binding == null) return;
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        currentArtwork = response.body();
                        currentImageUrl = currentArtwork.getDisplayImage();
                        bindArtworkData(currentArtwork);
                        loadRelatedArtworks(currentArtwork.getClassification());
                    } else {
                        Toast.makeText(DetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isFinishing() || binding == null) return;
                    showLoading(false);
                    Toast.makeText(DetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void bindArtworkData(MetArtwork artwork) {
        binding.tvTitleDetail.setText(artwork.getTitle());
        binding.tvArtistDetail.setText(artwork.getArtistDisplay());
        binding.tvYearBadge.setText(artwork.getObjectDate());
        binding.tvMediumBadge.setText(artwork.getMedium());
        binding.tvDescriptionDetail.setText(artwork.getDescription());

        binding.tvLocation.setText(artwork.getCulture() != null && !artwork.getCulture().isEmpty() ? artwork.getCulture() : artwork.getDepartment());
        binding.tvAccession.setText(String.valueOf(artwork.getObjectID()));
        binding.tvRestoration.setText(artwork.getDimensions() != null ? artwork.getDimensions() : "N/A");

        Glide.with(this)
                .load(artwork.getDisplayImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivArtworkDetail);
    }

    private void loadRelatedArtworks(String classification) {
        if (classification == null || classification.isEmpty()) return;

        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> resp = RetrofitClient.getApiService()
                        .searchObjects(classification, true, true, null)
                        .execute();

                if (resp.isSuccessful() && resp.body() != null && resp.body().getObjectIDs() != null) {
                    List<Integer> ids = resp.body().getObjectIDs();
                    List<Integer> limitIds = ids.subList(0, Math.min(6, ids.size()));

                    List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                    CountDownLatch latch = new CountDownLatch(limitIds.size());

                    for (int id : limitIds) {
                        executorService.execute(() -> {
                            try {
                                Response<MetArtwork> detail = RetrofitClient.getApiService().getObjectDetail(id).execute();
                                if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                    results.add(detail.body());
                                }
                            } catch (Exception e) {
                                Log.e("MUSE_DETAIL", "Related error: " + e.getMessage());
                            } finally {
                                latch.countDown();
                            }
                        });
                    }

                    latch.await(10, TimeUnit.SECONDS);
                    mainHandler.post(() -> {
                        if (isFinishing() || binding == null) return;
                        relatedAdapter.setData(new ArrayList<>(results));
                    });
                }
            } catch (Exception e) {
                Log.e("MUSE_DETAIL", "Related load failed: " + e.getMessage());
            }
        });
    }

    private void updateFavoriteButton() {
        if (isFavorited) {
            binding.btnFavorite.setText(R.string.saved);
            binding.btnFavorite.setIconResource(R.drawable.ic_heart_filled);
        } else {
            binding.btnFavorite.setText(R.string.save_to_favorite);
            binding.btnFavorite.setIconResource(R.drawable.ic_heart_outline);
        }
    }

    private void toggleFavorite() {
        if (currentArtwork == null) return;

        executorService.execute(() -> {
            if (isFavorited) {
                favoriteDao.deleteFavorite(artworkId);
                isFavorited = false;
            } else {
                Favorite fav = new Favorite(
                        currentArtwork.getObjectID(),
                        currentArtwork.getTitle(),
                        currentArtwork.getArtistDisplayName(),
                        currentArtwork.getObjectDate(),
                        currentArtwork.getMedium(),
                        currentArtwork.getDisplayImage(),
                        currentArtwork.getDescription(),
                        System.currentTimeMillis()
                );
                favoriteDao.insertFavorite(fav);
                isFavorited = true;
            }
            mainHandler.post(() -> {
                if (isFinishing() || binding == null) return;
                updateFavoriteButton();
                Toast.makeText(DetailActivity.this, isFavorited ? "Ditambahkan ke Favorit" : "Dihapus dari Favorit", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void toggleDescription() {
        isDescriptionExpanded = !isDescriptionExpanded;
        binding.tvDescriptionDetail.setMaxLines(isDescriptionExpanded ? Integer.MAX_VALUE : 4);
        binding.tvReadMore.setText(isDescriptionExpanded ? R.string.read_less : R.string.read_more);
    }

    private void shareArtwork() {
        if (currentArtwork == null) return;
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Lihat karya: " + currentArtwork.getTitle() + "\n" + currentArtwork.getDisplayImage());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void showFullscreenImage(String imageUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        PhotoView photoView = new PhotoView(this);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(photoView);
        photoView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(photoView);
        dialog.show();
    }

    private void showLoading(boolean isLoading) {
        binding.progressBarDetail.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdownNow();
        if (mainHandler != null) mainHandler.removeCallbacksAndMessages(null);
    }
}
