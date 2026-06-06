package com.example.muse.model;

public class FilterOptions {
    private Integer departmentId = null;
    private Integer dateBegin = null;
    private Integer dateEnd = null;
    private String geoLocation = null;
    private String eraLabel = "Semua Era";
    private String locationLabel = "Semua Wilayah";
    private String typeLabel = "Semua Tipe";

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public Integer getDateBegin() { return dateBegin; }
    public void setDateBegin(Integer dateBegin) { this.dateBegin = dateBegin; }

    public Integer getDateEnd() { return dateEnd; }
    public void setDateEnd(Integer dateEnd) { this.dateEnd = dateEnd; }

    public String getGeoLocation() { return geoLocation; }
    public void setGeoLocation(String geoLocation) { this.geoLocation = geoLocation; }

    public String getEraLabel() { return eraLabel; }
    public void setEraLabel(String eraLabel) { this.eraLabel = eraLabel; }

    public String getLocationLabel() { return locationLabel; }
    public void setLocationLabel(String locationLabel) { this.locationLabel = locationLabel; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public void reset() {
        departmentId = null;
        dateBegin = null;
        dateEnd = null;
        geoLocation = null;
        eraLabel = "Semua Era";
        locationLabel = "Semua Wilayah";
        typeLabel = "Semua Tipe";
    }
}
