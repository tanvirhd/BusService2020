package com.example.busservice2020.SentNotification;

public class NewNotificationData {
    private String title;
    private String message;

    public NewNotificationData(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public NewNotificationData(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
