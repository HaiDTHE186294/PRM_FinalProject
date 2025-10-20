package com.lkms.data.model.java;


import com.google.gson.annotations.SerializedName;

public class UserManual {

    @SerializedName("manualId")
    private String manualId;

    @SerializedName("url")
    private String url;

    public void UserManual() {}

    // --- Getters & Setters ---
    public String getManualId() { return manualId; }
    public void setManualId(String manualId) { this.manualId = manualId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
