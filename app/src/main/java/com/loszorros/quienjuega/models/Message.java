package com.loszorros.quienjuega.models;

import java.util.Date;

public class Message {
    private String message;

    public Message() {
    }

    private String from;
    private Boolean typeAprooval;

    public Message(String message, String from, Boolean typeAprooval) {
        this.message = message;
        this.from = from;
        this.dob = new Date();
        this.typeAprooval = typeAprooval;
    }

    private Date dob;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Boolean getTypeAprooval() {
        return typeAprooval;
    }

    public void setTypeAprooval(Boolean typeAprooval) {
        this.typeAprooval = typeAprooval;
    }
}
