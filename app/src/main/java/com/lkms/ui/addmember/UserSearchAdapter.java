package com.lkms.ui.addmember;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.User;

import java.util.ArrayList;
import java.util.HashSet; // ⭐ THÊM NÀY
import java.util.List;
import java.util.Set;    // ⭐ THÊM NÀY

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private OnUserClickListener listener;

    // ⭐ THÊM 1: Một Set để lưu trữ các item đang được chọn ⭐
    private final Set<User> selectedUsers = new HashSet<>();

    public interface OnUserClickListener {
        void onUserClick(User user, boolean isSelected); // Sửa lại để trả về trạng thái
    }

    public UserSearchAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search_result, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // ⭐ THÊM 2: Kiểm tra xem user hiện tại có trong danh sách chọn không ⭐
        boolean isSelected = selectedUsers.contains(user);

        // Truyền cả user và trạng thái `isSelected` vào hàm bind
        holder.bind(user, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUsers(List<User> users) {
        this.userList.clear();
        this.userList.addAll(users);
        // Quan trọng: Xóa danh sách chọn cũ khi có kết quả tìm kiếm mới
        this.selectedUsers.clear();
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvUserEmail;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        }

        // ⭐ THÊM 3: Hàm bind bây giờ nhận cả trạng thái isSelected ⭐
        public void bind(final User user, final boolean isSelected, final OnUserClickListener listener) {
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());

            // ⭐ THAY ĐỔI QUAN TRỌNG: Cập nhật trạng thái của itemView ⭐
            // Lệnh này sẽ kích hoạt selector mà chúng ta đã tạo ở Bước 1
            itemView.setSelected(isSelected);

            itemView.setOnClickListener(v -> {
                // Khi click, đảo ngược trạng thái của item và báo cho listener
                // Listener (chính là Activity) sẽ cập nhật lại adapter
                listener.onUserClick(user, !itemView.isSelected());
            });
        }
    }
    public List<User> getUserList() {
        return userList;
    }

    /**
     * ⭐ HÀM CÒN THIẾU ⭐
     * Cập nhật trạng thái lựa chọn (chọn/bỏ chọn) cho một user
     * trong danh sách selectedUsers nội bộ của Adapter.
     * @param user User vừa được click.
     */
    public void toggleSelection(User user) {
        if (selectedUsers.contains(user)) {
            selectedUsers.remove(user);
        } else {
            selectedUsers.add(user);
        }
    }
}
