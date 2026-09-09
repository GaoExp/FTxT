package exp.ftxt.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import java.io.File;

public final class ApkDownloadHelper {

    private ApkDownloadHelper() {
    }

    /**
     * Menampilkan dialog progress unduhan, mengunduh APK di thread latar,
     * lalu membuka prompt instal Android bawaan saat selesai. Seluruh callback
     * dipanggil di main thread.
     */
    public static void downloadAndInstall(final Activity activity, final String apkUrl) {
        if (activity == null || activity.isFinishing() || apkUrl == null || apkUrl.isEmpty()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Mengunduh pembaruan");
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        float density = activity.getResources().getDisplayMetrics().density;
        layout.setPadding((int) (20 * density), (int) (20 * density),
                (int) (20 * density), (int) (20 * density));

        ProgressBar bar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);

        TextView tv = new TextView(activity);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText("0%");
        tv.setPadding(0, (int) (8 * density), 0, 0);

        layout.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(tv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        builder.setView(layout);
        final AlertDialog dialog = builder.show();

        UpdateChecker.downloadApk(activity, apkUrl, new UpdateChecker.DownloadCallback() {
            @Override
            public void onProgress(int percent) {
                bar.setProgress(percent);
                tv.setText(percent + "%");
            }

            @Override
            public void onSuccess(File apkFile) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                installApk(activity, apkFile);
            }

            @Override
            public void onError(String message) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                new AlertDialog.Builder(activity)
                        .setTitle("Gagal mengunduh")
                        .setMessage("Terjadi kesalahan saat mengunduh (" + message
                                + "). Periksa koneksi dan coba lagi.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private static void installApk(Activity activity, File apkFile) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity, activity.getPackageName() + ".fileprovider", apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            new AlertDialog.Builder(activity)
                    .setTitle("Gagal membuka instalasi")
                    .setMessage("Tidak dapat membuka prompt instalasi. Buka berkas APK secara manual "
                            + "di folder file aplikasi FTxT.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}