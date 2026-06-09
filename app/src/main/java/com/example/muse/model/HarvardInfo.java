package com.example.muse.model;

public class HarvardInfo {
    private int totalrecordsperquery;
    private int totalrecords;
    private int pages;
    private int page;
    private String next;

    public int getTotalrecords() { return totalrecords; }
    public int getTotalrecordsperquery() { return totalrecordsperquery; }
    public int getPages() { return pages; }
    public int getPage() { return page; }
    public String getNext() { return next; }
}
