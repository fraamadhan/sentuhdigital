package com.fakhrifajar.sentuhinterfaceservice.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MyOverlayService extends Service {

    public interface ServiceCallback {
        void onMessageFromService(String message);
    }

    private final IBinder binder = new LocalBinder();
    private ServiceCallback callback;

    private WindowManager windowManager;
    private Button floatingButton;
    private boolean isButtonShown = false;

    public class LocalBinder extends Binder {
        public MyOverlayService getService() {
            return MyOverlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(ServiceCallback cb) {
        this.callback = cb;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingButton();
        return START_STICKY;
    }

    private void showFloatingButton() {
        if (isButtonShown) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingButton = new Button(this);
        floatingButton.setText("Send Text Ye");
        floatingButton.setAllCaps(false);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 500;
        params.y = 200;

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onMessageFromService("Hello there my name is Fakhri  Alexander");
                }
            }
        });

        windowManager.addView(floatingButton, params);
        isButtonShown = true;
    }

    private void removeFloatingButton() {
        if (isButtonShown && windowManager != null && floatingButton != null) {
            windowManager.removeView(floatingButton);
            isButtonShown = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeFloatingButton();
    }
}

