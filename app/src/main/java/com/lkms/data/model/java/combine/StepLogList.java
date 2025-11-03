package com.lkms.data.model.java.combine;

import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.LogEntry;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepLogList {
    private ExperimentStep step;
    private List<LogEntry> logs;
}
