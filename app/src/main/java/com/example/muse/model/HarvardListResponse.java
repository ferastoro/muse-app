package com.example.muse.model;

import java.util.List;

public class HarvardListResponse {
    private HarvardInfo info;
    private List<HarvardArtwork> records;

    public HarvardInfo getInfo() { return info; }
    public List<HarvardArtwork> getRecords() { return records; }
}
