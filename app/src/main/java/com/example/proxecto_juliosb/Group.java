package com.example.proxecto_juliosb;

import android.graphics.Bitmap;

public class Group {
    private String title;
    private String subtitle;
    private Bitmap image;

    public Group(String title, String subtitle, Bitmap image) {
        this.title = title;
        this.subtitle = subtitle;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
