package com.lkms.ui.experimentdetail.adapter;

import com.lkms.data.model.java.LogEntry;
import java.util.List;

public class LogInsertWrapper {
    public final int adapterPosition;
    public final List<LogEntry> logs;

    public LogInsertWrapper(int adapterPosition, List<LogEntry> logs) {
        this.adapterPosition = adapterPosition;
        this.logs = logs;
    }

}
