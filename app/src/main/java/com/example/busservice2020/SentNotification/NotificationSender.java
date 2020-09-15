package com.example.busservice2020.SentNotification;

import com.teliver.sdk.models.NotificationData;

public class NotificationSender {
    public NewNotificationData data;
    public String to;

    public NotificationSender(NewNotificationData data, String to) {
        this.data = data;
        this.to = to;
    }

    public NotificationSender() {
    }
}
