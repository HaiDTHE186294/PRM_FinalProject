package com.lkms.ui.addmember;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.IUserRepository;
import com.lkms.data.repository.ITeamRepository;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.data.repository.implement.java.TeamRepositoryImplJava;
import com.lkms.domain.AddMemberUseCase;
import com.lkms.domain.AddMemberToTeamUseCase; // Chúng ta sẽ tạo file này ngay sau đây

public class AddMemberViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddMemberViewModel.class)) {
            // Khởi tạo các Repository cần thiết
            IUserRepository userRepository = new UserRepositoryImplJava();
            ITeamRepository teamRepository = new TeamRepositoryImplJava();

            // Khởi tạo các UseCase
            AddMemberUseCase addMemberUseCase = new AddMemberUseCase(userRepository);
            AddMemberToTeamUseCase addMemberToTeamUseCase = new AddMemberToTeamUseCase(teamRepository);

            return (T) new AddMemberViewModel(addMemberUseCase, addMemberToTeamUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
    