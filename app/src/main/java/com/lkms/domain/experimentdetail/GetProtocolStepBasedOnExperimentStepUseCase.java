package com.lkms.domain.experimentdetail;

import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;

import java.util.List;

public class GetProtocolStepBasedOnExperimentStepUseCase {
    private final IProtocolRepository protocolRepository;

    public GetProtocolStepBasedOnExperimentStepUseCase (IProtocolRepository repo){
        this.protocolRepository = repo;
    }

    public interface getProtocolStepBasedOnExperimentStepCallback {
        void onSuccess(ProtocolStep protocolStep);
        void onError(String error);
    }

    public void execute(int protocolId, final getProtocolStepBasedOnExperimentStepCallback callback) {

        // Gọi phương thức của Repository (Data Layer)
        // Chúng ta tạo một Callback mới của Repository để lắng nghe kết quả từ Data Layer
        protocolRepository.getProtocolStep(protocolId, new IProtocolRepository.ProtocolStepCallback()
        {
            @Override
            public void onSuccess(ProtocolStep protocol) {
                // [Nơi xử lý logic nghiệp vụ - Tùy chọn]

                callback.onSuccess(protocol);
            }

            @Override
            public void onError(String error) {
                // Trả lỗi về cho ViewModel qua Callback của UseCase
                callback.onError(error);
            }
        });
    }
}
