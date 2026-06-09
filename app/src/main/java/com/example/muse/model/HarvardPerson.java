package com.example.muse.model;

import com.google.gson.annotations.SerializedName;

public class HarvardPerson {
    @SerializedName("role") private String role;
    @SerializedName("displayname") private String displayName;
    @SerializedName("displaydate") private String displayDate;
    @SerializedName("birthplace") private String birthPlace;
    @SerializedName("deathplace") private String deathPlace;
    @SerializedName("culture") private String culture;
    @SerializedName("gender") private String gender;

    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public String getDisplayDate() { return displayDate; }
    public String getBirthPlace() { return birthPlace; }
    public String getDeathPlace() { return deathPlace; }
    public String getCulture() { return culture; }
    public String getGender() { return gender; }
}
