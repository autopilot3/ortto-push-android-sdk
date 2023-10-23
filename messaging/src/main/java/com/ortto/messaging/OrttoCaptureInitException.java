package com.ortto.messaging;

public class OrttoCaptureInitException extends Exception {
    OrttoCaptureInitException() {
        super("Capture is not initialized. Call Ortto.instance().initCapture first.");
    }
}
