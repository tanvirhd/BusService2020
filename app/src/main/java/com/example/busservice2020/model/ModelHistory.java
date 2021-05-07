package com.example.busservice2020.model;

import com.google.android.gms.maps.model.LatLng;

public class ModelHistory {
    double startLat,startlang;
    private String userId,destinationPlaceName,busname,date,buslicense;

    public ModelHistory() {
    }

    public ModelHistory(double startLat, double startlang, String userId, String destinationPlaceName, String busname, String date, String buslicense) {
        this.startLat = startLat;
        this.startlang = startlang;
        this.userId = userId;
        this.destinationPlaceName = destinationPlaceName;
        this.busname = busname;
        this.date = date;
        this.buslicense = buslicense;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartlang() {
        return startlang;
    }

    public void setStartlang(double startlang) {
        this.startlang = startlang;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDestinationPlaceName() {
        return destinationPlaceName;
    }

    public void setDestinationPlaceName(String destinationPlaceName) {
        this.destinationPlaceName = destinationPlaceName;
    }

    public String getBusname() {
        return busname;
    }

    public void setBusname(String busname) {
        this.busname = busname;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBuslicense() {
        return buslicense;
    }

    public void setBuslicense(String buslicense) {
        this.buslicense = buslicense;
    }
}
