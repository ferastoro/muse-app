package com.example.muse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muse.BuildConfig;
import com.example.muse.R;
import com.example.muse.activity.HomeActivity;
import com.example.muse.adapter.FeaturedArtworkAdapter;
import com.example.muse.adapter.RecentArtworkAdapter;
import com.example.muse.databinding.FragmentHomeBinding;
import com.example.muse.model.FilterOptions;
import com.example.muse.model.HarvardArtwork;
import com.example.muse.model.HarvardListResponse;
import com.example.muse.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private static List<HarvardArtwork> cachedFeatured = null;
    private static List<HarvardArtwork> cachedRecent = null;
    private FilterOptions currentFilters = new FilterOptions();
    
    private final String FIELDS = "id,title,primaryimageurl,people,dated,medium,culture,classification,description,imagepermissionlevel";

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
        setupButtons();
        setupSwipeRefresh();

        if (cachedFeatured == null || cachedRecent == null) {
            loadData();
        } else {
            displayCachedData();
        }
    }

    private void setupRecyclerViews() {
        binding.rvFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupButtons() {
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).openDrawer();
            }
        });

        binding.btnRefreshToolbar.setOnClickListener(v -> {
            refreshData();
        });

        binding.btnSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.navigation_search);
                }
            }
        });

        // Retry and Reset buttons (Direct access via binding as they are inline in fragment_home.xml)
        binding.btnRetryNetwork.setOnClickListener(v -> loadData());
        
        binding.btnResetFilter.setOnClickListener(v -> {
            currentFilters.reset();
            refreshData();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.muse_gold));
        binding.swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        cachedFeatured = null;
        cachedRecent = null;
        loadData();
    }

    public void onFilterChanged(FilterOptions filterOptions) {
        this.currentFilters = filterOptions;
        refreshData();
    }

    private void loadData() {
        // Show loading state if not already refreshing via swipe
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        
        binding.layoutNetworkError.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        
        loadFeatured();
        loadRecent(new Random().nextInt(50) + 1);
    }

    private void displayCachedData() {
        if (cachedFeatured != null) {
            binding.rvFeatured.setAdapter(new FeaturedArtworkAdapter(cachedFeatured));
        }
        if (cachedRecent != null) {
            binding.rvRecent.setAdapter(new RecentArtworkAdapter(cachedRecent));
        }
    }

    private void loadFeatured() {
        String classification = currentFilters.getClassification() != null ? currentFilters.getClassification() : "Paintings";
        
        RetrofitClient.getClient().getFeaturedArtworks(
                BuildConfig.HARVARD_API_KEY, 1, 0, classification, "totalpageviews", 20, FIELDS
        ).enqueue(new Callback<HarvardListResponse>() {
            @Override
            public void onResponse(Call<HarvardListResponse> call, Response<HarvardListResponse> response) {
                checkLoadingFinished();
                if (response.isSuccessful() && response.body() != null) {
                    List<HarvardArtwork> results = new ArrayList<>();
                    for (HarvardArtwork art : response.body().getRecords()) {
                        if (art.getDisplayImage() != null) {
                            results.add(art);
                            if (results.size() >= 10) break;
                        }
                    }
                    cachedFeatured = results;
                    binding.rvFeatured.setAdapter(new FeaturedArtworkAdapter(cachedFeatured));
                    
                    if (results.isEmpty()) {
                        binding.layoutEmptyFilter.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<HarvardListResponse> call, Throwable t) {
                checkLoadingFinished();
                binding.layoutNetworkError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadRecent(int page) {
        RetrofitClient.getClient().getRecentArtworks(
                BuildConfig.HARVARD_API_KEY, 1, 0, 20, page, FIELDS
        ).enqueue(new Callback<HarvardListResponse>() {
            @Override
            public void onResponse(Call<HarvardListResponse> call, Response<HarvardListResponse> response) {
                checkLoadingFinished();
                if (response.isSuccessful() && response.body() != null) {
                    List<HarvardArtwork> results = new ArrayList<>();
                    for (HarvardArtwork art : response.body().getRecords()) {
                        if (art.getDisplayImage() != null) {
                            results.add(art);
                            if (results.size() >= 10) break;
                        }
                    }
                    cachedRecent = results;
                    binding.rvRecent.setAdapter(new RecentArtworkAdapter(cachedRecent));
                }
            }
            @Override
            public void onFailure(Call<HarvardListResponse> call, Throwable t) {
                checkLoadingFinished();
            }
        });
    }

    private void checkLoadingFinished() {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
