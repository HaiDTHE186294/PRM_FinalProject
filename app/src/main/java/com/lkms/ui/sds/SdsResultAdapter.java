package com.lkms.ui.sds;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Item;

import java.util.List;

public class SdsResultAdapter extends RecyclerView.Adapter<SdsResultAdapter.SdsVH> {

    private List<Item> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onSdsClick(Item item);
    }

    public SdsResultAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SdsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sds_result, parent, false);
        return new SdsVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SdsVH holder, int position) {
        Item item = items.get(position);
        holder.tvName.setText(item.getItemName());
        holder.tvCas.setText(item.getCasNumber());

        holder.btnViewSds.setOnClickListener(v -> listener.onSdsClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class SdsVH extends RecyclerView.ViewHolder {
        TextView tvName, tvCas;
        ImageView btnViewSds;

        public SdsVH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCas = itemView.findViewById(R.id.tvCas);
            btnViewSds = itemView.findViewById(R.id.btnOpenSds);
        }
    }
}
