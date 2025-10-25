package com.lkms.ui.equipment;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.lkms.R;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PDF_URL = "EXTRA_PDF_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        WebView webView = findViewById(R.id.pdfWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String pdfUrl = getIntent().getStringExtra(EXTRA_PDF_URL);
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            finish();
            return;
        }

        String viewerUrl = "https://drive.google.com/viewerng/viewer?embedded=true&url=" + pdfUrl;

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(viewerUrl);
    }
}
