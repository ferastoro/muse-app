package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class Artwork {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("artist_display")
    private String artistDisplay;

    @SerializedName("date_display")
    private String dateDisplay;

    @SerializedName("medium_display")
    private String mediumDisplay;

    @SerializedName("dimensions")
    private String dimensions;

    @SerializedName("description")
    private String description;

    @SerializedName("image_id")
    private String imageId;

    @SerializedName("artwork_type_title")
    private String artworkTypeTitle;

    @SerializedName("place_of_origin")
    private String placeOfOrigin;

    @SerializedName("thumbnail")
    private Thumbnail thumbnail;

    @SerializedName("is_public_domain")
    private boolean isPublicDomain;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtistDisplay() { return artistDisplay; }
    public void setArtistDisplay(String artistDisplay) { this.artistDisplay = artistDisplay; }

    public String getDateDisplay() { return dateDisplay; }
    public void setDateDisplay(String dateDisplay) { this.dateDisplay = dateDisplay; }

    public String getMediumDisplay() { return mediumDisplay; }
    public void setMediumDisplay(String mediumDisplay) { this.mediumDisplay = mediumDisplay; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public String getArtworkTypeTitle() { return artworkTypeTitle; }
    public void setArtworkTypeTitle(String artworkTypeTitle) { this.artworkTypeTitle = artworkTypeTitle; }

    public String getPlaceOfOrigin() { return placeOfOrigin; }
    public void setPlaceOfOrigin(String placeOfOrigin) { this.placeOfOrigin = placeOfOrigin; }

    public Thumbnail getThumbnail() { return thumbnail; }
    public void setThumbnail(Thumbnail thumbnail) { this.thumbnail = thumbnail; }

    public boolean isPublicDomain() { return isPublicDomain; }
    public void setPublicDomain(boolean publicDomain) { isPublicDomain = publicDomain; }
}
