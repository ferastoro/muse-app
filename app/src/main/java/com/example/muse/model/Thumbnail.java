package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class Thumbnail {
    @SerializedName("lqip")
    private String lqip;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    @SerializedName("alt_text")
    private String altText;

    public String getLqip() {
        return lqip;
    }

    public void setLqip(String lqip) {
        this.lqip = lqip;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }
}
