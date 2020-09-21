package com.example.busservice2020.model_distancematrix;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Distance {
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("value")
    @Expose
    private Integer value;


    public String getText() {
        return text;
    }

    public Integer getValue() {
        return value;
    }
}
