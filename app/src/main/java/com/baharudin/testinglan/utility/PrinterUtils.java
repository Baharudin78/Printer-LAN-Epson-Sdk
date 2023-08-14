package com.baharudin.testinglan.utility;

import android.graphics.Bitmap;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;

public class PrinterUtils {

    public static void addImage(Printer mPrinter, Bitmap logoData) throws Epos2Exception {
        int x = 0;
        int y = 0;
        int width = logoData.getWidth();
        int height = logoData.getHeight();
        int color = Printer.COLOR_1;
        int mode = Printer.MODE_MONO;
        int halftone = Printer.HALFTONE_DITHER;
        int brightness = Printer.PARAM_DEFAULT;
        int compress = Printer.COMPRESS_AUTO;

        mPrinter.addImage(logoData, x, y, width, height, color, mode, halftone, brightness, compress);
    }
}
