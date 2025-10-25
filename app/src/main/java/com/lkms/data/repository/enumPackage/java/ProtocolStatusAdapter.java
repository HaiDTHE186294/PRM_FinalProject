package com.lkms.data.repository.enumPackage.java;


import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
// ✅ SỬA 1: Import Enum từ package chính xác của bạn
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;

import java.io.IOException;

/**
 * Lớp này giúp Gson chuyển đổi giữa String từ API và Enum ProtocolApproveStatus trong Java.
 */
public class ProtocolStatusAdapter extends TypeAdapter<ProtocolApproveStatus> {

    @Override
    public void write(JsonWriter out, ProtocolApproveStatus value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Chuyển Enum thành String khi gửi đi (ví dụ: PENDING -> "PENDING")
        out.value(value.name());
    }

    @Override
    public ProtocolApproveStatus read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            // Trả về PENDING hoặc một giá trị mặc định khác nếu API trả về null
            return ProtocolApproveStatus.PENDING;
        }
        String statusStr = in.nextString();
        try {
            // Chuyển String từ API về Enum (ví dụ: "APPROVED" -> ProtocolApproveStatus.APPROVED)
            return ProtocolApproveStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Nếu API trả về một giá trị lạ, ta sẽ coi nó là PENDING để an toàn
            return ProtocolApproveStatus.PENDING;
        }
    }
}

