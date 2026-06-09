package com.example.muse.model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterOptions {
    private Set<String> classifications = new HashSet<>();
    private Set<String> cultures = new HashSet<>();
    private Set<String> eras = new HashSet<>();
    
    private Integer dateBegin = null;
    private Integer dateEnd = null;
    private String century = null;

    public String getClassification() {
        if (classifications.isEmpty()) return null;
        return String.join("|", classifications);
    }

    public void toggleClassification(String classification) {
        if (classifications.contains(classification)) {
            classifications.remove(classification);
        } else {
            classifications.add(classification);
        }
    }

    public void setClassification(String classification) {
        this.classifications.clear();
        if (classification != null) this.classifications.add(classification);
    }

    public String getCulture() {
        if (cultures.isEmpty()) return null;
        return String.join("|", cultures);
    }

    public void toggleCulture(String culture) {
        if (culture.contains("|")) {
            // Special case for groups like Europe
            String[] parts = culture.split("\\|");
            boolean allPresent = true;
            for (String p : parts) if (!cultures.contains(p)) allPresent = false;
            
            if (allPresent) {
                for (String p : parts) cultures.remove(p);
            } else {
                for (String p : parts) cultures.add(p);
            }
        } else {
            if (cultures.contains(culture)) {
                cultures.remove(culture);
            } else {
                cultures.add(culture);
            }
        }
    }

    public void setCulture(String culture) {
        this.cultures.clear();
        if (culture != null) {
            for (String s : culture.split("\\|")) this.cultures.add(s);
        }
    }

    public void toggleEra(String era, Integer begin, Integer end, String century) {
        if (eras.contains(era)) {
            eras.remove(era);
            // This is simplified, ideally we'd track min/max dates if multiple eras selected
            // But for now let's just use the last one added or clear if none
            updateEraDates();
        } else {
            eras.add(era);
            this.dateBegin = begin;
            this.dateEnd = end;
            this.century = century;
        }
    }
    
    private void updateEraDates() {
        // Very basic implementation: just use null if multiple or none for now
        // to avoid complex date range merging in this step
        if (eras.size() != 1) {
            this.dateBegin = null;
            this.dateEnd = null;
            this.century = null;
        }
    }

    public Integer getDateBegin() { return dateBegin; }
    public void setDateBegin(Integer dateBegin) { this.dateBegin = dateBegin; }

    public Integer getDateEnd() { return dateEnd; }
    public void setDateEnd(Integer dateEnd) { this.dateEnd = dateEnd; }

    public String getCentury() { return century; }
    public void setCentury(String century) { this.century = century; }

    public void reset() {
        classifications.clear();
        cultures.clear();
        eras.clear();
        dateBegin = null;
        dateEnd = null;
        century = null;
    }
    
    public boolean isClassificationSelected(String c) { return classifications.contains(c); }
    public boolean isCultureSelected(String c) { 
        if (c == null) return cultures.isEmpty();
        if (c.contains("|")) {
            for (String p : c.split("\\|")) if (!cultures.contains(p)) return false;
            return true;
        }
        return cultures.contains(c); 
    }
    public boolean isEraSelected(String e) { return eras.contains(e); }
}
