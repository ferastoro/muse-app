package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class HarvardColor {
    @SerializedName("color") private String color;
    @SerializedName("hue") private String hue;
    @SerializedName("percent") private double percent;

    public String getColor() { return color; }
    public String getHue() { return hue; }
    public double getPercent() { return percent; }
}
