package com.example.saftyapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

public class CameraCaptureHelper {
    private static final String TAG = "CameraCaptureHelper";

    private final Context context;
    private final CameraManager cameraManager;
    
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    
    private String frontPath = "";
    private String rearPath = "";
    private CameraCaptureCallback callback;
    private long timestamp;

    public interface CameraCaptureCallback {
        void onCaptured(String frontPhotoPath, String rearPhotoPath);
        void onError(String message);
    }

    public CameraCaptureHelper(Context context) {
        this.context = context.getApplicationContext();
        this.cameraManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void capturePhotos(CameraCaptureCallback callback) {
        this.callback = callback;
        this.timestamp = System.currentTimeMillis();
        startCameraThread();
        
        // Start with front camera
        captureNext(CameraCharacteristics.LENS_FACING_FRONT);
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("CameraBgThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopCameraThread() {
        if (cameraThread != null) {
            cameraThread.quitSafely();
            try {
                cameraThread.join();
                cameraThread = null;
                cameraHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while stopping camera thread", e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void captureNext(int lensFacing) {
        try {
            String cameraId = getCameraIdOfFacing(lensFacing);
            if (cameraId == null) {
                Log.w(TAG, "No camera found for lens facing: " + lensFacing);
                handleCameraFinished(lensFacing, null);
                return;
            }

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    takeSnapshot(lensFacing);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    closeCameraQuietly();
                    handleCameraFinished(lensFacing, null);
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    closeCameraQuietly();
                    handleCameraFinished(lensFacing, null);
                }
            }, cameraHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Access exception when opening camera", e);
            handleCameraFinished(lensFacing, null);
        }
    }

    private void takeSnapshot(int lensFacing) {
        if (cameraDevice == null) return;
        try {
            // We use 640x480 resolution for fast background capture
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                File photoFile = null;
                try {
                    image = reader.acquireNextImage();
                    if (image != null) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        
                        File dir = new File(context.getFilesDir(), "evidence");
                        if (!dir.exists()) dir.mkdirs();
                        
                        String prefix = (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) ? "front" : "rear";
                        photoFile = new File(dir, prefix + "_" + timestamp + ".jpg");
                        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                            fos.write(bytes);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save photo", e);
                } finally {
                    if (image != null) image.close();
                    closeCameraQuietly();
                    handleCameraFinished(lensFacing, (photoFile != null) ? photoFile.getAbsolutePath() : null);
                }
            }, cameraHandler);

            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        builder.addTarget(imageReader.getSurface());
                        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        session.capture(builder.build(), null, cameraHandler);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Capture exception", e);
                        closeCameraQuietly();
                        handleCameraFinished(lensFacing, null);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    closeCameraQuietly();
                    handleCameraFinished(lensFacing, null);
                }
            }, cameraHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Setup exception", e);
            closeCameraQuietly();
            handleCameraFinished(lensFacing, null);
        }
    }

    private void handleCameraFinished(int lensFacing, String savedPath) {
        if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            frontPath = (savedPath != null) ? savedPath : "";
            // Proceed to rear camera
            cameraHandler.post(() -> captureNext(CameraCharacteristics.LENS_FACING_BACK));
        } else {
            rearPath = (savedPath != null) ? savedPath : "";
            // Complete process
            stopCameraThread();
            if (callback != null) {
                callback.onCaptured(frontPath, rearPath);
            }
        }
    }

    private void closeCameraQuietly() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private String getCameraIdOfFacing(int lensFacing) throws CameraAccessException {
        for (String id : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == lensFacing) {
                return id;
            }
        }
        // Fallback: return first camera if specific facing not found
        String[] ids = cameraManager.getCameraIdList();
        return ids.length > 0 ? ids[0] : null;
    }
}
