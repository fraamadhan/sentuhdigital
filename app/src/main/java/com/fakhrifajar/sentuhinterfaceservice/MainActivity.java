package com.fakhrifajar.sentuhinterfaceservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fakhrifajar.sentuhinterfaceservice.databinding.ActivityMainBinding;
import com.fakhrifajar.sentuhinterfaceservice.service.MyOverlayService;

public class MainActivity extends AppCompatActivity implements MyOverlayService.ServiceCallback {

    private static final int REQ_OVERLAY = 1001;

    private ActivityMainBinding binding;   // <— field binding

    private MyOverlayService myService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            MyOverlayService.LocalBinder binder = (MyOverlayService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.setCallback(MainActivity.this);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            myService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        binding.btnStartService.setOnClickListener(v -> {
            if (canDrawOverlays()) {
                startAndBindService();
            } else {
                requestOverlayPermission();
            }
        });
    }

    private boolean canDrawOverlays() {
        return Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        Toast.makeText(this, "Allow overlay permission to show Service button.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQ_OVERLAY);
    }

    private void startAndBindService() {
        Intent svc = new Intent(this, MyOverlayService.class);
        startService(svc);
        bindService(svc, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY) {
            if (canDrawOverlays()) {
                startAndBindService();
            } else {
                Toast.makeText(this, "Overlay permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            myService.setCallback(null);
            unbindService(connection);
            isBound = false;
        }
        binding = null;
    }

    @Override
    public void onMessageFromService(final String message) {
        runOnUiThread(() -> {
            if (binding != null) {
                binding.tvResult.setText("From Service: " + message);
            }
        });
    }
}