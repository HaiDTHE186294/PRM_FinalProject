package com.lkms.ui.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.ui.user.view.RoleTag;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<User> memberList;

    public MemberAdapter(List<User> memberList) {
        this.memberList = memberList;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater  .from(parent.getContext())
                                    .inflate(R.layout.activity_member_list_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        User member = memberList.get(position);

        //User
        holder.user = member;

        //Name
        holder.nameTextView.setText(member.getName());

        //Role
        LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.values()[member.getRoleId()];
        holder.roleTagView.setRole(role);

    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    // ViewHolder class
    class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        RoleTag roleTagView;
        User user;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.item_member_name);
            roleTagView = itemView.findViewById(R.id.user_role);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Context context = v.getContext();

            Intent intent = new Intent(context, MemberDetailActvity.class);
            intent.putExtra("MemberId", user.getUserId());

            context.startActivity(intent);
        }

    }
}
