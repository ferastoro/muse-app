package com.example.muse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muse.BuildConfig;
import com.example.muse.adapter.SearchArtworkAdapter;
import com.example.muse.databinding.FragmentSearchBinding;
import com.example.muse.model.FilterOptions;
import com.example.muse.model.HarvardArtwork;
import com.example.muse.model.HarvardListResponse;
import com.example.muse.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private final String FIELDS = "id,title,primaryimageurl,people,dated,medium,culture,classification,imagepermissionlevel";
    private FilterOptions currentFilters = new FilterOptions();

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
    }

    private void setupRecyclerView() {
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    performSearch(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupButtons() {
        binding.btnRetryNetwork.setOnClickListener(v -> {
            String query = binding.searchView.getQuery().toString();
            if (!query.isEmpty()) performSearch(query);
        });
        
        binding.btnResetFilter.setOnClickListener(v -> {
            binding.searchView.setQuery("", false);
            binding.rvSearchResults.setVisibility(View.GONE);
            binding.resultsHeader.setVisibility(View.GONE);
            binding.layoutEmptyFilter.setVisibility(View.GONE);
            currentFilters.reset();
        });
    }

    public void onFilterChanged(FilterOptions filterOptions) {
        this.currentFilters = filterOptions;
        String query = binding.searchView.getQuery().toString();
        if (!query.isEmpty()) {
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        if (binding == null) return;
        
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.layoutNetworkError.setVisibility(View.GONE);

        Call<HarvardListResponse> call;
        if (currentFilters.hasFilters()) {
            call = RetrofitClient.getClient().searchWithFilters(
                    BuildConfig.HARVARD_API_KEY, query, 1, null,
                    currentFilters.getClassification(),
                    currentFilters.getCulture(),
                    currentFilters.getDateBegin(),
                    currentFilters.getDateEnd(),
                    currentFilters.getCentury(),
                    50, "rank", FIELDS
            );
        } else {
            call = RetrofitClient.getClient().searchArtworks(
                    BuildConfig.HARVARD_API_KEY, query, 1, null, 50, "rank", FIELDS
            );
        }

        call.enqueue(new Callback<HarvardListResponse>() {
            @Override
            public void onResponse(Call<HarvardListResponse> call, Response<HarvardListResponse> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<HarvardArtwork> records = response.body().getRecords();
                    List<HarvardArtwork> results = new ArrayList<>();
                    
                    if (records != null) {
                        for (HarvardArtwork art : records) {
                            if (art.getDisplayImage() != null) {
                                results.add(art);
                            }
                        }
                    }

                    if (results.isEmpty()) {
                        binding.layoutEmptyFilter.setVisibility(View.VISIBLE);
                    } else {
                        binding.resultsHeader.setVisibility(View.VISIBLE);
                        binding.tvResultCount.setText(results.size() + " karya ditemukan");
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                        binding.rvSearchResults.setAdapter(new SearchArtworkAdapter(results));
                    }
                } else {
                    binding.layoutNetworkError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<HarvardListResponse> call, Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutNetworkError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
