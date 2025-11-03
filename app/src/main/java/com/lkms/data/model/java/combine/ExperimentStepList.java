package com.lkms.data.model.java.combine;

import com.lkms.data.model.java.Experiment;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentStepList {
    private Experiment experiment;
    private List<StepLogList> stepLogs;
}
