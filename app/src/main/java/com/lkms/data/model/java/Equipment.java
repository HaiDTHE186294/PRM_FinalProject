package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;

public class Equipment {

    @SerializedName("equipmentId")
    private Integer equipmentId;

    @SerializedName("equipmentName")
    private String equipmentName;

    @SerializedName("model")
    private String model;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("availability")
    private Boolean availability;

    public void EquipmentJava() {}

    public Integer getEquipmentId() { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public Boolean getAvailability() { return availability; }

    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public void setModel(String model) { this.model = model; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setAvailability(Boolean availability) { this.availability = availability; }
}
