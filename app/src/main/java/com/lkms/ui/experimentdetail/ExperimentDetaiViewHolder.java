package com.lkms.ui.experimentdetail;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

public class ExperimentDetaiViewHolder extends RecyclerView.ViewHolder {
    public ExperimentDetaiViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    private final MutableLiveData<String> placeholder_currentName = new MutableLiveData<>();
}
