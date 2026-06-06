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

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchArtworkAdapter adapter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentQuery = "";
    
    private FilterOptions filterOptions = new FilterOptions();

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
        setupButtons();

        showEmptyFilterState(true);
    }

    // FIX: Add onFilterChanged to resolve compilation error and enable dynamic filtering
    public void onFilterChanged(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        if (isAdded() && !currentQuery.isEmpty()) {
            performSearch(currentQuery);
        } else if (isAdded() && currentQuery.isEmpty()) {
            showEmptyFilterState(true);
        }
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
                    showEmptyFilterState(true);
                } else {
                    searchRunnable = () -> performSearch(newText);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
                return true;
            }
        });
    }

    private void setupButtons() {
        binding.btnRetryNetwork.setOnClickListener(v -> performSearch(currentQuery));
        binding.btnResetFilter.setOnClickListener(v -> {
            filterOptions.reset();
            performSearch(currentQuery);
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        if (!isNetworkAvailable()) {
            showNetworkError();
            return;
        }

        showLoading();
        executorService.execute(() -> {
            try {
                Response<MetObjectsResponse> response = RetrofitClient.getApiService()
                        .searchWithFilters(query, true, true, 
                                filterOptions.getDepartmentId(),
                                filterOptions.getDateBegin(),
                                filterOptions.getDateEnd(),
                                filterOptions.getGeoLocation())
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MUSE_SEARCH", "SEARCH_SUCCESS");
                    List<Integer> ids = response.body().getObjectIDs();
                    
                    if (ids == null || ids.isEmpty()) {
                        Log.d("MUSE_SEARCH", "SEARCH_EMPTY");
                        mainHandler.post(() -> showEmptyFilterState(false));
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
                                Log.e("MUSE_SEARCH", "Detail fetch failed for ID: " + id);
                            } finally {
                                latch.countDown();
                            }
                        });
                    }

                    latch.await(15, TimeUnit.SECONDS);

                    mainHandler.post(() -> {
                        if (results.isEmpty()) {
                            Log.d("MUSE_SEARCH", "SEARCH_EMPTY (details failed)");
                            showEmptyFilterState(false);
                        } else {
                            showResults(new ArrayList<>(results));
                        }
                    });
                } else {
                    Log.e("MUSE_SEARCH", "SEARCH_NETWORK_ERROR: Response not successful");
                    mainHandler.post(this::showNetworkError);
                }
            } catch (java.io.IOException e) {
                Log.e("MUSE_SEARCH", "SEARCH_NETWORK_ERROR: Connection failed", e);
                mainHandler.post(this::showNetworkError);
            } catch (Exception e) {
                Log.e("MUSE_SEARCH", "SEARCH_NETWORK_ERROR: Unexpected error", e);
                mainHandler.post(this::showNetworkError);
            }
        });
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.layoutNetworkError.setVisibility(View.GONE);
    }

    private void showResults(List<MetArtwork> results) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.resultsHeader.setVisibility(View.VISIBLE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.layoutNetworkError.setVisibility(View.GONE);

        adapter.setData(results);
        binding.tvResultCount.setText(getString(R.string.search_results_count, results.size()));
    }

    private void showEmptyFilterState(boolean isInitial) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.VISIBLE);
        binding.layoutNetworkError.setVisibility(View.GONE);
        
        if (isInitial) {
            binding.btnResetFilter.setVisibility(View.GONE);
        } else {
            binding.btnResetFilter.setVisibility(View.VISIBLE);
        }
    }

    private void showNetworkError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.layoutNetworkError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
