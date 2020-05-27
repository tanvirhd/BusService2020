package com.example.busservice2020;

import android.app.Application;

import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.Teliver;

public class application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Teliver.init(this,"ff96673e68ef21aed136fe5d9d9c677d");
        //TLog.setVisible(true);
    }


}
