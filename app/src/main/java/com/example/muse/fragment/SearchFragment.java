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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muse.R;
import com.example.muse.activity.DetailActivity;
import com.example.muse.adapter.SearchArtworkAdapter;
import com.example.muse.databinding.FragmentSearchBinding;
import com.example.muse.model.MetArtwork;
import com.example.muse.model.MetObjectsResponse;
import com.example.muse.network.RetrofitClient;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchArtworkAdapter adapter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentQuery = "";
    private Integer selectedDepartmentId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchView();
        setupFilters();
        setupButtons();

        showEmptyState(true);
    }

    private void setupRecyclerView() {
        adapter = new SearchArtworkAdapter();
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchResults.setAdapter(adapter);

        adapter.setOnItemClickListener(artwork -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("artwork_id", artwork.getObjectID());
            intent.putExtra("title", artwork.getTitle());
            intent.putExtra("image_url", artwork.getDisplayImage());
            intent.putExtra("artist", artwork.getArtistDisplayName());
            intent.putExtra("date", artwork.getObjectDate());
            intent.putExtra("from_favorite", false);
            startActivity(intent);
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (newText.isEmpty()) {
                    showEmptyState(true);
                } else {
                    searchRunnable = () -> performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
                return true;
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupSearch.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedDepartmentId = null;
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                String text = chip.getText().toString();
                if (text.equals(getString(R.string.chip_recommendation))) {
                    selectedDepartmentId = 11; // Paintings
                } else {
                    selectedDepartmentId = null;
                }
            }
            if (!currentQuery.isEmpty()) {
                performSearch(currentQuery);
            }
        });
    }

    private void setupButtons() {
        binding.btnRetry.setOnClickListener(v -> performSearch(currentQuery));
        binding.btnExplore.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        showLoading();
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> response = RetrofitClient.getApiService()
                        .searchObjects(query, true, true, selectedDepartmentId)
                        .execute();

                if (response.isSuccessful() && response.body() != null && response.body().getObjectIDs() != null) {
                    List<Integer> ids = response.body().getObjectIDs();
                    if (ids.isEmpty()) {
                        mainHandler.post(() -> showEmptyState(false));
                        return;
                    }

                    List<Integer> first20 = ids.subList(0, Math.min(20, ids.size()));
                    List<MetArtwork> results = Collections.synchronizedList(new ArrayList<>());
                    CountDownLatch latch = new CountDownLatch(first20.size());

                    for (int id : first20) {
                        executorService.execute(() -> {
                            try {
                                Response<MetArtwork> detail = RetrofitClient.getApiService().getObjectDetail(id).execute();
                                if (detail.isSuccessful() && detail.body() != null && detail.body().getDisplayImage() != null) {
                                    results.add(detail.body());
                                }
                            } catch (Exception e) {
                                Log.e("MUSE_M3", "Search detail error: " + e.getMessage());
                            } finally {
                                latch.countDown();
                            }
                        });
                    }

                    latch.await(15, TimeUnit.SECONDS);

                    mainHandler.post(() -> {
                        if (results.isEmpty()) {
                            showEmptyState(false);
                        } else {
                            showResults(new ArrayList<>(results));
                        }
                    });
                } else {
                    mainHandler.post(this::showError);
                }
            } catch (Exception e) {
                Log.e("MUSE_M3", "Search error: " + e.getMessage());
                mainHandler.post(this::showError);
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showResults(List<MetArtwork> results) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.resultsHeader.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        adapter.setData(results);
        binding.tvResultCount.setText(getString(R.string.search_results_count, results.size()));
    }

    private void showEmptyState(boolean isInitial) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);

        if (isInitial) {
            binding.tvEmptyTitle.setText(R.string.title_search);
            binding.tvEmptyDesc.setText(R.string.search_hint);
        } else {
            binding.tvEmptyTitle.setText(R.string.not_found_title);
            binding.tvEmptyDesc.setText(R.string.not_found_desc);
        }
    }

    private void showError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
