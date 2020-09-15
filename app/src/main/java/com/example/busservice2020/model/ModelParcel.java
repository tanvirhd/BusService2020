package com.example.busservice2020.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class ModelParcel implements Parcelable {
    private LatLng startlocation,destinationlication;
    private String userId,busId,destinationPlaceName;

    public ModelParcel() {
    }

    public ModelParcel(LatLng startlocation, LatLng destinationlication, String userId, String busId, String destinationPlaceName) {
        this.startlocation = startlocation;
        this.destinationlication = destinationlication;
        this.userId = userId;
        this.busId = busId;
        this.destinationPlaceName = destinationPlaceName;
    }

    protected ModelParcel(Parcel in) {
        startlocation = in.readParcelable(LatLng.class.getClassLoader());
        destinationlication = in.readParcelable(LatLng.class.getClassLoader());
        userId = in.readString();
        busId = in.readString();
        destinationPlaceName = in.readString();
    }

    public static final Creator<ModelParcel> CREATOR = new Creator<ModelParcel>() {
        @Override
        public ModelParcel createFromParcel(Parcel in) {
            return new ModelParcel(in);
        }

        @Override
        public ModelParcel[] newArray(int size) {
            return new ModelParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(startlocation, flags);
        dest.writeParcelable(destinationlication, flags);
        dest.writeString(userId);
        dest.writeString(busId);
        dest.writeString(destinationPlaceName);
    }

    public LatLng getStartlocation() {
        return startlocation;
    }

    public void setStartlocation(LatLng startlocation) {
        this.startlocation = startlocation;
    }

    public LatLng getDestinationlication() {
        return destinationlication;
    }

    public void setDestinationlication(LatLng destinationlication) {
        this.destinationlication = destinationlication;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getDestinationPlaceName() {
        return destinationPlaceName;
    }

    public void setDestinationPlaceName(String destinationPlaceName) {
        this.destinationPlaceName = destinationPlaceName;
    }

    public static Creator<ModelParcel> getCREATOR() {
        return CREATOR;
    }
}
