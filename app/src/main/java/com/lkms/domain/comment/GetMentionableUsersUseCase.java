package com.lkms.domain.comment;

import androidx.lifecycle.LiveData;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.util.List;

public class GetMentionableUsersUseCase {
    private final ICommentRepository repository;

    public GetMentionableUsersUseCase(ICommentRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<User>> execute(Integer targetId, LKMSConstantEnums.CommentType type) {
        // Trả về LiveData mà repository đang giữ
        return repository.getMentionableUsers(targetId, type);
    }
}
