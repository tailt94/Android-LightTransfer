package com.terralogic.alexle.lighttransfer.model;

/**
 * Created by alex.le on 23-Aug-17.
 */

public class Picture {
    private String name;
    private String url;

    public Picture() {
    }

    public Picture(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
