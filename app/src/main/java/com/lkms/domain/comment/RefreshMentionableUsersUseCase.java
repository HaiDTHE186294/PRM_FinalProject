package com.lkms.domain.comment;

import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

public class RefreshMentionableUsersUseCase {
    private final ICommentRepository repository;

    public RefreshMentionableUsersUseCase(ICommentRepository repository) {
        this.repository = repository;
    }

    public void execute(Integer targetId, LKMSConstantEnums.CommentType type, ICommentRepository.OnRefreshListener listener) {
        repository.refreshMentionableUsers(targetId, type, listener);
    }
}
