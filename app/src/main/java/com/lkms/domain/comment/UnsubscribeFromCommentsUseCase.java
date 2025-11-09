package com.lkms.domain.comment;

import com.lkms.data.repository.ICommentRepository;

public class UnsubscribeFromCommentsUseCase {
    private final ICommentRepository repository;

    public UnsubscribeFromCommentsUseCase(ICommentRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.unsubscribeFromComments();
    }
}
