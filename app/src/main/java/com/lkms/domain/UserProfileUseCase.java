package com.lkms.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

public class UserProfileUseCase
{
    UserRepositoryImplJava userRepository;

    private final MutableLiveData<User> _user = new MutableLiveData<>();

    public UserProfileUseCase() {
        userRepository = new UserRepositoryImplJava();
    }

    public LiveData<User> getUser() {
        return _user;
    }

    /**
     * Load user data from the repository based on the provided user ID.
     *
     * @param userId The ID of the user to load.
     */
    public void loadUser(int userId)
    {
        // Check if the user data is already loaded
        User existUser = _user.getValue();
        if (existUser != null && existUser.getUserId() == userId)
            return;

        userRepository.getUserById(userId, new UserRepositoryImplJava.UserCallback() {
            @Override
            public void onSuccess(User user) {
                _user.postValue(user);
            }

            @Override
            public void onError(String errorMessage) {
                _user.postValue(null);
            }
        });
    }

    /**
     * Update user's name and contact info and save it to database
     *
     * @param newName The ID of the user to load.
     */
    public void updateUser(String newName, String newContactInfo)
    {
        User getUser = _user.getValue();
        if (getUser == null)
            return;

        //Update user's data inside this ViewModel
        getUser.setName(newName);
        getUser.setContactInfo(newContactInfo);
        _user.postValue(getUser);

        //Update user's data inside DB
        userRepository.updateUserProfile(
                getUser.getUserId(),
                newName,
                newContactInfo,
                new UserRepositoryImplJava.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        _user.postValue(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        _user.postValue(null);
                    }
                });
    }

    /**
     * Update user's role and save it to database
     *
     * @param newRole
     */
    public void updateUserRole(LKMSConstantEnums.UserRole newRole)
    {
        User getUser = _user.getValue();
        if (getUser == null)
            return;

        //Update user's data inside this ViewModel
        getUser.setRoleId(newRole.ordinal());
        _user.postValue(getUser);

        //Update user's data inside DB
        userRepository.updateUserRole(
                getUser.getUserId(),
                newRole.ordinal(),
                new UserRepositoryImplJava.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        _user.postValue(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        _user.postValue(null);
                    }
                });
    }

    public void reloadUser()
    {
        User getUser = _user.getValue();
        if (getUser == null)
            return;

        userRepository.getUserById(_user.getValue().getUserId(), new UserRepositoryImplJava.UserCallback() {
            @Override
            public void onSuccess(User user) {
                _user.postValue(user);
            }

            @Override
            public void onError(String errorMessage) {
                _user.postValue(null);
            }
        });
    }
}
