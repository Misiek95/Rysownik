package com.example.michal.rysownik2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Michal on 14.06.2018.
 */

class BitmapUtil {
    public static Bitmap loadBitmap(URI uri) throws IOException {
        File file = new File(uri);

        try (FileInputStream fileInputStream = new FileInputStream(file)){
            return BitmapFactory.decodeStream(fileInputStream);
        }
    }

    public static URI saveBitmap(Context context, Bitmap bitmap, String name, Bitmap.CompressFormat format)
            throws IOException {

        File directory = context.getCacheDir();
        File file = new File(directory, name);

        try (FileOutputStream output = new FileOutputStream(file)) {
            bitmap.compress(format, 100, output);
        }

        return file.toURI();
    }
}
