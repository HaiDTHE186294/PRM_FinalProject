package com.lkms.data.model.java.combine;

import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentUserProjectProtocol {
    Experiment experiment;
    User user;
    Project project;
    Protocol protocol;
}
