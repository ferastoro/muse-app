package com.example.muse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.muse.R;
import com.example.muse.model.HarvardArtwork;
import java.util.List;

public class RelatedArtworkAdapter extends RecyclerView.Adapter<RelatedArtworkAdapter.ViewHolder> {
    private List<HarvardArtwork> artworks;

    public RelatedArtworkAdapter(List<HarvardArtwork> artworks) {
        this.artworks = artworks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artwork_related, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HarvardArtwork artwork = artworks.get(position);
        
        // FIX 2: Glide call yang benar
        Glide.with(holder.image.getContext())
                .load(artwork.getDisplayImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.image);
    }

    @Override
    public int getItemCount() { return artworks.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivArtwork);
        }
    }
}
