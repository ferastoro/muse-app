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
                    performSearch(query);
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
            // Logic to reset filters if implemented
            String query = binding.searchView.getQuery().toString();
            if (!query.isEmpty()) performSearch(query);
        });
    }

    public void onFilterChanged(FilterOptions filterOptions) {
        String query = binding.searchView.getQuery().toString();
        if (!query.isEmpty()) {
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.resultsHeader.setVisibility(View.GONE);
        binding.layoutEmptyFilter.setVisibility(View.GONE);
        binding.layoutNetworkError.setVisibility(View.GONE);

        RetrofitClient.getClient().searchArtworks(
                BuildConfig.HARVARD_API_KEY, query, 1, 0, 30, FIELDS
        ).enqueue(new Callback<HarvardListResponse>() {
            @Override
            public void onResponse(Call<HarvardListResponse> call, Response<HarvardListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<HarvardArtwork> results = new ArrayList<>();
                    for (HarvardArtwork art : response.body().getRecords()) {
                        if (art.getDisplayImage() != null) {
                            results.add(art);
                        }
                    }

                    if (results.isEmpty()) {
                        binding.layoutEmptyFilter.setVisibility(View.VISIBLE);
                    } else {
                        binding.resultsHeader.setVisibility(View.VISIBLE);
                        binding.tvResultCount.setText(results.size() + " ditemukan");
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                        binding.rvSearchResults.setAdapter(new SearchArtworkAdapter(results));
                    }
                } else {
                    binding.layoutNetworkError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<HarvardListResponse> call, Throwable t) {
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
