package com.t3.autoreceiver;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
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

    /** Opens the accessory and sends the AOA handshake. Returns true on success. */
    public boolean open() {
        accessory = getAccessory();
        if (accessory == null || usbManager == null) return false;
        fd = usbManager.openAccessory(accessory);
        if (fd == null) return false;
        FileDescriptor raw = fd.getFileDescriptor();
        input = new FileInputStream(raw);
        output = new FileOutputStream(raw);
        sendHandshake();
        return true;
    }

    private void sendHandshake() {
        // AOA handshake: control transfers 51/52 with the identification strings.
        // Best-effort: if the phone is not in accessory mode this simply no-ops.
        try {
            usbManager.controlTransfer(0x40, 51, 0, 0, MANUFACTURER.getBytes("UTF-8"), MANUFACTURER.length(), 5000);
            usbManager.controlTransfer(0x40, 51, 0, 1, MODEL.getBytes("UTF-8"), MODEL.length(), 5000);
            usbManager.controlTransfer(0x40, 51, 0, 2, DESCRIPTION.getBytes("UTF-8"), DESCRIPTION.length(), 5000);
            usbManager.controlTransfer(0x40, 51, 0, 3, VERSION.getBytes("UTF-8"), VERSION.length(), 5000);
            usbManager.controlTransfer(0x40, 51, 0, 4, URI.getBytes("UTF-8"), URI.length(), 5000);
            usbManager.controlTransfer(0x40, 51, 0, 5, SERIAL.getBytes("UTF-8"), SERIAL.length(), 5000);
            // Start accessory mode (request 53).
            usbManager.controlTransfer(0x40, 53, 0, 0, new byte[0], 0, 5000);
        } catch (Exception ignored) {
            // Phone may not support AOA — caller falls back to mirror/BT audio.
        }
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
