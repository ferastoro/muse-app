package com.example.muse.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

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
import com.example.muse.model.FilterOptions;
import com.example.muse.model.MetArtwork;
import com.example.muse.model.MetObjectsResponse;
import com.example.muse.network.RetrofitClient;

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
    
    private FilterOptions filterOptions = new FilterOptions();

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
        loadAllData();
    }

    private void setupListeners() {
        binding.tvSeeAll.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_search));

        binding.btnSearch.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_search));
        
        binding.btnMenu.setOnClickListener(v -> {
            // Sidebar trigger will be handled in HomeActivity
        });

        binding.btnRefreshToolbar.setOnClickListener(v -> {
            startRefreshAnimation();
            loadAllData();
        });

        binding.btnRetryNetwork.setOnClickListener(v -> loadAllData());
        binding.btnResetFilter.setOnClickListener(v -> {
            filterOptions.reset();
            loadAllData();
        });
    }

    /**
     * Fix for compilation error: Handlers filter changes from Sidebar
     */
    public void onFilterChanged(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        if (isAdded()) {
            loadAllData();
        }
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

    private void loadAllData() {
        if (!isNetworkAvailable()) {
            showNetworkError();
            return;
        }

        showLoading(true);
        loadFeaturedData();
        loadRecentData();
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void loadFeaturedData() {
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> resp = RetrofitClient.getApiService()
                        .getHighlightObjects(true, true, true, "painting")
                        .execute();

                if (!resp.isSuccessful() || resp.body() == null || resp.body().getObjectIDs() == null) {
                    mainHandler.post(this::showEmptyState);
                    return;
                }

                List<Integer> allIds = resp.body().getObjectIDs();
                if (allIds.isEmpty()) {
                    mainHandler.post(this::showEmptyState);
                    return;
                }

                Collections.shuffle(allIds);
                List<Integer> selectedIds = allIds.subList(0, Math.min(10, allIds.size()));

                List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                CountDownLatch latch = new CountDownLatch(selectedIds.size());

                for (int id : selectedIds) {
                    executorService.execute(() -> {
                        try {
                            Response<MetArtwork> detail = RetrofitClient.getApiService()
                                    .getObjectDetail(id)
                                    .execute();
                            if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                results.add(detail.body());
                            }
                        } catch (Exception e) {
                            Log.e("MUSE_HOME", "Detail error: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await(15, TimeUnit.SECONDS);

                mainHandler.post(() -> {
                    if (results.isEmpty()) {
                        showEmptyState();
                    } else {
                        featuredAdapter.setData(new ArrayList<>(results));
                        binding.rvFeatured.scrollToPosition(0);
                        showContent();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(this::showNetworkError);
            }
        });
    }

    private void loadRecentData() {
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> resp = RetrofitClient.getApiService()
                        .searchWithFilters("art", true, true, 
                                filterOptions.getDepartmentId(), 
                                filterOptions.getDateBegin(), 
                                filterOptions.getDateEnd(), 
                                filterOptions.getGeoLocation())
                        .execute();

                if (!resp.isSuccessful() || resp.body() == null || resp.body().getObjectIDs() == null) {
                    return;
                }

                List<Integer> allIds = resp.body().getObjectIDs();
                Collections.shuffle(allIds);
                List<Integer> selectedIds = allIds.subList(0, Math.min(10, allIds.size()));

                List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                CountDownLatch latch = new CountDownLatch(selectedIds.size());

                for (int id : selectedIds) {
                    executorService.execute(() -> {
                        try {
                            Response<MetArtwork> detail = RetrofitClient.getApiService()
                                    .getObjectDetail(id)
                                    .execute();
                            if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                results.add(detail.body());
                            }
                        } catch (Exception e) {
                            Log.e("MUSE_HOME", "Detail error: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await(15, TimeUnit.SECONDS);

                mainHandler.post(() -> {
                    if (!results.isEmpty()) {
                        recentAdapter.setData(new ArrayList<>(results));
                        binding.rvRecent.scrollToPosition(0);
                    }
                });

            } catch (Exception e) {
                Log.e("MUSE_HOME", "Recent error: " + e.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.rvFeatured.setVisibility(View.GONE);
            binding.rvRecent.setVisibility(View.GONE);
            binding.featuredHeader.setVisibility(View.GONE);
            binding.recentHeader.setVisibility(View.GONE);
            binding.layoutNetworkError.setVisibility(View.GONE);
            binding.layoutEmptyFilter.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvFeatured.setVisibility(View.VISIBLE);
        binding.rvRecent.setVisibility(View.VISIBLE);
        binding.featuredHeader.setVisibility(View.VISIBLE);
        binding.recentHeader.setVisibility(View.VISIBLE);
        binding.layoutNetworkError.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.btnRefreshToolbar.clearAnimation();
    }

    private void showNetworkError() {
        showLoading(false);
        binding.layoutNetworkError.setVisibility(View.VISIBLE);
        binding.btnRefreshToolbar.clearAnimation();
    }

    private void showEmptyState() {
        showLoading(false);
        binding.layoutEmptyFilter.setVisibility(View.VISIBLE);
        binding.btnRefreshToolbar.clearAnimation();
    }

    private void startRefreshAnimation() {
        RotateAnimation rotate = new RotateAnimation(0, 360, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        binding.btnRefreshToolbar.startAnimation(rotate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
