package com.lkms.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.InventoryTransaction;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of inventory transactions.
 */
public class InventoryTransactionAdapter extends RecyclerView.Adapter<InventoryTransactionAdapter.TransactionViewHolder> {

    private final List<InventoryTransaction> transactionList;

    /**
     * Constructor for the InventoryTransactionAdapter.
     * @param transactionList The list of transactions to display.
     */
    public InventoryTransactionAdapter(List<InventoryTransaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item of the RecyclerView
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        // Get the transaction at the current position
        InventoryTransaction transaction = transactionList.get(position);

        // Bind the transaction data to the views in the ViewHolder
        holder.transactionTypeTextView.setText(transaction.getTransactionType());
        holder.quantityTextView.setText("Quantity changed: " + transaction.getQuantity());
        holder.dateTextView.setText(transaction.getTransactionTime());
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return transactionList == null ? 0 : transactionList.size();
    }

    /**
     * ViewHolder class for the transaction item view.
     * It holds the references to the UI components for each item.
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView transactionTypeTextView;
        public TextView quantityTextView;
        public TextView dateTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views from the layout file
            // Note: Make sure the IDs in item_transaction_history.xml match these.
            transactionTypeTextView = itemView.findViewById(R.id.tv_transaction_type);
            quantityTextView = itemView.findViewById(R.id.tv_transaction_date);
            dateTextView = itemView.findViewById(R.id.tv_quantity);
        }
    }
}
