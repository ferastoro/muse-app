package com.example.muse.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.muse.R;
import com.example.muse.activity.DetailActivity;
import com.example.muse.adapter.FavoriteAdapter;
import com.example.muse.database.DatabaseHelper;
import com.example.muse.database.FavoriteDao;
import com.example.muse.databinding.FragmentFavoritBinding;
import com.example.muse.model.Favorite;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritFragment extends Fragment {

    private FragmentFavoritBinding binding;
    private FavoriteAdapter adapter;
    private DatabaseHelper dbHelper;
    private FavoriteDao favoriteDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        favoriteDao = new FavoriteDao(dbHelper);

        setupRecyclerView();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter();
        binding.rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvFavorites.setAdapter(adapter);

        adapter.setOnFavoriteClickListener(new FavoriteAdapter.OnFavoriteClickListener() {
            @Override
            public void onItemClick(Favorite favorite) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("artwork_id", favorite.getId());
                intent.putExtra("title", favorite.getTitle());
                intent.putExtra("image_url", favorite.getImageUrl());
                intent.putExtra("artist", favorite.getArtist());
                intent.putExtra("date", favorite.getDateDisplay());
                intent.putExtra("from_favorite", true);
                startActivity(intent);
            }

            @Override
            public void onHeartClick(Favorite favorite) {
                executorService.execute(() -> {
                    favoriteDao.deleteFavorite(favorite.getId());
                    mainHandler.post(() -> loadFavorites());
                });
            }
        });
    }

    private void setupButtons() {
        binding.btnExplore.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_home));
    }

    private void loadFavorites() {
        executorService.execute(() -> {
            List<Favorite> favorites = favoriteDao.getAllFavorites();
            mainHandler.post(() -> updateUI(favorites));
        });
    }

    private void updateUI(List<Favorite> favorites) {
        if (favorites.isEmpty()) {
            binding.rvFavorites.setVisibility(View.GONE);
            binding.headerFavorit.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvFavorites.setVisibility(View.VISIBLE);
            binding.headerFavorit.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            adapter.setFavorites(favorites);
            binding.tvCount.setText(getString(R.string.favorite_count, favorites.size()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
