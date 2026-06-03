package com.example.muse.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.muse.R;
import com.example.muse.databinding.ItemArtworkFeaturedBinding;
import com.example.muse.model.MetArtwork;

import java.util.ArrayList;
import java.util.List;

public class FeaturedArtworkAdapter extends RecyclerView.Adapter<FeaturedArtworkAdapter.ViewHolder> {

    private List<MetArtwork> artworks = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MetArtwork artwork);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<MetArtwork> artworks) {
        this.artworks = artworks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArtworkFeaturedBinding binding = ItemArtworkFeaturedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MetArtwork artwork = artworks.get(position);
        holder.binding.tvTitle.setText(artwork.getTitle());
        holder.binding.tvCategory.setText(artwork.getClassification());

        Glide.with(holder.itemView.getContext())
                .load(artwork.getDisplayImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.binding.ivArtwork);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(artwork);
            }
        });

        holder.binding.btnVisit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(artwork);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemArtworkFeaturedBinding binding;

        ViewHolder(ItemArtworkFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
