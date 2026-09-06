package exp.ftxt;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import exp.ftxt.core.FloatingService;
import exp.ftxt.core.NotificationHelper;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.memory_stats.MemoryMonitor;
import exp.ftxt.features.memory_stats.MemoryModule;

public class SettingsActivity extends AppCompatActivity {

    private Switch overlaySwitch;
    private Switch notificationSwitch;
    private Switch batterySwitch;
    private Switch iconSwitch;
    private Switch debuggingSidebarSwitch;
    private EditText debuggingPasswordInput;
    private TextView developerStatusLabel;
    private TextView debuggingUnlockBtn;
    private TextView debuggingRelockBtn;
    private Switch memorySidebarSwitch;
    private TextView exportDbBtn;
    private RadioGroup statusBarModeGroup;
    private ImageView statusBarPreview;
    private LinearLayout statusBarTempScaleGroup;
    private LinearLayout statusBarPercentScaleGroup;
    private LinearLayout statusBarDateScaleGroup;
    private SeekBar statusBarTempScaleBar;
    private SeekBar statusBarPercentScaleBar;
    private SeekBar statusBarDateScaleBar;
    private TextView statusBarTempScaleValue;
    private TextView statusBarPercentScaleValue;
    private TextView statusBarDateScaleValue;
    private SeekBar statusBarDateTopScaleBar;
    private TextView statusBarDateTopScaleValue;
    private SeekBar statusBarDateBottomScaleBar;
    private TextView statusBarDateBottomScaleValue;
    private SeekBar statusBarPercentRingBar;
    private TextView statusBarPercentRingValue;
    private SeekBar statusBarPercentNumBar;
    private TextView statusBarPercentNumValue;
    private RadioGroup statusBarDateLangGroup;
    private TextView statusBarDateFormatValue;
    private PopupWindow statusBarFormatPopup;
    private Handler statusBarPreviewHandler;
    private Switch notifCustomSwitch;
    private LinearLayout notifIntervalRow;
    private RadioGroup notifIntervalGroup;

    private static final String[] DATE_FORMATS = {
            "d + Hari", "dd + Hari", "d + Bulan", "dd + Bulan",
            "d/M + Hari", "dd/MM + Hari", "Hari + d/M", "Hari + dd/MM"
    };

    private static final String DEBUGGING_PASSWORD = "01000110 01010100 01111000 01010100";
    public static final String PREF_DEVELOPER_UNLOCKED = "developer_unlocked";

    public static final String ACTION_PANEL_VISIBILITY_CHANGED = "exp.ftxt.PANEL_VISIBILITY_CHANGED";
    public static final String EXTRA_PANEL_ID = "panel_id";
    public static final String EXTRA_PANEL_VISIBLE = "panel_visible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Konfigurasi");
        toolbar.setNavigationOnClickListener(v -> finish());

        overlaySwitch = findViewById(R.id.overlayPermissionSwitch);
        notificationSwitch = findViewById(R.id.notificationPermissionSwitch);
        batterySwitch = findViewById(R.id.batteryPermissionSwitch);
        iconSwitch = findViewById(R.id.iconSwitch);

        SharedPreferences prefs = getSharedPreferences("ftxt_prefs", MODE_PRIVATE);

        updatePermissionSwitches();

        overlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(overlaySwitch, isChecked);
            if (isChecked && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            }
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(notificationSwitch, isChecked);
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                }
            }
        });

        batterySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(batterySwitch, isChecked);
            if (isChecked) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:" + getPackageName())));
                }
            }
        });

        boolean useAltIcon = prefs.getBoolean("alt_icon", false);
        iconSwitch.setChecked(useAltIcon);
        applySwitchTint(iconSwitch, useAltIcon);

        iconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(iconSwitch, isChecked);
            prefs.edit().putBoolean("alt_icon", isChecked).apply();
            setIcon(isChecked);
        });

        statusBarModeGroup = findViewById(R.id.statusBarModeGroup);
        statusBarPreview = findViewById(R.id.statusBarPreview);

        String statusBarMode = prefs.getString(NotificationHelper.PREF_STATUS_BAR_MODE, "temp");
        if ("percent".equals(statusBarMode)) {
            statusBarModeGroup.check(R.id.statusBarModePercent);
        } else if ("date".equals(statusBarMode)) {
            statusBarModeGroup.check(R.id.statusBarModeDate);
        } else {
            statusBarModeGroup.check(R.id.statusBarModeTemp);
        }

        statusBarModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = "temp";
            if (checkedId == R.id.statusBarModePercent) mode = "percent";
            else if (checkedId == R.id.statusBarModeDate) mode = "date";
            prefs.edit().putString(NotificationHelper.PREF_STATUS_BAR_MODE, mode).apply();
            FloatingService.updateNotification();
            applyStatusBarScaleVisibility();
            updateStatusBarPreview();
        });

        statusBarPreviewHandler = new Handler(Looper.getMainLooper());

        statusBarTempScaleGroup = findViewById(R.id.statusBarTempScaleGroup);
        statusBarPercentScaleGroup = findViewById(R.id.statusBarPercentScaleGroup);
        statusBarDateScaleGroup = findViewById(R.id.statusBarDateScaleGroup);
        statusBarTempScaleBar = findViewById(R.id.statusBarTempScaleBar);
        statusBarPercentScaleBar = findViewById(R.id.statusBarPercentScaleBar);
        statusBarDateScaleBar = findViewById(R.id.statusBarDateScaleBar);
        statusBarTempScaleValue = findViewById(R.id.statusBarTempScaleValue);
        statusBarPercentScaleValue = findViewById(R.id.statusBarPercentScaleValue);
        statusBarDateScaleValue = findViewById(R.id.statusBarDateScaleValue);
        statusBarDateTopScaleBar = findViewById(R.id.statusBarDateTopScaleBar);
        statusBarDateTopScaleValue = findViewById(R.id.statusBarDateTopScaleValue);
        statusBarDateBottomScaleBar = findViewById(R.id.statusBarDateBottomScaleBar);
        statusBarDateBottomScaleValue = findViewById(R.id.statusBarDateBottomScaleValue);
        statusBarPercentRingBar = findViewById(R.id.statusBarPercentRingBar);
        statusBarPercentRingValue = findViewById(R.id.statusBarPercentRingValue);
        statusBarPercentNumBar = findViewById(R.id.statusBarPercentNumBar);
        statusBarPercentNumValue = findViewById(R.id.statusBarPercentNumValue);

        bindStatusBarScale(statusBarTempScaleBar, statusBarTempScaleValue, "temp");
        bindStatusBarScale(statusBarPercentScaleBar, statusBarPercentScaleValue, "percent");
        bindStatusBarScale(statusBarDateScaleBar, statusBarDateScaleValue, "date");
        bindStatusBarScale(statusBarDateTopScaleBar, statusBarDateTopScaleValue, "date_top");
        bindStatusBarScale(statusBarDateBottomScaleBar, statusBarDateBottomScaleValue, "date_bottom");
        bindStatusBarScale(statusBarPercentRingBar, statusBarPercentRingValue, "percent_ring");
        bindStatusBarScale(statusBarPercentNumBar, statusBarPercentNumValue, "percent_num");

        statusBarDateLangGroup = findViewById(R.id.statusBarDateLangGroup);
        statusBarDateFormatValue = findViewById(R.id.statusBarDateFormatValue);

        String dateLang = prefs.getString(NotificationHelper.PREF_STATUS_BAR_DATE_LANG, "eng");
        statusBarDateLangGroup.check("in".equals(dateLang)
                ? R.id.statusBarDateLangIn : R.id.statusBarDateLangEng);

        statusBarDateLangGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = (checkedId == R.id.statusBarDateLangIn) ? "in" : "eng";
            prefs.edit().putString(NotificationHelper.PREF_STATUS_BAR_DATE_LANG, lang).apply();
            FloatingService.updateNotification();
            updateStatusBarPreview();
        });

        statusBarDateFormatValue.setText(DATE_FORMATS[NotificationHelper.getStatusBarDateFormat(this)]);
        statusBarDateFormatValue.setOnClickListener(v -> showDateFormatPopup(v, prefs));

        applyStatusBarScaleVisibility();

        notifCustomSwitch = findViewById(R.id.notifCustomSwitch);
        notifIntervalRow = findViewById(R.id.notifIntervalRow);
        notifIntervalGroup = findViewById(R.id.notifIntervalGroup);

        boolean notifCustom = prefs.getBoolean(NotificationHelper.PREF_NOTIF_CUSTOM, true);
        notifCustomSwitch.setChecked(notifCustom);
        applySwitchTint(notifCustomSwitch, notifCustom);

        long interval = NotificationHelper.getNotificationTitleIntervalMs(this);
        if (interval == 1000) {
            notifIntervalGroup.check(R.id.notifInterval1);
        } else if (interval == 5000) {
            notifIntervalGroup.check(R.id.notifInterval5);
        } else if (interval == 10000) {
            notifIntervalGroup.check(R.id.notifInterval10);
        } else {
            notifIntervalGroup.check(R.id.notifInterval3);
        }
        applyNotifIntervalEnabled(notifCustom);

        notifCustomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(notifCustomSwitch, isChecked);
            prefs.edit().putBoolean(NotificationHelper.PREF_NOTIF_CUSTOM, isChecked).apply();
            applyNotifIntervalEnabled(isChecked);
            FloatingService.updateNotification();
        });

        notifIntervalGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int ms = 3000;
            if (checkedId == R.id.notifInterval1) ms = 1000;
            else if (checkedId == R.id.notifInterval5) ms = 5000;
            else if (checkedId == R.id.notifInterval10) ms = 10000;
            prefs.edit().putInt(NotificationHelper.PREF_NOTIF_CUSTOM_INTERVAL, ms).apply();
            FloatingService.restartIconCycling();
        });

        debuggingSidebarSwitch = findViewById(R.id.debuggingSidebarSwitch);
        debuggingPasswordInput = findViewById(R.id.debuggingPasswordInput);
        developerStatusLabel = findViewById(R.id.developerStatusLabel);
        debuggingUnlockBtn = findViewById(R.id.debuggingUnlockBtn);
        debuggingRelockBtn = findViewById(R.id.debuggingRelockBtn);
        memorySidebarSwitch = findViewById(R.id.memorySidebarSwitch);

        exportDbBtn = findViewById(R.id.exportDbBtn);
        exportDbBtn.setOnClickListener(v -> exportDatabases());

        boolean developerUnlocked = prefs.getBoolean(PREF_DEVELOPER_UNLOCKED, false);
        boolean showDebugging = developerUnlocked && prefs.getBoolean("debugging_show_in_sidebar", false);
        boolean showMemory = developerUnlocked && prefs.getBoolean("memory_show_in_sidebar", false);

        memorySidebarSwitch.setChecked(showMemory);
        applySwitchTint(memorySidebarSwitch, showMemory);

        memorySidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(memorySidebarSwitch, isChecked);
            prefs.edit().putBoolean("memory_show_in_sidebar", isChecked).apply();
            if (!isChecked) turnOffMemoryPanel(prefs);
        });

        debuggingSidebarSwitch.setChecked(showDebugging);
        applySwitchTint(debuggingSidebarSwitch, showDebugging);

        debuggingSidebarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applySwitchTint(debuggingSidebarSwitch, isChecked);
            prefs.edit().putBoolean("debugging_show_in_sidebar", isChecked).apply();
            if (!isChecked) sendPanelHiddenBroadcast("debugging");
        });

        debuggingUnlockBtn.setOnClickListener(v -> {
            String input = debuggingPasswordInput.getText().toString();
            if (input.equals(DEBUGGING_PASSWORD)) {
                debuggingPasswordInput.setText("");
                prefs.edit().putBoolean(PREF_DEVELOPER_UNLOCKED, true).apply();
                applyDeveloperState(true);
            } else {
                Toast.makeText(this, "Kunci salah", Toast.LENGTH_SHORT).show();
            }
        });

        debuggingRelockBtn.setOnClickListener(v -> {
            memorySidebarSwitch.setChecked(false);
            debuggingSidebarSwitch.setChecked(false);
            prefs.edit()
                    .putBoolean(PREF_DEVELOPER_UNLOCKED, false)
                    .putBoolean("memory_show_in_sidebar", false)
                    .putBoolean("debugging_show_in_sidebar", false)
                    .apply();
            applyDeveloperState(false);
        });

        applyDeveloperState(developerUnlocked);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionSwitches();
        startStatusBarPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (statusBarPreviewHandler != null) {
            statusBarPreviewHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startStatusBarPreview() {
        if (statusBarPreviewHandler == null || statusBarPreview == null) return;
        statusBarPreviewHandler.removeCallbacksAndMessages(null);
        final Runnable updater = new Runnable() {
            @Override
            public void run() {
                updateStatusBarPreview();
                statusBarPreviewHandler.postDelayed(this, 1000);
            }
        };
        statusBarPreviewHandler.post(updater);
    }

    private String getSelectedStatusBarMode() {
        int checkedId = statusBarModeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.statusBarModePercent) return "percent";
        if (checkedId == R.id.statusBarModeDate) return "date";
        return "temp";
    }

    private void updateStatusBarPreview() {
        if (statusBarModeGroup == null || statusBarPreview == null) return;
        statusBarPreview.setImageBitmap(
                NotificationHelper.buildStatusIconBitmap(this, getSelectedStatusBarMode()));
    }

    private void bindStatusBarScale(SeekBar bar, TextView value, String mode) {
        float scale = NotificationHelper.getStatusBarScale(this, mode);
        bar.setProgress(Math.round((scale - 0.6f) * 100f));
        value.setText(Math.round(scale * 100f) + "%");
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                float scale = 0.6f + progress * 0.01f;
                getSharedPreferences("ftxt_prefs", MODE_PRIVATE)
                        .edit().putFloat(NotificationHelper.getStatusBarScalePref(mode), scale).apply();
                value.setText(Math.round(scale * 100f) + "%");
                FloatingService.updateNotification();
                updateStatusBarPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void showDateFormatPopup(View anchor, SharedPreferences prefs) {
        if (statusBarFormatPopup != null && statusBarFormatPopup.isShowing()) {
            statusBarFormatPopup.dismiss();
            return;
        }

        int currentIdx = NotificationHelper.getStatusBarDateFormat(this);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(0xFFFFFFFF);

        for (int i = 0; i < DATE_FORMATS.length; i++) {
            TextView item = new TextView(this);
            item.setText(DATE_FORMATS[i]);
            item.setPadding(dp(16), dp(10), dp(16), dp(10));
            item.setTextSize(14);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextColor(0xFF222222);
            item.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (i == currentIdx) {
                item.setBackgroundColor(0xFF4A90D9);
                item.setTextColor(0xFFFFFFFF);
            }

            final int idx = i;
            item.setOnClickListener(v -> {
                prefs.edit().putInt(NotificationHelper.PREF_STATUS_BAR_DATE_FORMAT, idx).apply();
                statusBarDateFormatValue.setText(DATE_FORMATS[idx]);
                FloatingService.updateNotification();
                updateStatusBarPreview();
                if (statusBarFormatPopup != null) statusBarFormatPopup.dismiss();
            });
            content.addView(item);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(content);

        statusBarFormatPopup = new PopupWindow(scrollView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(200), true);
        statusBarFormatPopup.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        statusBarFormatPopup.setOutsideTouchable(true);
        statusBarFormatPopup.setElevation(dp(4));
        statusBarFormatPopup.showAsDropDown(anchor, 0, dp(2));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void applyStatusBarScaleVisibility() {
        String mode = getSelectedStatusBarMode();
        statusBarTempScaleGroup.setVisibility("temp".equals(mode) ? View.VISIBLE : View.GONE);
        statusBarPercentScaleGroup.setVisibility("percent".equals(mode) ? View.VISIBLE : View.GONE);
        statusBarDateScaleGroup.setVisibility("date".equals(mode) ? View.VISIBLE : View.GONE);
    }

    private void applyNotifIntervalEnabled(boolean enabled) {
        notifIntervalRow.setEnabled(enabled);
        notifIntervalGroup.setEnabled(enabled);
        for (int i = 0; i < notifIntervalGroup.getChildCount(); i++) {
            notifIntervalGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    private void applySwitchTint(Switch sw, boolean isChecked) {
        if (isChecked) {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#90CAF9")));
        } else {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#EF9A9A")));
        }
    }

    private void applyDeveloperState(boolean unlocked) {
        String status = unlocked ? "Terbuka" : "Terkunci";
        int color = Color.parseColor(unlocked ? "#4CAF50" : "#E53935");
        SpannableString label = new SpannableString("Fitur Developer • " + status);
        label.setSpan(new ForegroundColorSpan(color),
                18, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        developerStatusLabel.setText(label);
        debuggingPasswordInput.setVisibility(unlocked ? android.view.View.GONE : android.view.View.VISIBLE);
        debuggingUnlockBtn.setVisibility(unlocked ? android.view.View.GONE : android.view.View.VISIBLE);
        debuggingRelockBtn.setVisibility(unlocked ? android.view.View.VISIBLE : android.view.View.GONE);
        memorySidebarSwitch.setEnabled(unlocked);
        debuggingSidebarSwitch.setEnabled(unlocked);
        exportDbBtn.setEnabled(unlocked);
    }

    private void turnOffMemoryPanel(SharedPreferences prefs) {
        if (MemoryConfig.enabled) {
            MemoryConfig.enabled = false;
            prefs.edit().putBoolean("mem_enabled", false).apply();
            if (FloatingService.instance != null) {
                MemoryModule memoryModule = FloatingService.memoryModule();
                if (memoryModule != null && memoryModule.isRunning()) {
                    FloatingService.stopModule(memoryModule);
                }
            }
        }
        if (MemoryConfig.backgroundMonitor) {
            MemoryConfig.backgroundMonitor = false;
            prefs.edit().putBoolean("mem_bg_monitor", false).apply();
            MemoryMonitor.stop();
        }
        sendPanelHiddenBroadcast("memory");
    }

    private void sendPanelHiddenBroadcast(String panelId) {
        Intent intent = new Intent(ACTION_PANEL_VISIBILITY_CHANGED);
        intent.putExtra(EXTRA_PANEL_ID, panelId);
        intent.putExtra(EXTRA_PANEL_VISIBLE, false);
        sendBroadcast(intent);
    }

    private void setIcon(boolean useAlt) {
        PackageManager pm = getPackageManager();
        ComponentName def = new ComponentName(this, "exp.ftxt.MainActivityDefault");
        ComponentName alt = new ComponentName(this, "exp.ftxt.MainActivityAlt");
        int defState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        int altState = useAlt ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(def, defState, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(alt, altState, PackageManager.DONT_KILL_APP);
    }

    private void exportDatabases() {
        File dbDir = getDatabasePath("ftxt_db").getParentFile();
        if (dbDir == null || !dbDir.isDirectory()) {
            Toast.makeText(this, "Folder database tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = dbDir.listFiles();
        if (files == null || files.length == 0) {
            Toast.makeText(this, "Tidak ada database untuk diekstrak", Toast.LENGTH_SHORT).show();
            return;
        }
        int ok = 0;
        for (File f : files) {
            if (f.isFile() && f.canRead() && f.length() > 0) {
                if (copyDbToDocuments(f)) ok++;
            }
        }
        Toast.makeText(this, ok > 0
                        ? "Diekstrak " + ok + " file ke Documents/FTxT/extract/db"
                        : "Gagal mengekstrak database",
                Toast.LENGTH_LONG).show();
    }

    private boolean copyDbToDocuments(File src) {
        try {
            String relPath = Environment.DIRECTORY_DOCUMENTS + "/FTxT/extract/db/";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, src.getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relPath);
                Uri uri = getContentResolver().insert(
                        MediaStore.Files.getContentUri("external"), values);
                if (uri == null) return false;
                try (OutputStream os = getContentResolver().openOutputStream(uri);
                     FileInputStream is = new FileInputStream(src)) {
                    if (os == null) return false;
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
                }
                return true;
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "FTxT/extract/db");
                if (!dir.exists() && !dir.mkdirs()) return false;
                File dst = new File(dir, src.getName());
                try (FileInputStream is = new FileInputStream(src);
                     FileOutputStream os = new FileOutputStream(dst)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePermissionSwitches() {
        boolean overlayOk = Settings.canDrawOverlays(this);
        overlaySwitch.setChecked(overlayOk);
        applySwitchTint(overlaySwitch, overlayOk);

        boolean notifOk;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifOk = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            notifOk = true;
        }
        notificationSwitch.setChecked(notifOk);
        applySwitchTint(notificationSwitch, notifOk);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean batteryOk = pm.isIgnoringBatteryOptimizations(getPackageName());
        batterySwitch.setChecked(batteryOk);
        applySwitchTint(batterySwitch, batteryOk);
    }

}