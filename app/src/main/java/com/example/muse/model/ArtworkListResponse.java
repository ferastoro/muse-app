package com.example.muse.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArtworkListResponse {
    @SerializedName("data")
    private List<Artwork> data;

    public List<Artwork> getData() {
        return data;
    }

    public void setData(List<Artwork> data) {
        this.data = data;
    }
}
