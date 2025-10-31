package com.lkms.data.model.java.combine;

import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentReport {
    private ExperimentStepList experimentDetail;
    private User user;
    private Project project;
    private Protocol protocol;
}
