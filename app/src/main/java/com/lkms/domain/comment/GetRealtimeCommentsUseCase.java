package com.lkms.domain.comment;

import androidx.lifecycle.LiveData;

import com.lkms.data.model.java.Comment;
import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.util.List;

public class GetRealtimeCommentsUseCase {
    private final ICommentRepository repository;

    public GetRealtimeCommentsUseCase(ICommentRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Comment>> execute(Integer targetId, LKMSConstantEnums.CommentType type) {
        return repository.getRealtimeComments(targetId, type);
    }
}
