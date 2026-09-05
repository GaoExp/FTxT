package exp.ftxt.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Mendeteksi ANR (Application Not Responding) dengan cara memantau main thread
 * secara non-invasif: sebuah Runnable "heartbeat" dikirim ke main Looper secara
 * berkala. Selama main thread memproses pesan (tidak diblokir), heartbeat terus
 * diperbarui. Jika heartbeat tertunda melebihi ambang batas, berarti main thread
 * macet, lalu trace seluruh thread ditulis ke Documents/FTxT/Log_ANR.
 *
 * ANR tidak memicu UncaughtException, sehingga CrashLogger tidak menangkapnya.
 * Watcher ini menutup celah tersebut. Fokus: knack menangkap main-thread block.
 */
public final class AnrWatcher {

    private static final long CHECK_INTERVAL_MS = 1000L;
    private static final long ANR_THRESHOLD_MS = 5000L;
    private static final long COOLDOWN_MS = 30000L;

    private static volatile boolean started = false;
    private static volatile long lastBeat = 0L;
    private static volatile long lastWrite = 0L;

    private AnrWatcher() {}

    public static synchronized void start(final Context context) {
        if (started) return;
        started = true;
        final Context app = context.getApplicationContext();
        final Looper mainLooper = Looper.getMainLooper();
        final Handler mainHandler = new Handler(mainLooper);
        final Thread mainThread = mainLooper.getThread();

        final Runnable beat = new Runnable() {
            @Override
            public void run() {
                lastBeat = SystemClock.uptimeMillis();
                if (started) {
                    mainHandler.postDelayed(this, CHECK_INTERVAL_MS);
                }
            }
        };
        mainHandler.post(beat);

        Thread watcher = new Thread(() -> {
            while (started) {
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
                long now = SystemClock.uptimeMillis();
                if (now - lastBeat > ANR_THRESHOLD_MS && now - lastWrite >= COOLDOWN_MS) {
                    lastWrite = now;
                    writeAnrLog(app, mainThread, now - lastBeat);
                }
            }
            started = false;
        }, "AnrWatcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    public static synchronized void stop() {
        started = false;
    }

    private static void writeAnrLog(Context ctx, Thread mainThread, long blockedMs) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("=== FTxT ANR Trace ===");
            pw.println("Waktu : " + new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            pw.println("Main thread tidak memproses Looper message selama "
                    + (blockedMs / 1000L) + " detik");
            pw.println();
            pw.println("--- Thread \"main\" (terblokir) ---");
            pw.println("\"" + mainThread.getName() + "\" tid=" + mainThread.getId()
                    + " state=" + mainThread.getState());
            for (StackTraceElement el : mainThread.getStackTrace()) {
                pw.println("\tat " + el.toString());
            }
            pw.println();
            pw.println("--- Semua thread ---");
            for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                Thread t = entry.getKey();
                if (t == mainThread) continue;
                pw.println();
                pw.println("\"" + t.getName() + "\" tid=" + t.getId()
                        + " state=" + t.getState());
                for (StackTraceElement el : entry.getValue()) {
                    pw.println("\tat " + el.toString());
                }
            }
            pw.println();
            pw.println("=== Build Info ===");
            pw.println("Model   : " + Build.MODEL);
            pw.println("Android : " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            pw.flush();
            String content = sw.toString();

            String fileName = "FTxT_anr_"
                    + new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date())
                    + ".txt";
            writeToDocuments(ctx, content, fileName);

            ctx.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE)
                    .edit().putString("last_anr", content).apply();
        } catch (Exception ignored) {
        }
    }

    private static void writeToDocuments(Context ctx, String content, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/FTxT/Log_ANR");
            Uri uri = ctx.getContentResolver().insert(
                    MediaStore.Files.getContentUri("external"), values);
            if (uri == null) return;
            try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                if (os == null) return;
                os.write(content.getBytes("UTF-8"));
            }
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "FTxT/Log_ANR");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes("UTF-8"));
            }
        }
    }
}