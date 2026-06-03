package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class MetArtwork {
    @SerializedName("objectID")
    private int objectID;
    private String title;
    private String artistDisplayName;
    private String artistDisplayBio;
    private String objectDate;
    private String medium;
    private String dimensions;
    private String department;
    private String culture;
    private String classification;
    private String primaryImage;
    private String primaryImageSmall;
    private String creditLine;
    private boolean isHighlight;
    private boolean isPublicDomain;
    private String objectURL;

    // Helper methods
    public String getDescription() {
        if (creditLine != null && !creditLine.isEmpty()) {
            return creditLine;
        }
        return "Karya ini merupakan bagian dari koleksi Metropolitan Museum of Art.";
    }

    public String getDisplayImage() {
        if (primaryImageSmall != null && !primaryImageSmall.isEmpty()) {
            return primaryImageSmall;
        }
        if (primaryImage != null && !primaryImage.isEmpty()) {
            return primaryImage;
        }
        return null;
    }

    public String getArtistDisplay() {
        if (artistDisplayName != null && !artistDisplayName.isEmpty()) {
            if (artistDisplayBio != null && !artistDisplayBio.isEmpty()) {
                return artistDisplayName + "\n" + artistDisplayBio;
            }
            return artistDisplayName;
        }
        return "Seniman tidak diketahui";
    }

    // Standard getters
    public int getObjectID() { return objectID; }
    public String getTitle() { return title; }
    public String getArtistDisplayName() { return artistDisplayName; }
    public String getArtistDisplayBio() { return artistDisplayBio; }
    public String getObjectDate() { return objectDate; }
    public String getMedium() { return medium; }
    public String getDimensions() { return dimensions; }
    public String getDepartment() { return department; }
    public String getCulture() { return culture; }
    public String getClassification() { return classification; }
    public String getPrimaryImage() { return primaryImage; }
    public String getPrimaryImageSmall() { return primaryImageSmall; }
    public String getCreditLine() { return creditLine; }
    public boolean isHighlight() { return isHighlight; }
    public boolean isPublicDomain() { return isPublicDomain; }
    public String getObjectURL() { return objectURL; }
}
