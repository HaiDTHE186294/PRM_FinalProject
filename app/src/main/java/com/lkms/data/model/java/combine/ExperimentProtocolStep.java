package com.lkms.data.model.java.combine;

import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.ProtocolStep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentProtocolStep {
    ExperimentStep experimentStep;
    ProtocolStep protocolStep;
}
