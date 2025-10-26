package com.lkms.ui.experimentdetail.adapter;

import com.lkms.data.model.java.LogEntry;


public class LogItemWrapper extends LogEntry implements AdapterItem {

    public LogItemWrapper(LogEntry entry) {
        setLogId(entry.getLogId());
        setExperimentStepId(getExperimentStepId());
        setLogType(entry.getLogType());
        setUserId(entry.getUserId());
        setContent(entry.getContent());
        setUrl(entry.getUrl());
        setLogType(entry.getLogTime());
    }
    @Override
    public int getViewType() {
        return AdapterConstants.TYPE_LOG;
    }
}
