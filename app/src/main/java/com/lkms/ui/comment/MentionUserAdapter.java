package com.lkms.ui.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách user khi gõ @ để mention.
 * Dùng với Spyglass (3.x).
 */
public class MentionUserAdapter extends RecyclerView.Adapter<MentionUserAdapter.UserViewHolder> {

    private final List<User> users = new ArrayList<>();
    private final OnUserClickListener clickListener;

    // Interface callback
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public MentionUserAdapter(OnUserClickListener listener) {
        this.clickListener = listener;
    }

    // Cập nhật danh sách user
    public void submitList(List<User> newList) {
        users.clear();
        if (newList != null) {
            users.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mention_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_mention_name);
        }

        void bind(User user) {
            nameTextView.setText(user.getName());
        }
    }
}
