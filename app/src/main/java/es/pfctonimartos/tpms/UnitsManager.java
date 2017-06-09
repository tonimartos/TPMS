package es.pfctonimartos.tpms;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

import java.text.DecimalFormat;

public class UnitsManager {

    static String getPressureValue(int value) {
        return new DecimalFormat("0.0").format(((((double) value) * 1.0d) / 10.0d) * 1.0d);
    }

    static int byteToInt(byte b, int id) {
        String str = Integer.toBinaryString(b | AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY);
        int len = str.length();
        return Integer.valueOf(new StringBuffer(str.substring(len - 8, len)).reverse().toString().substring(id - 1, id)).intValue();
    }

    static String getTemValue(int value) {
        return Integer.toString(value);
    }

    static int getAbsValue(byte item) {
        byte value1 = item;
        if (value1 < 0) {
            return value1 + AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        }
        return value1;
    }

    static byte uniteByte(byte src0, byte src1) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        byte[] bArr = new byte[1];
        bArr[0] = src0;
        byte _b0 = (byte) (Byte.decode(stringBuilder.append(new String(bArr)).toString()).byteValue() << 4);
        stringBuilder = new StringBuilder("0x");
        bArr = new byte[1];
        bArr[0] = src1;
        return (byte) (_b0 ^ Byte.decode(stringBuilder.append(new String(bArr)).toString()).byteValue());
    }

    static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx += 1) {
            int intVal = bytes[idx] & MotionEventCompat.ACTION_MASK;
            if (intVal < 16) {
                sbuf.append("0");
            }
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }
}
