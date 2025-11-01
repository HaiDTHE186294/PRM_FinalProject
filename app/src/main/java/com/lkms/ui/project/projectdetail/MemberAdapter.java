package com.lkms.ui.project.projectdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R; // Giả định
import com.lkms.data.model.java.User;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<User> members = new ArrayList<>();

    public void setMembers(List<User> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Chúng ta có thể dùng lại item_project.xml,
        // nhưng tốt hơn là tạo 1 file layout riêng (ví dụ: item_member.xml)
        // Ở đây tôi dùng tạm item_project cho đơn giản
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrimary;
        TextView tvSecondary;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ lại ID từ item_project.xml
            tvPrimary = itemView.findViewById(R.id.tvProjectTitle);
            tvSecondary = itemView.findViewById(R.id.tvProjectLeader);
        }

        public void bind(User user) {
            tvPrimary.setText(user.getName());
            tvSecondary.setText("Email: " + user.getEmail()); // Hiển thị email hoặc role
        }
    }
}