package exp.ftxt.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Util cek pembaruan FTxT via GitHub Releases API.
 * Menjaga sumber data versi remote, status terakhir, dan logika pemeriksaan otomatis.
 * Tidak menambah dependency: memakai HttpURLConnection + org.json bawaan.
 */
public class UpdateChecker {

    public static final String PREFS_NAME = "ftxt_prefs";

    public static final String PREF_AUTO = "update_check_auto";
    public static final String PREF_INTERVAL = "update_check_interval";
    public static final String PREF_LAST_CHECKED = "update_last_checked_at";
    public static final String PREF_LATEST_VERSION = "update_latest_version";
    public static final String PREF_LATEST_URL = "update_latest_url";
    public static final String PREF_LATEST_DOWNLOAD = "update_latest_download";

    /** Endpoint rilis terbaru dari GitHub. */
    public static final String RELEASE_URL = "https://api.github.com/repos/GaoExp/FTxT/releases/latest";
    /** Halaman rilis fallback (jika html_url tidak tersedia). */
    public static final String RELEASES_PAGE = "https://github.com/GaoExp/FTxT/releases";

    /** Frekuensi pemeriksaan otomatis (ms) sesuai urutan pilihan di UI. */
    public static final long[] INTERVALS_MS = {
            0L,            // setiap buka aplikasi
            12L * 3600000L, // 12 jam
            24L * 3600000L, // 1 hari
            3L * 24L * 3600000L, // 3 hari
            7L * 24L * 3600000L  // 7 hari
    };
    public static final String[] INTERVAL_LABELS = {
            "Setiap buka aplikasi", "12 jam", "1 hari", "3 hari", "7 hari"
    };
    public static final int DEFAULT_INTERVAL_INDEX = 2; // 1 hari

    public interface Callback {
        void onResult(boolean success, String latestVersion, String latestUrl, boolean updateAvailable);
    }

    public interface DownloadCallback {
        void onProgress(int percent);
        void onSuccess(java.io.File apkFile);
        void onError(String message);
    }

    /**
     * Periksa ke GitHub di thread latar, lalu simpan hasil ke prefs.
     * Dipanggil dari UI (memulai thread sendiri), aman dipanggil dari main thread.
     */
    public static void checkForUpdate(final Context context, final Callback callback) {
        new Thread(() -> {
            final String[] result = fetchLatest(context);
            // result[0] = tag, result[1] = html_url, result[2] = download_url, result[3] = pesan error
            boolean updateAvailable = false;
            if (result[0] != null) {
                int cmp = compareVersions(result[0], currentVersion(context));
                updateAvailable = cmp > 0;
                if (!updateAvailable) {
                    // Versi remote lebih lama / sama → anggap terkini, kosongkan data remote
                    result[0] = null;
                    result[1] = null;
                    result[2] = null;
                }
                saveResult(context, result[0], result[1], result[2]);
            } else {
                saveLastChecked(context);
            }

            final boolean finalAvailable = updateAvailable;
            final String finalVersion = result[0];
            final String finalUrl = result[1];
            postResult(callback, result[3] == null, finalVersion, finalUrl, finalAvailable);
        }).start();
    }

    /**
     * Cek otomatis saat aplikasi dibuka (diam-diam, tanpa dialog hasil terkini/gagal).
     * Dipanggil dari UI — memulai thread sendiri.
     * Hanya berjalan jika toggle ON dan interval sudah terlewati sejak cek terakhir.
     */
    public static void autoCheckIfNeeded(final Context context, final Runnable onUpdateFound) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(PREF_AUTO, false)) return;

        int idx = prefs.getInt(PREF_INTERVAL, DEFAULT_INTERVAL_INDEX);
        if (idx < 0 || idx >= INTERVALS_MS.length) idx = DEFAULT_INTERVAL_INDEX;
        long interval = INTERVALS_MS[idx];
        long last = prefs.getLong(PREF_LAST_CHECKED, 0L);
        long now = System.currentTimeMillis();
        // interval 0 = setiap kali buka aplikasi
        if (interval != 0 && (now - last) < interval) return;

        new Thread(() -> {
            String[] result = fetchLatest(context);
            if (result[0] != null) {
                int cmp = compareVersions(result[0], currentVersion(context));
                if (cmp > 0) {
                    saveResult(context, result[0], result[1], result[2]);
                    if (onUpdateFound != null) {
                        postRunnable(onUpdateFound);
                    }
                    return;
                }
                saveResult(context, null, null, null);
            } else {
                saveLastChecked(context);
            }
        }).start();
    }

    /** Ambil data rilis terbaru. Mengembalikan String[4]: {tag, html_url, download_url, errorMessage}. */
    private static String[] fetchLatest(Context context) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(RELEASE_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "FTxT");

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return new String[]{null, null, null, "HTTP " + code};
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject root = new JSONObject(sb.toString());
            String tag = root.optString("tag_name", "");
            String htmlUrl = root.optString("html_url", RELEASES_PAGE);
            if (tag.isEmpty()) {
                return new String[]{null, null, null, "no tag"};
            }
            // Bersihkan prefix 'v' jika ada
            String clean = tag.startsWith("v") ? tag.substring(1) : tag;
            String apkUrl = extractApkUrl(root);
            return new String[]{clean, htmlUrl, apkUrl, null};
        } catch (Exception e) {
            return new String[]{null, null, null, e.getClass().getSimpleName()};
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** Ambil URL unduhan APK pertama dari daftar asset rilis. */
    private static String extractApkUrl(JSONObject root) {
        try {
            JSONArray assets = root.optJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.optString("name", "");
                    if (name.toLowerCase(Locale.US).endsWith(".apk")) {
                        String url = asset.optString("browser_download_url", "");
                        if (!url.isEmpty()) return url;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Bandingkan dua versi string major.minor.patch. Return negatif/0/positif. */
    public static int compareVersions(String v1, String v2) {
        String[] a = v1.split("\\.");
        String[] b = v2.split("\\.");
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int na = i < a.length ? parseIntSafe(a[i]) : 0;
            int nb = i < b.length ? parseIntSafe(b[i]) : 0;
            if (na != nb) return Integer.compare(na, nb);
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String currentVersion(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "0";
        }
    }

    private static void saveResult(Context context, String latestVersion, String latestUrl, String latestDownload) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        e.putLong(PREF_LAST_CHECKED, System.currentTimeMillis());
        if (latestVersion != null) {
            e.putString(PREF_LATEST_VERSION, latestVersion);
        } else {
            e.remove(PREF_LATEST_VERSION);
        }
        if (latestUrl != null) {
            e.putString(PREF_LATEST_URL, latestUrl);
        } else {
            e.remove(PREF_LATEST_URL);
        }
        if (latestDownload != null) {
            e.putString(PREF_LATEST_DOWNLOAD, latestDownload);
        } else {
            e.remove(PREF_LATEST_DOWNLOAD);
        }
        e.apply();
    }

    private static void saveLastChecked(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putLong(PREF_LAST_CHECKED, System.currentTimeMillis()).apply();
    }

    /**
     * Unduh APK pembaruan ke folder file aplikasi (getExternalFilesDir) di thread latar.
     * Hasil dipanggil kembali di main thread. Tidak menambah dependency.
     */
    public static void downloadApk(final Context context, final String apkUrl,
                                   final DownloadCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(apkUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                conn.setRequestProperty("User-Agent", "FTxT");
                conn.setRequestProperty("Accept", "application/vnd.android.package-archive");

                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    postDownloadError(callback, "HTTP " + code);
                    return;
                }

                java.io.File dir = context.getExternalFilesDir(null);
                if (dir == null) {
                    postDownloadError(callback, "Storage tidak tersedia");
                    return;
                }
                java.io.File outFile = new java.io.File(dir, "FTxT_update.apk");
                if (outFile.exists()) {
                    outFile.delete();
                }

                int contentLength = conn.getContentLength();
                InputStream in = conn.getInputStream();
                FileOutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[8192];
                long total = 0;
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                    total += n;
                    if (contentLength > 0 && callback != null) {
                        final int pct = (int) (total * 100 / contentLength);
                        postRunnable(() -> callback.onProgress(pct));
                    }
                }
                out.flush();
                out.close();
                in.close();

                final java.io.File done = outFile;
                postRunnable(() -> callback.onSuccess(done));
            } catch (Exception e) {
                postDownloadError(callback, e.getClass().getSimpleName());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private static void postDownloadError(final DownloadCallback callback, final String message) {
        if (callback != null) {
            postRunnable(() -> callback.onError(message));
        }
    }

    private static void postResult(final Callback cb, final boolean success,
                                   final String version, final String url, final boolean available) {
        if (cb != null) {
            postRunnable(() -> cb.onResult(success, version, url, available));
        }
    }

    private static void postRunnable(final Runnable r) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }
}
