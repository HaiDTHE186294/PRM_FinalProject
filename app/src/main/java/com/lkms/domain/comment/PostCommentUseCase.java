package com.lkms.domain.comment;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.ICommentRepository;

import java.util.List;

public class PostCommentUseCase {
    private final ICommentRepository repository;

    public PostCommentUseCase(ICommentRepository repository) {
        this.repository = repository;
    }

    public void execute(
            Comment newComment,
            String senderName,
            List<User> mentionedUsers,
            ICommentRepository.OnPostResultListener listener
    ) {
        repository.postComment(newComment, senderName, mentionedUsers, listener);
    }
}