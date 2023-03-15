package com.app.edithormobile.model;

public class GPTModel {
    String message;
    String sender;

    public GPTModel(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public GPTModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
