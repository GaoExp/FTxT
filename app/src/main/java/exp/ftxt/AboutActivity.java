package exp.ftxt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import exp.ftxt.utils.ApkDownloadHelper;
import exp.ftxt.utils.UpdateChecker;

public class AboutActivity extends AppCompatActivity {

    private TextView versionNameText;
    private TextView versionCodeText;
    private TextView versionLabel;
    private Switch autoSwitch;
    private View intervalRow;
    private TextView intervalValue;
    private TextView checkNowBtn;
    private TextView statusText;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tentang Aplikasi");
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView appIcon = findViewById(R.id.aboutAppIcon);
        appIcon.setImageResource(R.mipmap.ic_launcher);

        versionNameText = findViewById(R.id.aboutVersionName);
        versionCodeText = findViewById(R.id.aboutVersionCode);
        versionLabel = findViewById(R.id.aboutAppVersionLabel);
        autoSwitch = findViewById(R.id.updateAutoSwitch);
        intervalRow = findViewById(R.id.updateIntervalRow);
        intervalValue = findViewById(R.id.updateIntervalValue);
        checkNowBtn = findViewById(R.id.updateCheckNowBtn);
        statusText = findViewById(R.id.updateStatusText);

        String versionName = getVersionName();
        int versionCode = getVersionCode();
        versionNameText.setText(versionName);
        versionCodeText.setText(String.valueOf(versionCode));
        versionLabel.setText("v" + versionName + " Beta");

        final SharedPreferences prefs = getSharedPreferences(UpdateChecker.PREFS_NAME, MODE_PRIVATE);

        boolean auto = prefs.getBoolean(UpdateChecker.PREF_AUTO, false);
        autoSwitch.setChecked(auto);

        int intervalIdx = prefs.getInt(UpdateChecker.PREF_INTERVAL, UpdateChecker.DEFAULT_INTERVAL_INDEX);
        applyIntervalUi(auto, intervalIdx);
        intervalValue.setText(UpdateChecker.INTERVAL_LABELS[clampIndex(intervalIdx)]);

        // Toggle otomatis
        autoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(UpdateChecker.PREF_AUTO, isChecked).apply();
            int idx = prefs.getInt(UpdateChecker.PREF_INTERVAL, UpdateChecker.DEFAULT_INTERVAL_INDEX);
            applyIntervalUi(isChecked, idx);
            if (isChecked) {
                // Saat baru dinyalakan, langsung cek sekali sebagai konfirmasi
                runManualCheck(false);
            }
        });

        // Frekuensi popup
        intervalValue.setOnClickListener(v -> showIntervalPopup(v));
        intervalRow.setOnClickListener(v -> showIntervalPopup(v));

        // Tombol periksa sekarang — memunculkan dialog jika ada update, update status jika tidak
        checkNowBtn.setOnClickListener(v -> runManualCheck(true));

        // Tombol kunjungi daftar rilis
        findViewById(R.id.updateReleasesBtn).setOnClickListener(v ->
                openBrowser(UpdateChecker.RELEASES_PAGE));

        refreshStatusText(prefs);

        // Tombol tautan
        findViewById(R.id.aboutReadmeBtn).setOnClickListener(v -> openDoc("README"));
        findViewById(R.id.aboutPanduanBtn).setOnClickListener(v -> openDoc("PANDUAN"));
        findViewById(R.id.aboutChangelogBtn).setOnClickListener(v -> openDoc("CHANGELOG"));
        findViewById(R.id.aboutGithubBtn).setOnClickListener(v ->
                openBrowser("https://github.com/GaoExp/FTxT"));
        findViewById(R.id.aboutIssueBtn).setOnClickListener(v ->
                openBrowser("https://github.com/GaoExp/FTxT/issues"));
    }

    private int clampIndex(int idx) {
        if (idx < 0 || idx >= UpdateChecker.INTERVALS_MS.length) {
            return UpdateChecker.DEFAULT_INTERVAL_INDEX;
        }
        return idx;
    }

    private void applyIntervalUi(boolean auto, int idx) {
        boolean enabled = auto;
        intervalRow.setEnabled(enabled);
        intervalRow.setAlpha(enabled ? 1f : 0.5f);
        intervalValue.setEnabled(enabled);
        intervalValue.setText(UpdateChecker.INTERVAL_LABELS[clampIndex(idx)]);
    }

    private void showIntervalPopup(View anchor) {
        final SharedPreferences prefs = getSharedPreferences(UpdateChecker.PREFS_NAME, MODE_PRIVATE);
        int current = clampIndex(prefs.getInt(UpdateChecker.PREF_INTERVAL, UpdateChecker.DEFAULT_INTERVAL_INDEX));
        new android.app.AlertDialog.Builder(this)
                .setTitle("Frekuensi Pemeriksaan Otomatis")
                .setSingleChoiceItems(UpdateChecker.INTERVAL_LABELS, current, (dialog, which) -> {
                    prefs.edit().putInt(UpdateChecker.PREF_INTERVAL, which).apply();
                    intervalValue.setText(UpdateChecker.INTERVAL_LABELS[which]);
                    dialog.dismiss();
                    if (autoSwitch.isChecked()) {
                        runManualCheck(false);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void runManualCheck(final boolean showDialog) {
        checkNowBtn.setText("Memeriksa…");
        checkNowBtn.setEnabled(false);
        UpdateChecker.checkForUpdate(this, (success, latestVersion, latestUrl, updateAvailable) -> {
            checkNowBtn.setText("Periksa Pembaruan Sekarang");
            checkNowBtn.setEnabled(true);
            if (success && updateAvailable) {
                refreshStatusText(getSharedPreferences(UpdateChecker.PREFS_NAME, MODE_PRIVATE));
                if (showDialog) {
                    showUpdateDialog(latestVersion, latestUrl);
                }
            } else if (success) {
                statusText.setText("Sudah menggunakan versi terbaru");
            } else {
                statusText.setText("Gagal memeriksa (periksa koneksi)");
            }
        });
    }

    private void showUpdateDialog(String latestVersion, String latestUrl) {
        if (latestVersion == null || latestVersion.isEmpty()) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Pembaruan tersedia")
                .setMessage("FTxT v" + latestVersion + " Beta tersedia untuk diunduh.")
                .setPositiveButton("Lihat Informasi", (d, w) ->
                        openBrowser(latestUrl == null ? UpdateChecker.RELEASES_PAGE : latestUrl))
                .setNegativeButton("Unduh", (d, w) -> startApkDownload())
                .setNeutralButton("Nanti Saja", null)
                .show();
    }

    private void startApkDownload() {
        final SharedPreferences prefs = getSharedPreferences(UpdateChecker.PREFS_NAME, MODE_PRIVATE);
        String apkUrl = prefs.getString(UpdateChecker.PREF_LATEST_DOWNLOAD, null);
        if (apkUrl == null || apkUrl.isEmpty()) {
            openBrowser(prefs.getString(UpdateChecker.PREF_LATEST_URL, UpdateChecker.RELEASES_PAGE));
            return;
        }
        ApkDownloadHelper.downloadAndInstall(this, apkUrl);
    }

    private void refreshStatusText(SharedPreferences prefs) {
        String latest = prefs.getString(UpdateChecker.PREF_LATEST_VERSION, null);
        if (latest != null && !latest.isEmpty()) {
            statusText.setText("Pembaruan tersedia: FTxT v" + latest + " Beta");
            statusText.setTextColor(0xFF2196F3);
            statusText.setOnClickListener(v -> showUpdateDialog(
                    latest, prefs.getString(UpdateChecker.PREF_LATEST_URL, null)));
        } else {
            statusText.setText("Sudah menggunakan versi terbaru");
            statusText.setTextColor(getColor(R.color.about_card_text_secondary));
            statusText.setOnClickListener(null);
        }
    }

    private void openDoc(String doc) {
        Intent intent = new Intent(this, DocumentationActivity.class);
        intent.putExtra(DocumentationActivity.EXTRA_DOC, doc);
        startActivity(intent);
    }

    private void openBrowser(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            // Browser tidak tersedia — abaikan
        }
    }

    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "4.93.0";
        }
    }

    private int getVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 279;
        }
    }
}
