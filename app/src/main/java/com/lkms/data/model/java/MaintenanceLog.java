package com.lkms.data.model.java;
import com.google.gson.annotations.SerializedName;

    public class MaintenanceLog {

        @SerializedName("maintenanceId")
        private Integer maintenanceId;

        @SerializedName("userId")
        private Integer userId;

        @SerializedName("equipmentId")
        private Integer equipmentId;

        @SerializedName("maintenanceTime")
        private String maintenanceTime;

        @SerializedName("maintenanceType")
        private String maintenanceType;

        @SerializedName("detail")
        private String detail;

        public void MaintenanceLog() {}

        // --- Getters & Setters ---
        public Integer getMaintenanceId() { return maintenanceId; }
        public void setMaintenanceId(Integer maintenanceId) { this.maintenanceId = maintenanceId; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public Integer getEquipmentId() { return equipmentId; }
        public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

        public String getMaintenanceTime() { return maintenanceTime; }
        public void setMaintenanceTime(String maintenanceTime) { this.maintenanceTime = maintenanceTime; }

        public String getMaintenanceType() { return maintenanceType; }
        public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
