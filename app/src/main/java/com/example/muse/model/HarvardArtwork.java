package com.example.muse.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HarvardArtwork {
    @SerializedName("id") private int id;
    @SerializedName("title") private String title;
    @SerializedName("dated") private String dated;
    @SerializedName("datebegin") private int dateBegin;
    @SerializedName("dateend") private int dateEnd;
    @SerializedName("medium") private String medium;
    @SerializedName("technique") private String technique;
    @SerializedName("culture") private String culture;
    @SerializedName("classification") private String classification;
    @SerializedName("department") private String department;
    @SerializedName("description") private String description;
    @SerializedName("provenance") private String provenance;
    @SerializedName("creditline") private String creditline;
    @SerializedName("dimensions") private String dimensions;
    @SerializedName("primaryimageurl") private String primaryImageUrl;
    @SerializedName("imagepermissionlevel") private int imagePermissionLevel;
    @SerializedName("century") private String century;
    @SerializedName("period") private String period;
    @SerializedName("people") private List<HarvardPerson> people;
    @SerializedName("colors") private List<HarvardColor> colors;

    // Helper Methods
    public String getArtistDisplay() {
        if (people != null && !people.isEmpty()) {
            for (HarvardPerson p : people) {
                if ("Artist".equals(p.getRole())) {
                    String name = p.getDisplayName();
                    return (p.getDisplayDate() != null) ? name + "\n" + p.getDisplayDate() : name;
                }
            }
        }
        return "Seniman tidak diketahui";
    }

    public String getArtistName() {
        if (people != null && !people.isEmpty()) {
            for (HarvardPerson p : people) {
                if ("Artist".equals(p.getRole()) && p.getDisplayName() != null) return p.getDisplayName();
            }
        }
        return "Seniman tidak diketahui";
    }

    public String getDisplayDescription() {
        if (description != null && !description.isEmpty()) return description;
        if (provenance != null && !provenance.isEmpty()) return provenance;
        if (creditline != null && !creditline.isEmpty()) return creditline;
        return "Deskripsi karya ini belum tersedia.";
    }

    public String getDisplayImage() {
        // More lenient: Show image if URL exists
        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) return primaryImageUrl;
        return null;
    }

    public String getDisplayPeriod() {
        if (century != null && !century.isEmpty()) return century;
        if (dated != null && !dated.isEmpty()) return dated;
        return "Periode tidak diketahui";
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDated() { return dated; }
    public String getMedium() { return medium; }
    public String getCulture() { return culture; }
    public String getClassification() { return classification; }
    public String getDepartment() { return department; }
    public String getDimensions() { return dimensions; }
    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public int getImagePermissionLevel() { return imagePermissionLevel; }
}
