package com.lkms.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lkms.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "MENTION_CHANNEL";
    private static final String CHANNEL_NAME = "Mentions";
    private static final String CHANNEL_DESC = "Notifications for user mentions";

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    // 1. Tạo Channel (Bắt buộc cho Android 8.0+)
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // 2. Xây dựng và Gửi Thông Báo
    public void sendMentionNotification(String title, String content, int notificationId) {

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // ⚠️ MEOW! Ngài hãy đổi icon này!
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Tự động xóa khi người dùng nhấn

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Kiểm tra quyền cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Với mock-up, chúng ta chỉ ghi log
                Log.w("NotificationHelper", "Thiếu quyền POST_NOTIFICATIONS!");
                return;
            }
        }

        // Gửi đi!
        notificationManager.notify(notificationId, builder.build());
    }
}