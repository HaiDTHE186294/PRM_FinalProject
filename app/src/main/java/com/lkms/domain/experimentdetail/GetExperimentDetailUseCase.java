package com.lkms.domain.experimentdetail;

import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.User;
import com.lkms.data.model.java.combine.ExperimentUserProjectProtocol;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.IUserRepository;

public class GetExperimentDetailUseCase {

    private final IExperimentRepository experimentRepository;
    private final IUserRepository userRepository;
    private final IProtocolRepository protocolRepository;

    public GetExperimentDetailUseCase(IExperimentRepository experimentRepository,
                                      IUserRepository userRepository,
                                      IProtocolRepository protocolRepository) {
        this.experimentRepository = experimentRepository;
        this.userRepository = userRepository;
        this.protocolRepository = protocolRepository;
    }

    public interface GetExperimentDetailCallback {
        void onSuccess(ExperimentUserProjectProtocol result);
        void onError(String error);
    }

    public void execute(int experimentId, final GetExperimentDetailCallback callback) {

        experimentRepository.getExperimentById(experimentId,
                new IExperimentRepository.ExperimentCallback() {

                    @Override
                    public void onSuccess(Experiment experiment) {

                        ExperimentUserProjectProtocol result =
                                new ExperimentUserProjectProtocol();
                        result.setExperiment(experiment);

                        final int[] counter = {0};
                        final StringBuilder errorHolder = new StringBuilder();

                        Runnable checkDone = () -> {
                            synchronized (counter) {
                                if (counter[0] == 3) {
                                    if (errorHolder.length() == 0) {
                                        callback.onSuccess(result);
                                    } else {
                                        callback.onError(errorHolder.toString().trim());
                                    }
                                }
                            }
                        };

                        // Request User info
                        userRepository.getUserById(experiment.getUserId(),
                                new IUserRepository.UserCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        result.setUser(user);
                                        counter[0]++;
                                        checkDone.run();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        errorHolder.append(error).append("\n");
                                        counter[0]++;
                                        checkDone.run();
                                    }
                                });

                        // Request Project
                        experimentRepository.getExperimentProject(experiment.getProjectId(),
                                new IExperimentRepository.ProjectCallBack() {
                                    @Override
                                    public void onSuccess(Project project) {
                                        result.setProject(project);
                                        counter[0]++;
                                        checkDone.run();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        errorHolder.append(error).append("\n");
                                        counter[0]++;
                                        checkDone.run();
                                    }
                                });

                        // Request Protocol
                        protocolRepository.getProtocolById(experiment.getProtocolId(),
                                new IProtocolRepository.ProtocolCallBack() {
                                    @Override
                                    public void onSuccess(Protocol protocol) {
                                        result.setProtocol(protocol);
                                        counter[0]++;
                                        checkDone.run();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        errorHolder.append(error).append("\n");
                                        counter[0]++;
                                        checkDone.run();
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
    }
}
