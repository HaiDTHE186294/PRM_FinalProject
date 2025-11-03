// Đặt trong package: com.lkms.ui.addlog
package com.lkms.ui.addlog;

import android.app.Application; // Sử dụng Application context
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.domain.logentry.AddLogUseCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Setter;

public class AddLogViewModel extends AndroidViewModel {

    private static final String TAG = "AddLogViewModel";

    private final AddLogUseCase addLogUseCase;
    private SharedPreferences sharedPreferences;
    private static final String KEY_USER_ID = "user_id";
    private final ExecutorService backgroundExecutor;

    // Activity gọi hàm này để thiết lập stepId ban đầu
    // == Trạng thái (State) ==
    @Setter
    private int stepId = -1;

    // LiveData cho file đã chọn
    private final MutableLiveData<File> _selectedFile = new MutableLiveData<>(null);
    public LiveData<File> getSelectedFile() { return _selectedFile; }

    // LiveData cho trạng thái upload
    private final MutableLiveData<UploadState> _uploadState = new MutableLiveData<>(new UploadState.Idle());
    public LiveData<UploadState> getUploadState() { return _uploadState; }


    public AddLogViewModel(
            Application application,
            AddLogUseCase addLogUseCase
    ) {
        super(application);
        this.addLogUseCase = addLogUseCase;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    // Hàm này được gọi khi ViewModel bị hủy
    @Override
    protected void onCleared() {
        super.onCleared();
        // Dọn dẹp luồng nền
        backgroundExecutor.shutdown();
    }

    /**
     * Xử lý logic khi người dùng chọn một file từ trình chọn file.
     * @param uri Uri của file được chọn.
     */
    public void onFileSelected(Uri uri) {
        _uploadState.setValue(new UploadState.Loading()); // Hiển thị loading khi đang sao chép file

        backgroundExecutor.execute(() -> {
            String fileName = getFileName(uri);
            File cacheFile = new File(getApplication().getCacheDir(), fileName);

            try (InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(cacheFile)) {

                if (inputStream == null) {
                    throw new IOException("Unable to open input stream for URI");
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // Cập nhật LiveData trên Main Thread
                _selectedFile.postValue(cacheFile);
                _uploadState.postValue(new UploadState.Idle()); // Sẵn sàng để upload

            } catch (IOException e) {
                Log.e(TAG, "Failed to copy file from URI", e);
                _selectedFile.postValue(null);
                _uploadState.postValue(new UploadState.Error("Failed to attach file."));
            }
        });
    }

    /**
     * Xử lý logic khi người dùng nhấn nút Upload.
     * @param content Nội dung từ EditText.
     */
    public void onUploadClicked(String content) {
        if (content.isEmpty()) {
            _uploadState.setValue(new UploadState.Error("Content is required"));
            return;
        }

        if (stepId == -1) {
            _uploadState.setValue(new UploadState.Error("Invalid Step ID"));
            return;
        }

        int userId = getUserIdFromSession();
        if (userId == -1) {
            _uploadState.setValue(new UploadState.Error("User not logged in"));
            return;
        }

        // Đặt trạng thái Loading
        _uploadState.setValue(new UploadState.Loading());

        File fileToUpload = _selectedFile.getValue();

        // Gọi UseCase
        addLogUseCase.execute(stepId, userId, content, fileToUpload, new AddLogUseCase.AddLogCallback() {
            @Override
            public void onSuccess(int newLogEntryId) {
                // Cập nhật LiveData (đã ở trên Main Thread nếu UseCase
                // không đổi luồng, nhưng postValue() an toàn hơn)
                _uploadState.postValue(new UploadState.Success(newLogEntryId));
            }

            @Override
            public void onError(String error) {
                _uploadState.postValue(new UploadState.Error(error));
            }
        });
    }

    /**
     * Đặt lại trạng thái về Idle, dùng sau khi hiển thị lỗi.
     */
    public void resetStateToIdle() {
        _uploadState.setValue(new UploadState.Idle());
    }

    /**
     * Lấy tên file gốc từ một Content URI (logic y hệt như trước).
     */
    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getApplication().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
            if (fileName == null) {
                fileName = "attached_file"; // Tên dự phòng
            }
        }
        return fileName;
    }

    private int getUserIdFromSession() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplication().getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplication().getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            return sharedPreferences.getInt("user_id", -1); // -1 nếu chưa lưu
        } catch (Exception e) {
            Log.d("get userId failed:", e.toString() );
            return -1;
        }
    }
}