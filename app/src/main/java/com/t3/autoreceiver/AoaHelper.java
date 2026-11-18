package com.t3.autoreceiver;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Android Open Accessory (AOA) helper — the USB transport that Android Auto
 * uses to talk to a phone over a cable.
 *
 * What this does:
 *  - Lists USB accessories attached to the head unit.
 *  - Opens the first accessory and performs the AOA handshake strings so a
 *    phone in accessory mode can identify us as an Android Auto head unit.
 *
 * What this does NOT do (by design, see docs/PROTOCOL-NOTES.md):
 *  - The full Android Auto Protocol (AAP) media/navigation stream. Google does
 *    not publish a head-unit SDK for sideloaded apps; a complete AAP receiver
 *    requires the proprietary protocol implementation. This app therefore
 *    auto-opens on USB connect and provides the Auto-style UI + Bluetooth
 *    audio + screen-mirror fallback, which is the practical private-use setup
 *    on a rooted T3.
 */
public final class AoaHelper {

    // AOA identification strings (must match the accessory filter on the phone side).
    private static final String MANUFACTURER = "T3 Auto";
    private static final String MODEL = "T3 Receiver";
    private static final String DESCRIPTION = "Android Auto Receiver for Allwinner T3";
    private static final String VERSION = "1.0";
    private static final String URI = "https://github.com/AMSVA011002/android_auto";
    private static final String SERIAL = "T3AUTO001";

    private final UsbManager usbManager;
    private UsbAccessory accessory;
    private ParcelFileDescriptor fd;
    private FileInputStream input;
    private FileOutputStream output;

    public AoaHelper(Context ctx) {
        usbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
    }

    /** Returns the first attached accessory, or null. */
    public UsbAccessory getAccessory() {
        if (usbManager == null) return null;
        UsbAccessory[] list = usbManager.getAccessoryList();
        if (list == null || list.length == 0) return null;
        return list[0];
    }

    /**
     * Performs the AOA handshake on the given device, then opens the accessory
     * for data. Returns true if the accessory was opened.
     * Best-effort: phones that don't support AOA simply skip the handshake.
     */
    public boolean open(UsbDevice device) {
        if (usbManager == null) return false;
        if (device != null) {
            sendHandshake(device);
        }
        accessory = getAccessory();
        if (accessory == null) return false;
        fd = usbManager.openAccessory(accessory);
        if (fd == null) return false;
        FileDescriptor raw = fd.getFileDescriptor();
        input = new FileInputStream(raw);
        output = new FileOutputStream(raw);
        return true;
    }

    /** Backwards-compatible no-arg open (handshake skipped). */
    public boolean open() {
        return open(null);
    }

    private void sendHandshake(UsbDevice device) {
        // AOA handshake: control transfers on the device connection.
        // NOTE: controlTransfer lives on UsbDeviceConnection, not UsbManager.
        UsbDeviceConnection conn = null;
        try {
            conn = usbManager.openDevice(device);
            if (conn == null) return;
            control(conn, 51, 0, MANUFACTURER);
            control(conn, 51, 1, MODEL);
            control(conn, 51, 2, DESCRIPTION);
            control(conn, 51, 3, VERSION);
            control(conn, 51, 4, URI);
            control(conn, 51, 5, SERIAL);
            // Start accessory mode (request 53).
            conn.controlTransfer(0x40, 53, 0, 0, new byte[0], 0, 5000);
        } catch (Exception ignored) {
            // Phone may not support AOA — caller falls back to mirror/BT audio.
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    private void control(UsbDeviceConnection conn, int request, int index, String value) throws Exception {
        byte[] bytes = value.getBytes("UTF-8");
        conn.controlTransfer(0x40, request, 0, index, bytes, bytes.length, 5000);
    }

    /** Reads one chunk from the accessory (blocking, caller should use a thread). */
    public int read(byte[] buf) throws IOException {
        if (input == null) return -1;
        return input.read(buf);
    }

    public void write(byte[] buf) throws IOException {
        if (output != null) output.write(buf);
    }

    public void close() {
        try { if (input != null) input.close(); } catch (Exception ignored) {}
        try { if (output != null) output.close(); } catch (Exception ignored) {}
        try { if (fd != null) fd.close(); } catch (Exception ignored) {}
        input = null;
        output = null;
        fd = null;
        accessory = null;
    }
}
