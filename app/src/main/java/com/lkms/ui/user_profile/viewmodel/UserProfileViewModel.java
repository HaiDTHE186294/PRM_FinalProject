package com.lkms.ui.user_profile.viewmodel;

//import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.data.model.java.User;

/*
A ViewModel is needed in order to preserve data when the application does
something that cause data to 'disappear' (Rotate screen for example).
If not using this, we'll have to load data again and again and potentially
cause god-knows-what error.
 */
public class UserProfileViewModel extends ViewModel {

    private final UserRepositoryImplJava userRepository;

    private final MutableLiveData<User> _user = new MutableLiveData<>();

    public UserProfileViewModel(UserRepositoryImplJava userRepository) {
        this.userRepository = userRepository;
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
