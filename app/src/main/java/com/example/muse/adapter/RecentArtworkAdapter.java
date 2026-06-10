package com.example.muse.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.muse.R;
import com.example.muse.activity.DetailActivity;
import com.example.muse.model.HarvardArtwork;
import java.util.List;

public class RecentArtworkAdapter extends RecyclerView.Adapter<RecentArtworkAdapter.ViewHolder> {
    private List<HarvardArtwork> artworks;

    public RecentArtworkAdapter(List<HarvardArtwork> artworks) {
        this.artworks = artworks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artwork_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HarvardArtwork artwork = artworks.get(position);
        holder.title.setText(artwork.getTitle());
        holder.artist.setText(artwork.getArtistName());
        holder.year.setText(artwork.getDated());

        String imageUrl = artwork.getDisplayImage();

        // Optimasi Glide: thumbnail, diskCache, dan override size
        Glide.with(holder.image.getContext())
                .load(imageUrl)
                .thumbnail(0.2f) // Load 20% quality first for "fast" feel
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("artwork_id", artwork.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return artworks.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, artist, year;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivArtwork);
            title = itemView.findViewById(R.id.tvTitle);
            artist = itemView.findViewById(R.id.tvArtist);
            year = itemView.findViewById(R.id.tvYear);
        }
    }
}
