package com.lkms.ui.comment;

import android.app.Application; // 1. MEOW! IMPORT CÁI NÀY
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// (Import 5 UseCase của ngài như cũ)
import com.lkms.domain.comment.GetMentionableUsersUseCase;
import com.lkms.domain.comment.GetRealtimeCommentsUseCase;
import com.lkms.domain.comment.PostCommentUseCase;
import com.lkms.domain.comment.RefreshMentionableUsersUseCase;
import com.lkms.domain.comment.UnsubscribeFromCommentsUseCase;

public class CommentViewModelFactory implements ViewModelProvider.Factory {

    // 2. THÊM Application VÀO ĐÂY
    private final Application application;
    private final GetMentionableUsersUseCase getMentionableUsersUseCase;
    private final GetRealtimeCommentsUseCase getRealtimeCommentsUseCase;
    private final PostCommentUseCase postCommentUseCase;
    private final RefreshMentionableUsersUseCase refreshMentionableUsersUseCase;
    private final UnsubscribeFromCommentsUseCase unsubscribeFromCommentsUseCase;

    // 3. THÊM Application VÀO CONSTRUCTOR
    public CommentViewModelFactory(
            Application application, // <-- THÊM VÀO
            GetMentionableUsersUseCase getMentionableUsersUseCase,
            GetRealtimeCommentsUseCase getRealtimeCommentsUseCase,
            PostCommentUseCase postCommentUseCase,
            RefreshMentionableUsersUseCase refreshMentionableUsersUseCase,
            UnsubscribeFromCommentsUseCase unsubscribeFromCommentsUseCase
    ) {
        this.application = application; // <-- THÊM VÀO
        this.getMentionableUsersUseCase = getMentionableUsersUseCase;
        this.getRealtimeCommentsUseCase = getRealtimeCommentsUseCase;
        this.postCommentUseCase = postCommentUseCase;
        this.refreshMentionableUsersUseCase = refreshMentionableUsersUseCase;
        this.unsubscribeFromCommentsUseCase = unsubscribeFromCommentsUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommentViewModel.class)) {

            // 4. TRUYỀN Application VÀO CONSTRUCTOR CỦA VIEWMODEL
            return (T) new CommentViewModel(
                    application,
                    getRealtimeCommentsUseCase,
                    postCommentUseCase,
                    getMentionableUsersUseCase,
                    refreshMentionableUsersUseCase,
                    unsubscribeFromCommentsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}