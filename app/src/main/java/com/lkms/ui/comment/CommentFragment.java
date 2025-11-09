package com.lkms.ui.comment;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linkedin.android.spyglass.mentions.Mentionable;
import com.linkedin.android.spyglass.tokenization.QueryToken;
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.linkedin.android.spyglass.mentions.MentionSpan;
import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.CommentRepositoryImplJava;
import com.lkms.domain.comment.GetMentionableUsersUseCase;
import com.lkms.domain.comment.GetRealtimeCommentsUseCase;
import com.lkms.domain.comment.PostCommentUseCase;
import com.lkms.domain.comment.RefreshMentionableUsersUseCase;
import com.lkms.domain.comment.UnsubscribeFromCommentsUseCase;


import java.util.ArrayList;
import java.util.List;

/**
 * CommentFragment: quản lý phần bình luận và tính năng @mention.
 * Tương thích với Spyglass 3.0.3
 */
public class CommentFragment extends Fragment implements
        MentionUserAdapter.OnUserClickListener,
        QueryTokenReceiver { // <-- BẮT BUỘC CHO Spyglass 3.x

    private static final String ARG_TARGET_ID = "ARG_TARGET_ID";
    private static final String ARG_COMMENT_TYPE = "ARG_COMMENT_TYPE";

    // UI
    private RecyclerView mCommentsRecyclerView;
    private MentionsEditText mCommentEditText;
    private ImageButton mSendButton;
    private RecyclerView mMentionsRecyclerView;

    // Adapters
    private CommentAdapter mCommentAdapter;
    private MentionUserAdapter mMentionAdapter;

    // Data
    private Integer mTargetId;
    private LKMSConstantEnums.CommentType mCommentType;
    private CommentViewModel mViewModel;

    private final List<User> localMentionableUsers = new ArrayList<>();

    public static CommentFragment newInstance(Integer targetId, LKMSConstantEnums.CommentType type) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TARGET_ID, targetId);
        args.putString(ARG_COMMENT_TYPE, type.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTargetId = getArguments().getInt(ARG_TARGET_ID);
            String typeString = getArguments().getString(ARG_COMMENT_TYPE);
            mCommentType = LKMSConstantEnums.CommentType.valueOf(typeString);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mCommentsRecyclerView = view.findViewById(R.id.recycler_view_comments);
        mCommentEditText = view.findViewById(R.id.edit_text_comment);
        mSendButton = view.findViewById(R.id.button_send_comment);
        mMentionsRecyclerView = view.findViewById(R.id.recycler_view_mentions);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CommentRepositoryImplJava commentRepo = new CommentRepositoryImplJava();
        GetMentionableUsersUseCase getUsersUC = new GetMentionableUsersUseCase(commentRepo);
        GetRealtimeCommentsUseCase getCommentsUC = new GetRealtimeCommentsUseCase(commentRepo);
        PostCommentUseCase postCommentUC = new PostCommentUseCase(commentRepo);
        RefreshMentionableUsersUseCase refreshUsersUC = new RefreshMentionableUsersUseCase(commentRepo);
        UnsubscribeFromCommentsUseCase unsubscribeUC = new UnsubscribeFromCommentsUseCase(commentRepo);

        Application application = requireActivity().getApplication();

        CommentViewModelFactory factory = new CommentViewModelFactory(
                application,
                getUsersUC,
                getCommentsUC,
                postCommentUC,
                refreshUsersUC,
                unsubscribeUC
        );

        mViewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);

        mCommentAdapter = new CommentAdapter();

        setupCommentsRecyclerView();
        setupMentionsRecyclerView();
        setupMentionsEditText();
        setupSendButton();

        mViewModel.loadInitialData(mTargetId, mCommentType);
        observeViewModel();
    }

    private void setupCommentsRecyclerView() {
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mCommentsRecyclerView.setAdapter(mCommentAdapter);
    }

    private void setupMentionsRecyclerView() {
        mMentionAdapter = new MentionUserAdapter(this);
        mMentionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMentionsRecyclerView.setAdapter(mMentionAdapter);
        mMentionsRecyclerView.setVisibility(View.GONE);
    }

    private void setupMentionsEditText() {

        // 2. Đặt ký tự kích hoạt @
        mCommentEditText.setTokenizer(new WordTokenizer());

        // 3. Set listener nhận QueryToken
        mCommentEditText.setQueryTokenReceiver(this);
    }

    @NonNull
    @Override
    public List<String> onQueryReceived(@NonNull QueryToken queryToken) {
        String keyword = queryToken.getKeywords().toLowerCase();

        List<User> filteredList = new ArrayList<>();
        for (User user : localMentionableUsers) {
            if (user.getName().toLowerCase().contains(keyword)) {
                filteredList.add(user);
            }
        }

        mMentionAdapter.submitList(filteredList);
        if (!filteredList.isEmpty()) {
            mMentionsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mMentionsRecyclerView.setVisibility(View.GONE);
        }

        // Trả về danh sách buckets để Spyglass nhận diện token hợp lệ
        List<String> buckets = new ArrayList<>();
        buckets.add("users"); // bucketName có thể là gì cũng được
        return buckets;
    }

    private void setupSendButton() {
        mSendButton.setOnClickListener(v -> {
            String commentText = mCommentEditText.getText().toString();

            // ✅ Lấy danh sách mention qua Spyglass
            List<User> mentionedUsers = new ArrayList<>();
            List<MentionSpan> spans = mCommentEditText.getMentionsText().getMentionSpans();


            for (MentionSpan span : spans) {
                // 2. Lấy Mentionable từ trong span
                Mentionable mention = span.getMention();

                if (mention instanceof UserMentionWrapper) {
                    mentionedUsers.add(((UserMentionWrapper) mention).getUser());
                }
            }

            mViewModel.sendComment(commentText, mentionedUsers);
        });
    }

    private void observeViewModel() {
        mViewModel.getComments().observe(getViewLifecycleOwner(), comments -> {

            mCommentAdapter.submitList(comments);

            if (comments != null && !comments.isEmpty()) {
                mCommentsRecyclerView.scrollToPosition(comments.size() - 1);
            }
        });

        mViewModel.getMentionableUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                localMentionableUsers.clear();
                localMentionableUsers.addAll(users);
                mCommentAdapter.updateMentionableUsers(users);
                mViewModel.updateLocalMentionableUsers(users);
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        mViewModel.getPostSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                mCommentEditText.setText("");
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        // Chèn mention qua wrapper
        mCommentEditText.insertMention(new UserMentionWrapper(user));
        mMentionsRecyclerView.setVisibility(View.GONE);
    }
}
