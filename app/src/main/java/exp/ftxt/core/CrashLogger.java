package exp.ftxt.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashLogger {
    private static boolean initialized = false;

    private CrashLogger() {}

    public static synchronized void init(Context context) {
        if (initialized) return;
        initialized = true;
        final Context app = context.getApplicationContext();
        final Thread.UncaughtExceptionHandler defaultHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            writeLog(app, thread, throwable);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    private static void writeLog(Context ctx, Thread thread, Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("=== FTxT Crash Log ===");
            pw.println("Waktu : " + new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            pw.println("Thread: " + thread.getName());
            pw.println();
            t.printStackTrace(pw);
            pw.println();
            pw.println("=== Build Info ===");
            pw.println("Model   : " + Build.MODEL);
            pw.println("Android : " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            pw.flush();
            String content = sw.toString();

            String fileName = "FTxT_crash_" + System.currentTimeMillis() + ".txt";
            writeToDownload(ctx, content, fileName);

            ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                    .edit().putString("last_crash", content).apply();
        } catch (Exception ignored) {
        }
    }

    private static void writeToDownload(Context ctx, String content, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = ctx.getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return;
            try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                if (os == null) return;
                os.write(content.getBytes("UTF-8"));
            }
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
        }
    }
}
