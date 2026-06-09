package com.example.muse.model;

import java.util.HashSet;
import java.util.Set;

public class FilterOptions {
    private Set<String> classifications = new HashSet<>();
    private Set<String> cultures = new HashSet<>();
    private Set<EraRange> activeEras = new HashSet<>();
    
    private Integer dateBegin = null;
    private Integer dateEnd = null;
    private String century = null;

    private static class EraRange {
        String name;
        Integer begin;
        Integer end;

        EraRange(String name, Integer begin, Integer end) {
            this.name = name;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EraRange eraRange = (EraRange) o;
            return name.equals(eraRange.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public boolean hasFilters() {
        return !classifications.isEmpty() || !cultures.isEmpty() || !activeEras.isEmpty() || 
               dateBegin != null || dateEnd != null || century != null;
    }

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

    public void toggleCulture(String culture) {
        if (cultures.contains(culture)) {
            cultures.remove(culture);
        } else {
            cultures.add(culture);
        }
    }

    public String getCulture() {
        if (cultures.isEmpty()) return null;
        return String.join("|", cultures);
    }

    public void toggleEra(String name, Integer begin, Integer end, String century) {
        EraRange newEra = new EraRange(name, begin, end);
        if (activeEras.contains(newEra)) {
            activeEras.remove(newEra);
        } else {
            activeEras.add(newEra);
        }
        updateEraDates();
        this.century = (activeEras.size() == 1) ? century : null;
    }

    private void updateEraDates() {
        if (activeEras.isEmpty()) {
            this.dateBegin = null;
            this.dateEnd = null;
            return;
        }

        Integer minBegin = null;
        Integer maxEnd = null;

        for (EraRange era : activeEras) {
            if (era.begin != null) {
                if (minBegin == null || era.begin < minBegin) minBegin = era.begin;
            }
            if (era.end != null) {
                if (maxEnd == null || era.end > maxEnd) maxEnd = era.end;
            }
        }

        this.dateBegin = minBegin;
        this.dateEnd = maxEnd;
    }

    public Integer getDateBegin() { return dateBegin; }
    public Integer getDateEnd() { return dateEnd; }
    public String getCentury() { return century; }

    public void reset() {
        classifications.clear();
        cultures.clear();
        activeEras.clear();
        dateBegin = null;
        dateEnd = null;
        century = null;
    }

    public boolean isClassificationSelected(String c) { return classifications.contains(c); }
    public boolean isCultureSelected(String c) { return cultures.contains(c); }
    public boolean isEraSelected(String e) { 
        for (EraRange era : activeEras) if (era.name.equals(e)) return true;
        return false;
    }
}
