package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;

public class Booking {

    @SerializedName("bookingId")
    private Integer bookingId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("equipmentId")
    private Integer equipmentId;

    @SerializedName("experimentId")
    private Integer experimentId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("bookingStatus")
    private String bookingStatus;

    @SerializedName("rejectReason")
    private String rejectReason;

    public void Booking() {}

    public Booking(Integer bookingId, Integer userId, Integer equipmentId,
                   Integer experimentId, String startTime, String endTime,
                   String bookingStatus) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.experimentId = experimentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingStatus = bookingStatus;
    }

    // --- Getters & Setters ---
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public Integer getExperimentId() { return experimentId; }
    public void setExperimentId(Integer experimentId) { this.experimentId = experimentId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
