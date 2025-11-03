package com.lkms.ui.project.projectmanage;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.IProjectRepository;
import com.lkms.data.repository.implement.java.ProjectRepositoryImplJava;
import com.lkms.domain.project.ProjectUserCase;

/**
 * Factory để tạo ProjectViewModel, cung cấp (inject) các dependency cần thiết.
 * Đây là cách làm đơn giản khi không dùng Hilt/Dagger.
 */
public class ProjectViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProjectViewModel.class)) {
            // Tạo các dependency từ tầng dữ liệu lên tầng domain
            IProjectRepository repository = new ProjectRepositoryImplJava();
            ProjectUserCase useCase = new ProjectUserCase(repository);

            // Tiêm useCase vào ViewModel
            return (T) new ProjectViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}