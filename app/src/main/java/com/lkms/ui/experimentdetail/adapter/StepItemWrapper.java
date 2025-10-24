package com.lkms.ui.experimentdetail.adapter;

import com.lkms.data.model.java.LogEntry;
import com.lkms.data.model.java.combine.ExperimentProtocolStep;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StepItemWrapper extends ExperimentProtocolStep implements AdapterItem {

    private boolean isExpanded;
    private List<LogEntry> downloadLog;

    public StepItemWrapper(ExperimentProtocolStep step) {
        super(step.getExperimentStep(), step.getProtocolStep());
        isExpanded = false;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }

    @Override
    public int getViewType() {
        return AdapterConstants.TYPE_STEP;
    }

}
