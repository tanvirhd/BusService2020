package com.example.busservice2020.model;

public class AvailableBus {
    private String busid, companyname,rem_time_distance,route;

    public AvailableBus() {
    }

    public AvailableBus(String busid, String companyname, String rem_time_distance, String route) {
        this.busid = busid;
        this.companyname = companyname;
        this.rem_time_distance = rem_time_distance;
        this.route = route;
    }

    public String getBusid() {
        return busid;
    }

    public void setBusid(String busid) {
        this.busid = busid;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getRem_time_distance() {
        return rem_time_distance;
    }

    public void setRem_time_distance(String rem_time_distance) {
        this.rem_time_distance = rem_time_distance;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
