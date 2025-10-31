package com.lkms.ui.loginmaindashboard.maindashboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Booking;
import com.lkms.data.model.java.BookingDisplay;
import com.lkms.ui.equipment.EquipmentDetailActivity;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<BookingDisplay> bookingList;
    private final Context context;

    public BookingAdapter(Context context, List<BookingDisplay> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingDisplay booking = bookingList.get(position);
        holder.tvBookingName.setText(booking.getEquipmentName());
        holder.tvBookingTime.setText(booking.getStartTime() + " - " + booking.getEndTime());

        // 🟢 Thêm sự kiện click cho mỗi booking item
        holder.itemView.setOnClickListener(v -> {
            int equipmentId = booking.getEquipmentId();

            Log.d("BOOKING_CLICK", "Clicked Equipment ID: " + equipmentId);

            if (equipmentId != -1) {
                Intent intent = new Intent(context, EquipmentDetailActivity.class);
                intent.putExtra("equipmentId", equipmentId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Equipment ID không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingName, tvBookingTime;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingName = itemView.findViewById(R.id.tvBookingName);
            tvBookingTime = itemView.findViewById(R.id.tvBookingTime);
        }
    }
}
