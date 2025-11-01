package com.lkms.ui.project.peerreview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R; // Giả định
import com.lkms.data.model.java.PeerReview;
import java.util.ArrayList;
import java.util.List;

public class PeerReviewAdapter extends RecyclerView.Adapter<PeerReviewAdapter.ReviewViewHolder> {

    private List<PeerReview> reviews = new ArrayList<>();

    public void setReviews(List<PeerReview> newReviews) {
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_peer_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeerReviewDetail;
        TextView tvPeerReviewTime;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeerReviewDetail = itemView.findViewById(R.id.tvPeerReviewDetail);
            tvPeerReviewTime = itemView.findViewById(R.id.tvPeerReviewTime);
        }

        public void bind(PeerReview review) {
            tvPeerReviewDetail.setText(review.getDetail());
            tvPeerReviewDetail.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            tvPeerReviewTime.setText("Time: " + review.getStartTime() + " to "+ review.getEndTime());
        }
    }
}