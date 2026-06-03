package com.example.muse.model;

public class Favorite {
    private int id;
    private String title;
    private String artist;
    private String dateDisplay;
    private String medium;
    private String imageUrl;
    private String description;
    private long savedAt;

    public Favorite() {}

    public Favorite(int id, String title, String artist, String dateDisplay, String medium, String imageUrl, String description, long savedAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.dateDisplay = dateDisplay;
        this.medium = medium;
        this.imageUrl = imageUrl;
        this.description = description;
        this.savedAt = savedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getDateDisplay() { return dateDisplay; }
    public void setDateDisplay(String dateDisplay) { this.dateDisplay = dateDisplay; }

    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getSavedAt() { return savedAt; }
    public void setSavedAt(long savedAt) { this.savedAt = savedAt; }
}
