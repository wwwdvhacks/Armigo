package com.example.zayn.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Region {
    private String name;
    private String history;
    private String kitchen;
    private String dances_songs;

    public Region() {
        // Default constructor required for calls to DataSnapshot.getValue(Region.class)
    }

    public Region(String name, String history, String kitchen, String dances_songs) {
        this.name = name;
        this.history = history;
        this.kitchen = kitchen;
        this.dances_songs = dances_songs;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public String getKitchen() { return kitchen; }
    public void setKitchen(String kitchen) { this.kitchen = kitchen; }

    public String getDances_songs() { return dances_songs; }
    public void setDances_songs(String dances_songs) { this.dances_songs = dances_songs; }
} 