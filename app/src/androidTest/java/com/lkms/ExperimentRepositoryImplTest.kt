package com.lkms

import com.lkms.data.repository.IExperimentRepository
import com.lkms.data.repository.implement.ExperimentRepositoryImpl
import com.lkms.data.model.Experiment
import com.lkms.data.model.ExperimentStep
import com.lkms.data.model.LogEntry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.util.*
import androidx.test.platform.app.InstrumentationRegistry

class ExperimentRepositoryImplTest {

    private val repo = ExperimentRepositoryImpl()

    //To do: Create all test file to completed implemented method
    // ✅ 1. Test tạo mới Experiment
    @Test
    fun testCreateNewExperiment() = runBlocking {
        repo.createNewExperiment(
            title = "Mock Test",
            objective = "Mock Test",
            userId = 1,
            protocolId = 1,
            projectId = 1,
            callback = object : IExperimentRepository.IdCallback {
                override fun onSuccess(id: Int) {
                    println("✅ Created Experiment ID: $id")
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 2. Test lấy danh sách Experiment đang thực hiện (INPROCESS)
    @Test
    fun testGetOngoingExperiments() = runBlocking {
        repo.getOngoingExperiments(
            userId = 1,
            callback = object : IExperimentRepository.ExperimentListCallback {
                override fun onSuccess(experimentList: List<Experiment>) {
                    println("✅ Ongoing Experiments: ${experimentList.size}")
                    experimentList.forEach {
                        println(" - ${it.experimentTitle} (${it.experimentStatus})")
                    }
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 3. Test lấy danh sách Step theo Experiment
    @Test
    fun testGetExperimentStepsList() = runBlocking {
        val experimentId = 1
        repo.getExperimentStepsList(
            experimentId = experimentId,
            callback = object : IExperimentRepository.ExperimentStepListCallback {
                override fun onSuccess(stepList: List<ExperimentStep>) {
                    println("✅ Step count: ${stepList.size}")
                    stepList.forEach {
                        println(" - Step ID ${it.experimentStepId}, ProtocolStep: ${it.protocolStepId}, Status: ${it.stepStatus}")
                    }
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 4. Test lấy danh sách LogEntry theo Step
    @Test
    fun testGetExperimentLogEntries() = runBlocking {
        val stepId = 1
        repo.getExperimentLogEntries(
            experimentStepId = stepId,
            callback = object : IExperimentRepository.LogEntryListCallback {
                override fun onSuccess(logList: List<LogEntry>) {
                    println("✅ Log entries: ${logList.size}")
                    logList.forEach {
                        println(" - LogID: ${it.logId} | Type: ${it.logType} | Content: ${it.content}")
                    }
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 5. Test thêm text note (log type = Text)
    @Test
    fun testAddTextNote() = runBlocking {
        repo.addTextNote(
            experimentStepId = 1,
            userId = 1,
            content = "Checked sample A, stable at room temperature.",
            callback = object : IExperimentRepository.IdCallback {
                override fun onSuccess(id: Int) {
                    println("✅ Text note added with Log ID: $id")
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 6. Test upload file + thêm file entry vào log
    @Test
    fun testAddFileEntry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // ✅ 1. Tạo file mẫu trong thư mục hợp lệ
        val cacheFile = File(context.filesDir, "sample.txt")

        if (cacheFile.exists()) cacheFile.delete() // tránh lỗi cache cũ
        cacheFile.writeText("This is a sample log file for upload test.")

        // ✅ 2. Kiểm tra file tồn tại thật sự
        println("File path: ${cacheFile.absolutePath}, exists = ${cacheFile.exists()}, size = ${cacheFile.length()} bytes")

        // Bước 1: Upload file
        repo.uploadFileToStorage(
            file = cacheFile,
            callback = object : IExperimentRepository.StringCallback {
                override fun onSuccess(result: String) {
                    println("✅ Uploaded file URL: $result")

                    // Bước 2: Lưu log entry vào DB
                    repo.addFileEntry(
                        experimentStepId = 1,
                        userId = 1,
                        logType = "TXT",
                        content = "Uploaded supporting document.",
                        fileUrl = result,
                        callback = object : IExperimentRepository.IdCallback {
                            override fun onSuccess(id: Int) {
                                println("✅ File log created with ID: $id")
                            }

                            override fun onError(error: String) {
                                println("❌ Error saving log entry: $error")
                            }
                        }
                    )
                }

                override fun onError(error: String) {
                    println("❌ Error uploading file: $error")
                }
            }
        )
        Thread.sleep(5000)
    }

}
