package com.example.busservice2020.model;

public class ModelHistoryRyc {

    private String pickuplocationname,destinationPlaceName,busname,buslicense,date;

    public ModelHistoryRyc(String pickuplocationname, String destinationPlaceName, String busname, String buslicense, String date) {
        this.pickuplocationname = pickuplocationname;
        this.destinationPlaceName = destinationPlaceName;
        this.busname = busname;
        this.buslicense = buslicense;
        this.date = date;
    }

    public ModelHistoryRyc() {
    }

    public String getPickuplocationname() {
        return pickuplocationname;
    }

    public void setPickuplocationname(String pickuplocationname) {
        this.pickuplocationname = pickuplocationname;
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

    public String getBuslicense() {
        return buslicense;
    }

    public void setBuslicense(String buslicense) {
        this.buslicense = buslicense;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
