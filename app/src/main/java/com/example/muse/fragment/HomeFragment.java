package com.example.muse.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muse.R;
import com.example.muse.activity.DetailActivity;
import com.example.muse.adapter.FeaturedArtworkAdapter;
import com.example.muse.adapter.RecentArtworkAdapter;
import com.example.muse.databinding.FragmentHomeBinding;
import com.example.muse.model.MetArtwork;
import com.example.muse.model.MetObjectsResponse;
import com.example.muse.network.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FeaturedArtworkAdapter featuredAdapter;
    private RecentArtworkAdapter recentAdapter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupListeners();
        loadAllData(null, null);

        binding.btnRefresh.setOnClickListener(v -> loadAllData(null, null));
    }

    private void setupListeners() {
        binding.tvSeeAll.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_search));

        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                loadRecentData("art", null);
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                String text = chip.getText().toString();
                
                if (text.equals(getString(R.string.category_all))) {
                    loadRecentData("art", null);
                } else if (text.equals(getString(R.string.category_painting))) {
                    loadRecentData("painting", 11);
                } else if (text.equals(getString(R.string.category_sculpture))) {
                    loadRecentData("sculpture", 13);
                } else if (text.equals(getString(R.string.category_photography))) {
                    loadRecentData("photography", 19);
                } else if (text.equals(getString(R.string.category_ceramic))) {
                    loadRecentData("ceramics", 6);
                }
            }
        });

        binding.btnSearch.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_search));
        
        binding.btnMenu.setOnClickListener(v -> 
            Snackbar.make(v, R.string.feature_soon, Snackbar.LENGTH_SHORT).show());
    }

    private void setupRecyclerViews() {
        featuredAdapter = new FeaturedArtworkAdapter();
        binding.rvFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeatured.setAdapter(featuredAdapter);

        recentAdapter = new RecentArtworkAdapter();
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecent.setAdapter(recentAdapter);

        featuredAdapter.setOnItemClickListener(this::navigateToDetail);
        recentAdapter.setOnItemClickListener(this::navigateToDetail);
    }

    private void navigateToDetail(MetArtwork artwork) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("artwork_id", artwork.getObjectID());
        intent.putExtra("title", artwork.getTitle());
        startActivity(intent);
    }

    private void loadAllData(@Nullable String query, @Nullable Integer deptId) {
        showLoading(true);
        loadFeaturedData();
        loadRecentData(query != null ? query : "art", deptId);
    }

    private void loadFeaturedData() {
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> resp = RetrofitClient.getApiService()
                        .getHighlightObjects(true, true, true, "painting")
                        .execute();

                if (!resp.isSuccessful() || resp.body() == null || resp.body().getObjectIDs() == null) {
                    mainHandler.post(this::showError);
                    return;
                }

                List<Integer> ids = resp.body().getObjectIDs();
                List<Integer> first10 = ids.subList(0, Math.min(10, ids.size()));

                List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                CountDownLatch latch = new CountDownLatch(first10.size());

                for (int id : first10) {
                    executorService.execute(() -> {
                        try {
                            Response<MetArtwork> detail = RetrofitClient.getApiService()
                                    .getObjectDetail(id)
                                    .execute();
                            if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                results.add(detail.body());
                            }
                        } catch (Exception e) {
                            Log.e("MUSE_M2", "Featured detail error: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await(15, TimeUnit.SECONDS);

                mainHandler.post(() -> {
                    if (results.isEmpty()) {
                        showError();
                    } else {
                        featuredAdapter.setData(new ArrayList<>(results));
                        showContent();
                    }
                });

            } catch (Exception e) {
                Log.e("MUSE_M2", "Featured error: " + e.getMessage());
                mainHandler.post(this::showError);
            }
        });
    }

    private void loadRecentData(String query, @Nullable Integer deptId) {
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> resp = RetrofitClient.getApiService()
                        .searchObjects(query, true, true, deptId)
                        .execute();

                if (!resp.isSuccessful() || resp.body() == null || resp.body().getObjectIDs() == null) {
                    mainHandler.post(this::showError);
                    return;
                }

                List<Integer> ids = resp.body().getObjectIDs();
                List<Integer> first10 = ids.subList(0, Math.min(10, ids.size()));

                List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                CountDownLatch latch = new CountDownLatch(first10.size());

                for (int id : first10) {
                    executorService.execute(() -> {
                        try {
                            Response<MetArtwork> detail = RetrofitClient.getApiService()
                                    .getObjectDetail(id)
                                    .execute();
                            if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                results.add(detail.body());
                            }
                        } catch (Exception e) {
                            Log.e("MUSE_M2", "Recent detail error: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await(15, TimeUnit.SECONDS);

                mainHandler.post(() -> {
                    if (results.isEmpty()) {
                        showError();
                    } else {
                        recentAdapter.setData(new ArrayList<>(results));
                        showContent();
                    }
                });

            } catch (Exception e) {
                Log.e("MUSE_M2", "Recent error: " + e.getMessage());
                mainHandler.post(this::showError);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.rvFeatured.setVisibility(View.GONE);
            binding.rvRecent.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvFeatured.setVisibility(View.VISIBLE);
        binding.rvRecent.setVisibility(View.VISIBLE);
        binding.btnRefresh.setVisibility(View.GONE);
    }

    private void showError() {
        showLoading(false);
        binding.btnRefresh.setVisibility(View.VISIBLE);
        if (getContext() != null) {
            Snackbar.make(binding.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
