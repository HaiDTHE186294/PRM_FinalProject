package com.lkms.ui.viewlog; // (Hoặc package của ViewModel của bạn)

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.domain.logentry.GetLogUseCase;
import com.lkms.data.repository.IExperimentRepository; // Giả sử import
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava; // Giả sử import

/**
 * Factory này chịu trách nhiệm "dạy" ViewModelProvider cách
 * tạo ra một ViewLogDetailViewModel, vì nó cần một GetLogUseCase.
 */
public class ViewLogDetailViewModelFactory implements ViewModelProvider.Factory {

    private final IExperimentRepository repository;

    // Factory này sẽ nhận Repository làm tham số
    // (Đây là cách đơn giản để giải quyết dependency)
    public ViewLogDetailViewModelFactory(IExperimentRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked") // Cần thiết cho việc cast T
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Kiểm tra xem có đúng là ViewModel mà chúng ta muốn tạo không
        if (modelClass.isAssignableFrom(ViewLogDetailViewModel.class)) {

            // Nếu đúng, hãy tạo UseCase
            GetLogUseCase useCase = new GetLogUseCase(repository);

            // Và dùng UseCase đó để tạo ViewModel
            return (T) new ViewLogDetailViewModel(useCase);
        }
        // Nếu không, báo lỗi
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}