package com.example.muse.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muse.R;
import com.example.muse.databinding.ItemArtworkFavoriteBinding;
import com.example.muse.model.Favorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<Favorite> favorites = new ArrayList<>();
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onItemClick(Favorite favorite);
        void onHeartClick(Favorite favorite);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArtworkFavoriteBinding binding = ItemArtworkFavoriteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favorite favorite = favorites.get(position);
        holder.binding.tvTitle.setText(favorite.getTitle());
        holder.binding.tvArtist.setText(favorite.getArtist());

        // Load image from URL directly via Glide (Met Museum API)
        Glide.with(holder.itemView.getContext())
                .load(favorite.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.binding.ivArtwork);

        // Card click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(favorite);
            }
        });

        // Heart click listener
        holder.binding.ivHeart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHeartClick(favorite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemArtworkFavoriteBinding binding;

        ViewHolder(ItemArtworkFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
