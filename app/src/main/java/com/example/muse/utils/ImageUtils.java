package com.example.muse.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class ImageUtils {
    public static Bitmap lqipToBitmap(String lqip) {
        if (lqip == null) {
            Log.d("MUSE_RCA", "lqipToBitmap: input is NULL");
            return null;
        }
        
        Log.d("MUSE_RCA", "lqipToBitmap: length = " + lqip.length());
        if (lqip.length() > 30) {
            Log.d("MUSE_RCA", "lqipToBitmap: prefix = " + lqip.substring(0, 30));
        }

        if (!lqip.contains(",")) {
            Log.d("MUSE_RCA", "lqipToBitmap: NO COMMA FOUND in string");
            // Some APIs might return raw base64 without the data URI prefix
        }

        try {
            String base64 = lqip.contains(",") ? lqip.substring(lqip.indexOf(",") + 1) : lqip;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            
            if (bitmap != null) {
                Log.d("MUSE_RCA", "lqipToBitmap: DECODE SUCCESS. Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            } else {
                Log.e("MUSE_RCA", "lqipToBitmap: DECODE FAILED (result is null)");
            }
            return bitmap;
        } catch (Exception e) {
            Log.e("MUSE_RCA", "lqipToBitmap: EXCEPTION: " + e.getMessage());
            return null;
        }
    }
}
