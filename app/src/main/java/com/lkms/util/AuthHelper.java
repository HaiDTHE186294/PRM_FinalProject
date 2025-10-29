// Đặt tại: com/lkms/util/AuthHelper.java
package com.lkms.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Lớp tiện ích (Utility) để ĐỌC thông tin xác thực (Auth) của người dùng
 * đã được lưu trên thiết bị sau khi đăng nhập thành công.
 */
public class AuthHelper {

    // Các hằng số này được lấy từ code đăng nhập để đảm bảo đọc đúng file và đúng key
    private static final String PREFERENCE_FILE_NAME = "secure_prefs";
    private static final String KEY_USER_ID = "user_id";

    /**
     * Phương thức nội bộ để mở file SharedPreferences đã được mã hóa.
     * Cần các thiết lập y hệt như lúc lưu để có thể giải mã.
     */
    private static SharedPreferences getEncryptedSharedPreferences(Context context)
            throws GeneralSecurityException, IOException {

        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREFERENCE_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Lấy ID của người dùng đang đăng nhập.
     * Đây là hàm chính mà ViewModel của bạn sẽ gọi tới.
     *
     * @param context Context của ứng dụng.
     * @return ID của người dùng, hoặc -1 nếu không tìm thấy hoặc có lỗi.
     */
    public static int getLoggedInUserId(Context context) {
        try {
            SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
            // Đọc giá trị số nguyên từ key "user_id", nếu không có thì trả về giá trị mặc định là -1
            return sharedPreferences.getInt(KEY_USER_ID, -1);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Trong trường hợp có lỗi (không giải mã được, file bị lỗi...),
            // trả về -1 để tránh làm ứng dụng bị crash.
            return -1;
        }
    }
}
