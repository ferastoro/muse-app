package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class ArtworkDetailResponse {
    @SerializedName("data")
    private Artwork data;

    public Artwork getData() {
        return data;
    }

    public void setData(Artwork data) {
        this.data = data;
    }
}
