package com.lkms.ui.viewlog;
// Giả định bạn đã import LiveData, ViewModel, ...
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.File;
import com.lkms.data.model.java.LogEntry;
import com.lkms.domain.logentry.GetLogUseCase;

import lombok.Getter;

public class ViewLogDetailViewModel extends ViewModel {

    private final GetLogUseCase getLogUseCase;

    // 1. Dùng LiveData để giữ trạng thái
    // Chúng ta tạo một class UiState để quản lý mọi trường hợp
    private final MutableLiveData<LogUiState> _uiState = new MutableLiveData<>();
    public LiveData<LogUiState> uiState = _uiState;

    // 5. Hàm để Fragment gọi khi nó cần đối tượng File thật
    // 2. Biến để giữ file, phòng trường hợp Fragment cần truy cập lại
    @Getter
    private File downloadedFile = null;

    // 3. Khởi tạo (ViewModel sẽ nhận UseCase qua Dependency Injection)
    public ViewLogDetailViewModel(GetLogUseCase getLogUseCase) {
        this.getLogUseCase = getLogUseCase;
    }

    // 4. Hàm để Activity gọi khi cần load data
    public void loadLogData(int logEntryId) {
        // Báo cho UI biết là đang loading
        _uiState.setValue(new LogUiState.Loading());

        getLogUseCase.execute(logEntryId, new GetLogUseCase.GetLogCallback() {
            @Override
            public void onSuccess(LogEntry logEntry, File file) {
                // Lưu file lại
                downloadedFile = file;

                // Báo cho UI biết là đã thành công
                // Gửi LogEntry, và chỉ gửi TÊN file (hoặc loại file) cho UI
                String fileName = (file != null) ? file.getName() : null;
                _uiState.postValue(new LogUiState.Success(logEntry, fileName));
            }

            @Override
            public void onError(String error) {
                _uiState.postValue(new LogUiState.Error(error));
            }
        });
    }

}

// 6. Lớp UiState để quản lý trạng thái (rất nên dùng)
abstract class LogUiState {
    static class Loading extends LogUiState {}
    static class Error extends LogUiState {
        String message;
        Error(String message) { this.message = message; }
    }
    static class Success extends LogUiState {
        LogEntry logEntry;
        String fileName; // Chỉ truyền thông tin "an toàn" cho UI
        Success(LogEntry logEntry, String fileName) {
            this.logEntry = logEntry;
            this.fileName = fileName;
        }
    }
}