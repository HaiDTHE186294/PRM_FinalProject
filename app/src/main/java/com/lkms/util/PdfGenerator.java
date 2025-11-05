package com.lkms.util; // (Hoặc package của ngài)

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment; // Cần cho việc lưu file
import android.provider.MediaStore;
import android.text.StaticLayout; // Cần cho text ngắt dòng
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import com.lkms.data.model.java.combine.ExperimentReportData;
import com.lkms.data.model.java.combine.ReportLog;
import com.lkms.data.model.java.combine.ReportStep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    private final Context context;
    private static final String TAG = "PdfGenerator";

    // Hằng số cho layout
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_RIGHT = 40;
    private static final int MARGIN_TOP = 50;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    // Biến theo dõi vị trí vẽ
    private int currentY;

    // Các loại bút vẽ
    private TextPaint titlePaint;
    private TextPaint headerPaint;
    private TextPaint bodyPaint;
    private TextPaint smallPaint;

    public PdfGenerator(Context context) {
        this.context = context;
        initPaints();
    }

    // Khởi tạo các loại bút vẽ (Paint)
    private void initPaints() {
        titlePaint = new TextPaint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(18f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        headerPaint = new TextPaint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(14f);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        bodyPaint = new TextPaint();
        bodyPaint.setColor(Color.BLACK);
        bodyPaint.setTextSize(12f);

        smallPaint = new TextPaint();
        smallPaint.setColor(Color.DKGRAY);
        smallPaint.setTextSize(10f);
    }

    // Phương thức chính mà Activity sẽ gọi
    public void createPdfReport(ExperimentReportData data) {
        // 1. Khởi tạo tài liệu
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        currentY = MARGIN_TOP; // Reset vị trí Y

        try {
            // --- BẮT ĐẦU VẼ ---

            // Thông tin Project
            drawMultiLineText(canvas, data.getProjectTitle(), titlePaint);
            currentY += 10;
            drawMultiLineText(canvas, "Trưởng dự án: " + data.getProjectLeaderName(), bodyPaint);

            drawDivider(canvas);

            // Thông tin Experiment
            drawMultiLineText(canvas, data.getExperimentTitle() + " (ID: " + data.getExperimentId() + ")", headerPaint);
            currentY += 10;
            drawMultiLineText(canvas, "Mục tiêu: " + data.getObjective(), bodyPaint);
            drawMultiLineText(canvas, "Người thực hiện: " + data.getCreatorName(), bodyPaint);
            drawMultiLineText(canvas, "Bắt đầu: " + data.getStartDate() + " - Kết thúc: " + data.getFinishDate(), bodyPaint);

            drawDivider(canvas);

            // Thông tin Protocol
            drawMultiLineText(canvas, "Quy trình: " + data.getProtocolTitle() + " (v" + data.getProtocolVersionNumber() + ")", headerPaint);
            currentY += 10;
            drawMultiLineText(canvas, "Giới thiệu: " + data.getProtocolIntroduction(), bodyPaint);

            drawDivider(canvas);

            // Chi tiết các bước
            drawMultiLineText(canvas, "CHI TIẾT THỰC HIỆN", titlePaint);
            currentY += 10;

            if (data.getSteps() != null) {
                for (ReportStep step : data.getSteps()) {
                    // (Kiểm tra tràn trang - sẽ làm sau)

                    String stepTitle = "Bước " + step.getStepOrder() + ": " + step.getInstruction();
                    drawMultiLineText(canvas, stepTitle, headerPaint);

                    if (step.getLogs() != null) {
                        for (ReportLog log : step.getLogs()) {
                            String logText = String.format("[%s] %s (%s): %s",
                                    log.getLogTime(), log.getUserName(), log.getLogType(), log.getContent());
                            drawMultiLineText(canvas, logText, bodyPaint, MARGIN_LEFT + 15); // Thụt lề cho log

                            // *** NƠI XỬ LÝ FILE ĐÍNH KÈM (BƯỚC 6) SẼ Ở ĐÂY ***
                            if (log.getFileUrl() != null && !log.getFileUrl().isEmpty()) {
                                drawMultiLineText(canvas, "File: " + log.getFileUrl(), smallPaint, MARGIN_LEFT + 15);

                                // (Tạm thời chỉ hiển thị URL)

                                currentY += 5;
                            }
                        }
                    }
                    currentY += 10; // Khoảng cách giữa các bước
                }
            }

            // --- KẾT THÚC VẼ ---

            // 2. Hoàn tất trang
            document.finishPage(page);

            // 3. Lưu file (Bước 7)
            savePdf(document, "Experiment_Report_" + data.getExperimentId() + ".pdf");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo PDF: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            // 4. Đóng tài liệu
            if (document != null) {
                document.close();
            }
        }
    }

    // Hàm tiện ích để vẽ text và tự động ngắt dòng
    private void drawMultiLineText(Canvas canvas, String text, TextPaint paint) {
        drawMultiLineText(canvas, text, paint, MARGIN_LEFT); // Mặc định không thụt lề
    }

    private void drawMultiLineText(Canvas canvas, String text, TextPaint paint, int leftMargin) {
        if (text == null) text = "N/A";

        StaticLayout staticLayout = StaticLayout.Builder.obtain(
                        text, 0, text.length(), paint, CONTENT_WIDTH - (leftMargin - MARGIN_LEFT))
                .setAlignment(StaticLayout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f) // Tăng khoảng cách dòng
                .setIncludePad(false)
                .build();

        canvas.save();
        canvas.translate(leftMargin, currentY); // Di chuyển canvas đến vị trí
        staticLayout.draw(canvas);
        canvas.restore();

        currentY += staticLayout.getHeight() + 5; // Cập nhật vị trí Y
    }

    // Hàm vẽ đường kẻ ngang
    private void drawDivider(Canvas canvas) {
        currentY += 10;
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, bodyPaint);
        currentY += 20;
    }

    // Bước 7: Lưu file PDF
    // Bên trong lớp PdfGenerator

    private void savePdf(PdfDocument document, String fileName) throws IOException {
        // Kiểm tra phiên bản SDK của thiết bị
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                FileOutputStream fos = (FileOutputStream) resolver.openOutputStream(uri);
                document.writeTo(fos);
                fos.close();

                showToast("Đã lưu PDF tại: Thư mục Downloads");
            }
        } else {
            // --- CÁCH CŨ (Cho Android 9 trở xuống) ---
            // (Dùng FileOutputStream, yêu cầu quyền WRITE_EXTERNAL_STORAGE)

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();

            showToast("Đã lưu PDF tại: " + file.getAbsolutePath());
        }
    }

    // Hàm helper để hiện Toast trên Main Thread
    private void showToast(String message) {
        ((android.app.Activity) context).runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
        });
    }
}