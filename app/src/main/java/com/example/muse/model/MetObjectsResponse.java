package com.example.muse.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MetObjectsResponse {
    private int total;
    @SerializedName("objectIDs")
    private List<Integer> objectIDs;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Integer> getObjectIDs() {
        return objectIDs;
    }

    public void setObjectIDs(List<Integer> objectIDs) {
        this.objectIDs = objectIDs;
    }
}
