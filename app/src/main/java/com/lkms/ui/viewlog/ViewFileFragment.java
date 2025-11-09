package com.lkms.ui.viewlog;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap; // MỚI
import android.graphics.BitmapFactory; // MỚI
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button; // MỚI
import android.widget.LinearLayout; // MỚI

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.github.barteksc.pdfviewer.PDFView;
import com.lkms.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ViewFileFragment extends Fragment {

    // Khai báo các View
    private PDFView pdfView;
    private ScrollView scrollViewText;
    private TextView textView;
    private ProgressBar progressBar;
    private ImageView imageView; // MỚI
    private WebView webView; // MỚI
    private LinearLayout layoutUnsupportedFile;
    private Button buttonOpenFileExternally;
    private TextView textViewUnsupportedInfo;

    private ViewLogDetailViewModel sharedViewModel;
    private File fileToDisplay; // Sẽ lấy từ ViewModel

    // Biến để nhận đường dẫn file
    private String filePath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // MỚI: Thêm onCreate
        super.onCreate(savedInstanceState);

        // Lấy Shared ViewModel được sở hữu bởi Activity cha
        // Đây là bước quan trọng nhất
        if (getActivity() != null) {
            sharedViewModel = new ViewModelProvider(requireActivity()).get(ViewLogDetailViewModel.class);
        } else {
            // Trường hợp hiếm gặp Fragment bị attach/detach lạ
            Toast.makeText(getContext(), "Lỗi nghiêm trọng: Không thể lấy ViewModel", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_file, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        pdfView = view.findViewById(R.id.pdfView);
        scrollViewText = view.findViewById(R.id.scrollViewText);
        textView = view.findViewById(R.id.textView);
        progressBar = view.findViewById(R.id.progressBar);
        imageView = view.findViewById(R.id.imageView); // MỚI
        webView = view.findViewById(R.id.webView); // MỚI
        layoutUnsupportedFile = view.findViewById(R.id.layoutUnsupportedFile);
        buttonOpenFileExternally = view.findViewById(R.id.buttonOpenFileExternally);
        textViewUnsupportedInfo = view.findViewById(R.id.textViewUnsupportedInfo); // (Optional)


        // SỬA: Lấy file từ ViewModel thay vì Arguments
        if (sharedViewModel != null) {
            this.fileToDisplay = sharedViewModel.getDownloadedFile();
            loadFile(); // Gọi hàm loadFile
        } else {
            // Hiển thị lỗi nếu không có ViewModel
            displayUnsupportedFileView(null);
            textViewUnsupportedInfo.setText("Lỗi: Không thể khởi tạo ViewModel.");
        }

        buttonOpenFileExternally.setOnClickListener(v -> {
            // Dùng biến fileToDisplay của Fragment
            if (fileToDisplay != null && fileToDisplay.exists()) {
                openFileExternally(fileToDisplay);
            } else {
                Toast.makeText(getContext(), "Lỗi: Không tìm thấy file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFile() {
        if (fileToDisplay == null || !fileToDisplay.exists()) {
            Toast.makeText(getContext(), "File không hợp lệ hoặc đã bị xóa khỏi cache", Toast.LENGTH_SHORT).show();
            displayUnsupportedFileView(null); // Hiển thị màn hình lỗi
            return;
        }

        String filePath = fileToDisplay.getAbsolutePath();
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath).toLowerCase();

        // ✅ Logic cốt lõi được cập nhật
        if (extension.equals("pdf")) {
            displayPdf(fileToDisplay);
        } else if (extension.equals("txt") || extension.equals("log")) {
            displayTxt(fileToDisplay);
        } else if (isImageExtension(extension)) {
            displayImage(fileToDisplay);
        } else if (extension.equals("html") || extension.equals("htm")) {
            displayWebView(fileToDisplay);
        } else {
            displayUnsupportedFileView(fileToDisplay);
        }
    }

    // Hàm kiểm tra đuôi file ảnh (MỚI)
    private boolean isImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("bmp") ||
                extension.equals("gif") || extension.equals("webp");
    }

    private void displayPdf(File file) {
        progressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.VISIBLE); // Hiển thị
        scrollViewText.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE); // CẬP NHẬT
        webView.setVisibility(View.GONE); // CẬP NHẬT

        pdfView.fromFile(file)
                .onLoad(numPages -> progressBar.setVisibility(View.GONE)) // Ẩn loading khi load xong
                .onError(t -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi khi mở file PDF", Toast.LENGTH_SHORT).show();
                })
                .load();
    }

    private void displayTxt(File file) {
        progressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.VISIBLE); // Hiển thị
        imageView.setVisibility(View.GONE); // CẬP NHẬT
        webView.setVisibility(View.GONE); // CẬP NHẬT

        // Đọc file trong một thread khác để không block UI
        new Thread(() -> {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                Log.e("Display Error","Read File Error");
            }

            // Cập nhật UI trên main thread
            String finalText = text.toString();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    textView.setText(finalText);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    // Hàm hiển thị ảnh (MỚI)
    private void displayImage(File file) {
        progressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE); // Hiển thị
        webView.setVisibility(View.GONE);

        // Load ảnh trong thread nền
        new Thread(() -> {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    imageView.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    // Hàm hiển thị WebView (MỚI)
    private void displayWebView(File file) {
        progressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE); // Hiển thị

        // Cấu hình cơ bản cho WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true); // Quan trọng: cho phép đọc file
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Ẩn progress bar khi load xong
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi khi load trang: " + description, Toast.LENGTH_SHORT).show();
            }
        });

        // Load file HTML
        webView.loadUrl("file://" + file.getAbsolutePath());
    }

    private void openFileExternally(File file) {
        // ... (Không thay đổi)
        Uri fileUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getApplicationContext().getPackageName() + ".provider",
                file
        );

        String mimeType = getContext().getContentResolver().getType(fileUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cấp quyền đọc cho app khác

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng để mở file này", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayUnsupportedFileView(File file) {
        progressBar.setVisibility(View.GONE); // Ẩn loading
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        layoutUnsupportedFile.setVisibility(View.VISIBLE); // HIỂN THỊ

        // (Optional) Cập nhật text để cho người dùng biết tên file
        if(textViewUnsupportedInfo != null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath()).toUpperCase();
            textViewUnsupportedInfo.setText(
                    String.format("Không hỗ trợ xem trước file '%s' (kiểu: %s).", file.getName(), extension)
            );
        }
    }
}