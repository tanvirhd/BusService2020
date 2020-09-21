package com.example.busservice2020.model_distancematrix;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DistanceResponse {


    @SerializedName("destinationAddresses")
    @Expose
    private List<String> destinationAddresses ;


    @SerializedName("originAddresses")
    @Expose
    private List<String> originAddresses ;


    @SerializedName("rows")
    @Expose
    private List<Row> rows;


    @SerializedName("status")
    @Expose
    private String status;


    public List<String> getDestinationAddresses() {
        return destinationAddresses;
    }

    public List<String> getOriginAddresses() {
        return originAddresses;
    }

    public List<Row> getRows() {
        return rows;
    }

    public String getStatus() {
        return status;
    }

}

