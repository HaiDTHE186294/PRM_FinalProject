package com.lkms.ui.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments = new ArrayList<>();

    private final Map<String, String> userIdToNameMap = new HashMap<>();

    public void submitList(List<Comment> list) {
        comments.clear();
        if (list != null) comments.addAll(list);
        notifyDataSetChanged();
    }

    public void updateMentionableUsers(List<User> users) {
        userIdToNameMap.clear(); // Xóa map cũ
        if (users != null) {
            for (User user : users) {
                String id = String.valueOf(user.getUserId());
                String name = user.getName();

                if (!id.isEmpty() && name != null) {
                    userIdToNameMap.put(id, name);
                }
            }
        }
        // Sau khi có map tra cứu, ta phải refresh list để hiển thị tên
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.content.setText(comment.getCommentText());

        String userId = String.valueOf(comment.getUserId());
        String userName = userIdToNameMap.get(userId);
        if (userName == null) {
            userName = "Unknown User";
        }
        holder.userName.setText(userName);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        final TextView userName, content;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.text_comment_user);
            content = itemView.findViewById(R.id.text_comment_content);
        }
    }
}
