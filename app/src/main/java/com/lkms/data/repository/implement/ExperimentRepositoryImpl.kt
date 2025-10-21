//package com.lkms.data.repository.implement
//
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.repository.IExperimentRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import io.github.jan.supabase.postgrest.postgrest
//import kotlinx.coroutines.launch
//import java.util.Date
//
//import com.lkms.data.model.kotlin.Experiment
//import com.lkms.data.model.kotlin.ExperimentStep
//import com.lkms.data.model.kotlin.LogEntry
//import com.lkms.data.repository.enumPackage.ExperimentStatus
//import io.github.jan.supabase.storage.storage
//import java.io.File
//
//
//class ExperimentRepositoryImpl : IExperimentRepository {
//
//    private val client = SupabaseClient.client
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    override fun createNewExperiment(
//        title: String?,
//        objective: String?,
//        userId: Int,
//        protocolId: Int,
//        projectId: Int,
//        callback: IExperimentRepository.IdCallback?
//    ) {
//        scope.launch {
//            try {
//                // ✅ Tạo object Experiment có @Serializable
//                val newExperiment = Experiment(
//                    experimentId = null, // Supabase auto-generate
//                    experimentTitle = title ?: "Untitled Experiment",
//                    objective = objective ?: "No objective provided",
//                    experimentStatus = ExperimentStatus.INPROCESS.toString(),
//                    userId = userId,
//                    protocolId = protocolId,
//                    projectId = projectId,
//                    startDate = Date().toString(),
//                    finishDate = null,
//                )
//
//                // ✅ Gửi request lên Supabase
//                val response = client.postgrest["Experiment"].insert(newExperiment) {select()}
//
//                // ✅ Giải mã response
//                val created = response.decodeSingleOrNull<Experiment>()
//
//                if (created != null && created.experimentId != null) {
//                    callback?.onSuccess(created.experimentId)
//                } else {
//                    callback?.onError("Failed to create experiment.")
//                }
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while creating experiment")
//            }
//        }
//    }
//
//    override fun getOngoingExperiments(
//        userId: Int,
//        callback: IExperimentRepository.ExperimentListCallback?
//    ) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Experiment"].select {
//                    filter {
//                        eq("userId", userId)
//                        eq("experimentStatus", ExperimentStatus.INPROCESS.toString())
//                    }
//                }
//                val experiments = response.decodeList<Experiment>()
//                callback?.onSuccess(experiments)
//
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while fetching experiments")
//            }
//        }
//    }
//
//
//
//    override fun getExperimentStepsList(
//        experimentId: Int,
//        callback: IExperimentRepository.ExperimentStepListCallback?
//    ) {
//        scope.launch {
//            try {
//                val response = client.postgrest["ExperimentStep"].select {
//                    filter {
//                        eq("experimentId", experimentId)
//                    }
//                }
//                val experimentsStep = response.decodeList<ExperimentStep>()
//                callback?.onSuccess(experimentsStep)
//
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while fetching experiments step")
//            }
//        }
//    }
//
//    override fun getExperimentLogEntries(
//        experimentStepId: Int,
//        callback: IExperimentRepository.LogEntryListCallback?
//    ) {
//        scope.launch {
//            try {
//                val response = client.postgrest["LogEntry"].select {
//                    filter {
//                        eq("experimentStepId", experimentStepId)
//                    }
//                }
//                val logEntries = response.decodeList<LogEntry>()
//                callback?.onSuccess(logEntries)
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while fetching log entries")
//            }
//        }
//    }
//
//    override fun addTextNote(
//        experimentStepId: Int,
//        userId: Int,
//        content: String?,
//        callback: IExperimentRepository.IdCallback?
//    ) {
//        scope.launch {
//            try {
//                // ✅ Tạo object Experiment có @Serializable
//                val newLogEntry = LogEntry(
//                    logId = null, // Supabase auto-generate
//                    experimentStepId = experimentStepId,
//                    "Text",
//                    userId = userId,
//                    content = content ?: "",
//                    url = null,
//                    logTime = Date().toString(),
//                )
//
//                // ✅ Gửi request lên Supabase
//                val response = client.postgrest["LogEntry"].insert(newLogEntry) {select()}
//
//                // ✅ Giải mã response
//                val created = response.decodeSingleOrNull<LogEntry>()
//
//                if (created != null && created.logId != null) {
//                    callback?.onSuccess(created.logId)
//                } else {
//                    callback?.onError("Failed to create log entry.")
//                }
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while creating log entry")
//            }
//        }
//    }
//
//    override fun addFileEntry(
//        experimentStepId: Int,
//        userId: Int,
//        logType: String?,
//        content: String?,
//        fileUrl: String?,
//        callback: IExperimentRepository.IdCallback?
//    ) {
//        scope.launch {
//            try {
//                // ✅ Tạo object Experiment có @Serializable
//                val newLogEntry = LogEntry(
//                    logId = null, // Supabase auto-generate
//                    experimentStepId = experimentStepId,
//                    logType = logType ?: "File",
//                    userId = userId,
//                    content = content ?: "",
//                    url = fileUrl,
//                    logTime = Date().toString(),
//                )
//
//                // ✅ Gửi request lên Supabase
//                val response = client.postgrest["LogEntry"].insert(newLogEntry) {select()}
//
//                // ✅ Giải mã response
//                val created = response.decodeSingleOrNull<LogEntry>()
//
//                if (created != null && created.logId != null) {
//                    callback?.onSuccess(created.logId)
//                } else {
//                    callback?.onError("Failed to create log entry.")
//                }
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while creating log entry")
//            }
//        }
//    }
//
//    override fun uploadFileToStorage(
//        file: File?,
//        callback: IExperimentRepository.StringCallback?
//    ) {
//        if (file == null) {
//            callback?.onError("File is null")
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val bucket = client.storage.from("ExperimentLog")
//                val path = "${System.currentTimeMillis()}_${file.name}"
//                val response = bucket.upload(path, file.readBytes())
//
//                val publicUrl = bucket.publicUrl(path)
//                callback?.onSuccess(publicUrl)
//
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while uploading file")
//            }
//        }
//    }
//
//    override fun requestExperimentReport(
//        experimentId: Int,
//        callback: IExperimentRepository.StringCallback?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun postComment(
//        experimentId: Int,
//        userId: Int,
//        commentText: String?,
//        callback: IExperimentRepository.GenericCallback?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getCommentsForExperiment(
//        experimentId: Int,
//        callback: IExperimentRepository.CommentListCallback?
//    ) {
//        TODO("Not yet implemented")
//    }
//}
//
