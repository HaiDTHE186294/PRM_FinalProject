package com.lkms.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.lkms.R;

public class QRScannerFragment extends Fragment {

    public interface QRScanListener {
        void onQRScanned(String serial);
    }

    private QRScanListener listener;
    private Button btnScanQR;

    public void setQRScanListener(QRScanListener listener) {
        this.listener = listener;
    }

    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String serial = result.getContents();
                    Toast.makeText(getContext(), "Scanned: " + serial, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onQRScanned(serial);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
        btnScanQR = view.findViewById(R.id.btnScanQR);
        btnScanQR.setOnClickListener(v -> startQRScanner());
        return view;
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Place a QR code inside the viewfinder");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        qrLauncher.launch(options);
    }
}