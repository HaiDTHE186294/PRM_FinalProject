package com.lkms.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts; // Thêm dự phòng

import com.lkms.data.model.java.combine.ExperimentReportData;
import com.lkms.data.model.java.combine.ReportLog;
import com.lkms.data.model.java.combine.ReportStep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfGenerator {

    private final Context context;
    private static final String TAG = "PdfGenerator";

    // *** SỬA LẠI: CHỈ CẦN 1 FONT ***
    private PdfFont fontVietnamese;

    public PdfGenerator(Context context) {
        this.context = context;
        try {
            // *** SỬA LẠI: NẠP FONT TỪ ĐƯỜNG DẪN MỚI CỦA NGÀI ***
            // 1. Đọc file font từ 'assets/Noto_Sans/...'
            byte[] fontBytes = loadFontFromAssets("fonts/Noto_Sans/NotoSans-VariableFont_wdth,wght.ttf");

            // 2. Tạo MỘT đối tượng font
            this.fontVietnamese = PdfFontFactory.createFont(fontBytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

        } catch (IOException e) {
            Log.e(TAG, "Lỗi nghiêm trọng khi tải font: " + e.getMessage());
            // (Nếu lỗi, dùng font dự phòng không dấu)
            try {
                this.fontVietnamese = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException ex) {
                // Không thể xảy ra với font tiêu chuẩn
            }
        }
    }

    // (Hàm loadFontFromAssets giữ nguyên)
    private byte[] loadFontFromAssets(String path) throws IOException {
        InputStream is = context.getAssets().open(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        return baos.toByteArray();
    }


    public void createPdfReport(ExperimentReportData data) {
        String fileName = "Experiment_Report_" + data.getExperimentId() + ".pdf";

        try {
            FileOutputStream fos = getFileOutputStream(fileName);
            if (fos == null) {
                showToast("Không thể tạo file PDF (không lấy được stream).");
                return;
            }

            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // --- BẮT ĐẦU VẼ ---

            // *** SỬA LẠI: DÙNG setFont VÀ setBold() ***
            document.add(new Paragraph(data.getProjectTitle())
                    .setFont(fontVietnamese).setFontSize(18).setBold()); // .setBold()
            document.add(new Paragraph("Trưởng dự án: " + data.getProjectLeaderName())
                    .setFont(fontVietnamese).setFontSize(12)); // (Mặc định là regular)

            drawDivider(document);

            document.add(new Paragraph(data.getExperimentTitle() + " (ID: " + data.getExperimentId() + ")")
                    .setFont(fontVietnamese).setFontSize(14).setBold()); // .setBold()
            document.add(new Paragraph("Mục tiêu: " + data.getObjective())
                    .setFont(fontVietnamese).setFontSize(12));
            document.add(new Paragraph("Người thực hiện: " + data.getCreatorName())
                    .setFont(fontVietnamese).setFontSize(12));
            document.add(new Paragraph("Bắt đầu: " + data.getStartDate() + " - Kết thúc: " + data.getFinishDate())
                    .setFont(fontVietnamese).setFontSize(12));

            drawDivider(document);

            document.add(new Paragraph("Quy trình: " + data.getProtocolTitle() + " (v" + data.getProtocolVersionNumber() + ")")
                    .setFont(fontVietnamese).setFontSize(14).setBold()); // .setBold()
            document.add(new Paragraph("Giới thiệu: " + data.getProtocolIntroduction())
                    .setFont(fontVietnamese).setFontSize(12));

            drawDivider(document);

            document.add(new Paragraph("CHI TIẾT THỰC HIỆN")
                    .setFont(fontVietnamese).setFontSize(18).setBold() // .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            if (data.getSteps() != null) {
                for (ReportStep step : data.getSteps()) {
                    Paragraph stepTitle = new Paragraph("Bước " + step.getStepOrder() + ": " + step.getInstruction())
                            .setFont(fontVietnamese).setFontSize(12).setBold() // .setBold()
                            .setMarginTop(10);
                    document.add(stepTitle);

                    if (step.getLogs() != null) {
                        for (ReportLog log : step.getLogs()) {
                            String logText = String.format("[%s] %s (%s): %s",
                                    log.getLogTime(), log.getUserName(), log.getLogType(), log.getContent());

                            Paragraph logP = new Paragraph(logText)
                                    .setFont(fontVietnamese).setFontSize(10).setMarginLeft(15);
                            document.add(logP);

                            if (log.getFileUrl() != null && !log.getFileUrl().isEmpty()) {
                                String url = log.getFileUrl();

                                PdfAction action = PdfAction.createURI(url);
                                Link link = new Link(url, action);

                                link.setFont(fontVietnamese); // Dùng font TV
                                link.setFontSize(9);
                                link.setFontColor(ColorConstants.BLUE);
                                link.setUnderline();

                                Paragraph linkP = new Paragraph("File: ")
                                        .setFont(fontVietnamese).setFontSize(9) // Dùng font TV
                                        .add(link)
                                        .setMarginLeft(15);

                                document.add(linkP);
                            }
                        }
                    }
                }
            }

            document.close();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo PDF: " + e.getMessage());
            e.printStackTrace();
            showToast("Lỗi khi tạo PDF: " + e.getMessage());
        }
    }

    // (Hàm này giữ nguyên)
    private void drawDivider(Document document) {
        SolidLine line = new SolidLine(1f);
        line.setColor(ColorConstants.BLACK);
        LineSeparator ls = new LineSeparator(line);
        ls.setMarginTop(10);
        ls.setMarginBottom(10);
        document.add(ls);
    }

    // (Hàm này giữ nguyên)
    private FileOutputStream getFileOutputStream(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                showToast("Đã lưu PDF tại: Thư mục Downloads");
                return (FileOutputStream) resolver.openOutputStream(uri);
            } else {
                return null;
            }
        } else {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            showToast("Đã lưu PDF tại: " + file.getAbsolutePath());
            return new FileOutputStream(file);
        }
    }

    private void showToast(String message) {
        ((android.app.Activity) context).runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
        });
    }
}