package com.example.muse.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.muse.R;
import com.example.muse.activity.DetailActivity;
import com.example.muse.model.HarvardArtwork;
import java.util.List;

public class SearchArtworkAdapter extends RecyclerView.Adapter<SearchArtworkAdapter.ViewHolder> {
    private List<HarvardArtwork> artworks;

    public SearchArtworkAdapter(List<HarvardArtwork> artworks) {
        this.artworks = artworks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artwork_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HarvardArtwork artwork = artworks.get(position);
        holder.title.setText(artwork.getTitle());
        holder.artist.setText(artwork.getArtistName());
        holder.location.setText(artwork.getCulture());

        // Optimasi Glide: thumbnail dan diskCache
        Glide.with(holder.image.getContext())
                .load(artwork.getDisplayImage())
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.image);

        // FIX: Tambahkan OnClickListener agar card bisa dipencet
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
        TextView title, artist, location;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivArtwork);
            title = itemView.findViewById(R.id.tvTitle);
            artist = itemView.findViewById(R.id.tvArtist);
            location = itemView.findViewById(R.id.tvLocation);
        }
    }
}
