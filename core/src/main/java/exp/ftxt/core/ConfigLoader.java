package exp.ftxt.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import exp.ftxt.features.battery_bar.BatteryBarConfig;
import exp.ftxt.features.battery_stats.BatteryStatsConfig;
import exp.ftxt.features.clock_module.ClockConfig;
import exp.ftxt.features.crosshair.CrosshairConfig;
import exp.ftxt.features.floating_text.TextConfig;
import exp.ftxt.features.fps_display.FpsConfig;
import exp.ftxt.features.memory_stats.MemoryConfig;
import exp.ftxt.features.network_stats.NetworkConfig;

/**
 * Memuat konfigurasi overlay dari SharedPreferences ke static config modul.
 * Dipakai bersama oleh MainActivity dan BootReceiver agar kondisi setelah boot
 * sama persis dengan setelah aplikasi dibuka.
 */
public final class ConfigLoader {

    private ConfigLoader() {}

    public static void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ftxt_prefs", Context.MODE_PRIVATE);

        TextConfig.touchPassthrough = prefs.getBoolean("text_lock", true);
        TextConfig.safeArea = prefs.getBoolean("text_safe_area", true);
        TextConfig.shadow.enabled = prefs.getBoolean("shadow_enabled", false);
        TextConfig.shadow.color = prefs.getInt("shadow_color", Color.BLACK);
        TextConfig.shadow.blur = prefs.getFloat("shadow_blur", 5f);
        TextConfig.shadow.offsetX = prefs.getFloat("shadow_offset_x", 3f);
        TextConfig.shadow.offsetY = prefs.getFloat("shadow_offset_y", 3f);
        TextConfig.bg.enabled = prefs.getBoolean("text_bg_enabled", false);
        TextConfig.bg.color = prefs.getInt("text_bg_color", 0xCC000000);
        TextConfig.bg.padding = prefs.getInt("text_bg_padding", 25);
        TextConfig.bg.offsetX = prefs.getInt("text_bg_offset_x", 0);
        TextConfig.bg.offsetY = prefs.getInt("text_bg_offset_y", 0);
        TextConfig.patternEnabled = prefs.getBoolean("text_pattern_enabled", false);
        TextConfig.patternSpacingH = prefs.getFloat("text_pattern_spacing_h", 180f);
        TextConfig.patternSpacingV = prefs.getFloat("text_pattern_spacing_v", 220f);
        TextConfig.patternAngle = prefs.getFloat("text_pattern_angle", -30f);

        String savedText = prefs.getString("text_content", "FunText");
        if (savedText != null && !savedText.isEmpty()) {
            TextConfig.text = savedText;
        }

        FpsConfig.enabled = prefs.getBoolean("fps_enabled", false);
        FpsConfig.safeArea = prefs.getBoolean("fps_safe_area", true);
        FpsConfig.color = prefs.getInt("fps_color", Color.GREEN);
        FpsConfig.shadow.enabled = prefs.getBoolean("fps_shadow_enabled", false);
        FpsConfig.shadow.color = prefs.getInt("fps_shadow_color", Color.BLACK);
        FpsConfig.shadow.blur = prefs.getFloat("fps_shadow_blur", 5f);
        FpsConfig.shadow.offsetX = prefs.getFloat("fps_shadow_offset_x", 3f);
        FpsConfig.shadow.offsetY = prefs.getFloat("fps_shadow_offset_y", 3f);
        FpsConfig.touchPassthrough = prefs.getBoolean("fps_lock", true);
        FpsConfig.showOnlyValue = prefs.getBoolean("fps_show_only_value", false);
        FpsConfig.labelColor = prefs.getInt("fps_label_color", Color.CYAN);
        FpsConfig.updateInterval = readFloatPref(prefs, "fps_update_interval", 1f);
        FpsConfig.bg.enabled = prefs.getBoolean("fps_bg_enabled", false);
        FpsConfig.bg.color = prefs.getInt("fps_bg_color", 0xCC000000);
        FpsConfig.bg.padding = prefs.getInt("fps_bg_padding", 10);
        FpsConfig.bg.offsetX = prefs.getInt("fps_bg_offset_x", 0);
        FpsConfig.bg.offsetY = prefs.getInt("fps_bg_offset_y", 0);

        ClockConfig.enabled = prefs.getBoolean("clock_enabled", false);
        ClockConfig.size = prefs.getFloat("clock_size", 12f);
        ClockConfig.dateSize = prefs.getFloat("clock_date_size", 10f);
        ClockConfig.color = prefs.getInt("clock_color", Color.GREEN);
        ClockConfig.shadow.enabled = prefs.getBoolean("clock_shadow_enabled", false);
        ClockConfig.shadow.color = prefs.getInt("clock_shadow_color", Color.BLACK);
        ClockConfig.shadow.blur = prefs.getFloat("clock_shadow_blur", 5f);
        ClockConfig.shadow.offsetX = prefs.getFloat("clock_shadow_offset_x", 3f);
        ClockConfig.shadow.offsetY = prefs.getFloat("clock_shadow_offset_y", 3f);
        ClockConfig.safeArea = prefs.getBoolean("clock_safe_area", true);
        ClockConfig.touchPassthrough = prefs.getBoolean("clock_lock", true);
        ClockConfig.showDate = prefs.getBoolean("clock_show_date", true);
        ClockConfig.bg.enabled = prefs.getBoolean("clock_bg_enabled", false);
        ClockConfig.bg.color = prefs.getInt("clock_bg_color", 0xCC000000);
        ClockConfig.bg.padding = prefs.getInt("clock_bg_padding", 10);

        NetworkConfig.enabled = prefs.getBoolean("network_enabled", false);
        NetworkConfig.size = readFloatPref(prefs, "network_size", 12f);
        NetworkConfig.color = prefs.getInt("network_color", Color.GREEN);
        NetworkConfig.labelColor = prefs.getInt("network_label_color", Color.CYAN);
        NetworkConfig.showOnlyValue = prefs.getBoolean("network_show_only_value", false);
        NetworkConfig.shadow.enabled = prefs.getBoolean("network_shadow_enabled", false);
        NetworkConfig.shadow.color = prefs.getInt("network_shadow_color", Color.BLACK);
        NetworkConfig.shadow.blur = prefs.getFloat("network_shadow_blur", 5f);
        NetworkConfig.shadow.offsetX = prefs.getFloat("network_shadow_offset_x", 3f);
        NetworkConfig.shadow.offsetY = prefs.getFloat("network_shadow_offset_y", 3f);
        NetworkConfig.safeArea = prefs.getBoolean("network_safe_area", true);
        NetworkConfig.touchPassthrough = prefs.getBoolean("network_lock", true);
        NetworkConfig.bg.enabled = prefs.getBoolean("network_bg_enabled", false);
        NetworkConfig.bg.color = prefs.getInt("network_bg_color", 0xCC000000);
        NetworkConfig.bg.padding = prefs.getInt("network_bg_padding", 8);
        NetworkConfig.bg.offsetX = prefs.getInt("network_bg_offset_x", 0);
        NetworkConfig.bg.offsetY = prefs.getInt("network_bg_offset_y", 0);
        NetworkConfig.bg.margin = prefs.getInt("network_bg_margin", 0);
        NetworkConfig.bg.radius = prefs.getInt("network_bg_radius", 0);
        NetworkConfig.textBold = prefs.getBoolean("network_text_bold", false);
        NetworkConfig.textItalic = prefs.getBoolean("network_text_italic", false);
        NetworkConfig.barColorDown = prefs.getInt("network_bar_color_down", NetworkConfig.DEFAULT_BAR_COLOR_DOWN);
        NetworkConfig.barColorUp = prefs.getInt("network_bar_color_up", NetworkConfig.DEFAULT_BAR_COLOR_UP);
        NetworkConfig.barEnabled = true;
        NetworkConfig.barInverted = prefs.getBoolean("network_bar_inverted", false);
        NetworkConfig.verticalLayout = prefs.getBoolean("network_vertical_layout", false);
        NetworkConfig.displayWidthDp = prefs.getInt("network_display_width_dp", NetworkConfig.DEFAULT_WIDTH_DP);
        NetworkConfig.updateInterval = readFloatPref(prefs, "network_update_interval", 1f);

        BatteryStatsConfig.enabled = prefs.getBoolean("battery_enabled", false);
        BatteryStatsConfig.size = prefs.getFloat("battery_size", 12f);
        BatteryStatsConfig.color = prefs.getInt("battery_color", Color.GREEN);
        BatteryStatsConfig.labelColor = prefs.getInt("battery_label_color", Color.CYAN);
        BatteryStatsConfig.separatorColor = prefs.getInt("battery_separator_color", Color.GRAY);
        BatteryStatsConfig.shadow.enabled = prefs.getBoolean("battery_shadow_enabled", false);
        BatteryStatsConfig.shadow.color = prefs.getInt("battery_shadow_color", Color.BLACK);
        BatteryStatsConfig.shadow.blur = prefs.getFloat("battery_shadow_blur", 5f);
        BatteryStatsConfig.shadow.offsetX = prefs.getFloat("battery_shadow_offset_x", 3f);
        BatteryStatsConfig.shadow.offsetY = prefs.getFloat("battery_shadow_offset_y", 3f);
        BatteryStatsConfig.safeArea = prefs.getBoolean("battery_safe_area", true);
        BatteryStatsConfig.touchPassthrough = prefs.getBoolean("battery_lock", true);
        BatteryStatsConfig.showOnlyValue = prefs.getBoolean("battery_show_only_value", false);
        BatteryStatsConfig.showTemperature = prefs.getBoolean("battery_show_temperature", true);
        BatteryStatsConfig.showPercentage = prefs.getBoolean("battery_show_percentage", false);
        BatteryStatsConfig.bg.enabled = prefs.getBoolean("battery_bg_enabled", false);
        BatteryStatsConfig.bg.color = prefs.getInt("battery_bg_color", 0xCC000000);
        BatteryStatsConfig.bg.padding = prefs.getInt("battery_bg_padding", 8);
        BatteryStatsConfig.bg.offsetX = prefs.getInt("battery_bg_offset_x", 0);
        BatteryStatsConfig.bg.offsetY = prefs.getInt("battery_bg_offset_y", 0);
        BatteryStatsConfig.bg.margin = prefs.getInt("battery_bg_margin", 0);
        BatteryStatsConfig.bg.radius = prefs.getInt("battery_bg_radius", 0);
        BatteryStatsConfig.updateInterval = readFloatPref(prefs, "battery_update_interval", 5f);

        BatteryBarConfig.enabled = prefs.getBoolean("batbar_enabled", false);
        BatteryBarConfig.quickMode = prefs.getBoolean("batbar_quick_mode", true);
        BatteryBarConfig.quickSide = prefs.getString("batbar_quick_side", "top");
        BatteryBarConfig.horizontal = prefs.getBoolean("batbar_horizontal", true);
        BatteryBarConfig.invert = prefs.getBoolean("batbar_invert", false);
        BatteryBarConfig.length = prefs.getFloat("batbar_length", 0.5f);
        BatteryBarConfig.thickness = prefs.getInt("batbar_thickness", 8);
        BatteryBarConfig.color = prefs.getInt("batbar_color", Color.GREEN);
        if (prefs.contains("batbar_color_scheme")) {
            BatteryBarConfig.colorScheme = prefs.getInt("batbar_color_scheme", BatteryBarConfig.SCHEME_NONE);
        } else {
            BatteryBarConfig.colorScheme = prefs.getBoolean("batbar_auto_color", false)
                    ? BatteryBarConfig.SCHEME_CLASSIC : BatteryBarConfig.SCHEME_NONE;
        }
        BatteryBarConfig.lowColor = prefs.getInt("batbar_low_color", Color.YELLOW);
        BatteryBarConfig.lowThreshold = prefs.getInt("batbar_low_threshold", 40);
        BatteryBarConfig.showEmptyStrip = prefs.getBoolean("batbar_show_empty_strip", true);
        BatteryBarConfig.emptyColor = prefs.getInt("batbar_empty_color", 0x66000000);
        BatteryBarConfig.radius = prefs.getInt("batbar_radius", 8);
        BatteryBarConfig.fadeSpeed = prefs.getInt("batbar_fade_speed", 1800);
        BatteryBarConfig.fadeEnabled = prefs.getBoolean("batbar_fade_enabled", false);
        BatteryBarConfig.shineEnabled = prefs.getBoolean("batbar_shine_enabled", false);
        BatteryBarConfig.shineSpeed = prefs.getInt("batbar_shine_speed", 1800);
        BatteryBarConfig.shineWidth = prefs.getInt("batbar_shine_width", 25);
        BatteryBarConfig.waveEnabled = prefs.getBoolean("batbar_wave_enabled", false);
        BatteryBarConfig.waveSpeed = prefs.getInt("batbar_wave_speed", 1000);
        BatteryBarConfig.waveAmplitude = prefs.getInt("batbar_wave_amplitude", 60);
        BatteryBarConfig.chargeWaveEnabled = prefs.getBoolean("batbar_charge_wave_enabled", false);
        BatteryBarConfig.chargeWaveSpeed = prefs.getInt("batbar_charge_wave_speed", 1000);
        BatteryBarConfig.chargeWaveAmplitude = prefs.getInt("batbar_charge_wave_amplitude", 60);
        BatteryBarConfig.updateInterval = readFloatPref(prefs, "batbar_update_interval", 1f);
        BatteryBarConfig.touchPassthrough = prefs.getBoolean("batbar_lock", true);
        BatteryBarConfig.safeArea = true;

        MemoryConfig.enabled = prefs.getBoolean("mem_enabled", false);
        MemoryConfig.backgroundMonitor = prefs.getBoolean("mem_bg_monitor", false);
        MemoryConfig.size = prefs.getFloat("mem_size", 12f);
        MemoryConfig.color = prefs.getInt("mem_color", Color.WHITE);
        MemoryConfig.labelColor = prefs.getInt("mem_label_color", Color.CYAN);
        MemoryConfig.separatorColor = prefs.getInt("mem_separator_color", Color.GRAY);
        MemoryConfig.shadow.enabled = prefs.getBoolean("mem_shadow_enabled", false);
        MemoryConfig.shadow.color = prefs.getInt("mem_shadow_color", Color.BLACK);
        MemoryConfig.shadow.blur = prefs.getFloat("mem_shadow_blur", 5f);
        MemoryConfig.shadow.offsetX = prefs.getFloat("mem_shadow_offset_x", 3f);
        MemoryConfig.shadow.offsetY = prefs.getFloat("mem_shadow_offset_y", 3f);
        MemoryConfig.safeArea = prefs.getBoolean("mem_safe_area", true);
        MemoryConfig.touchPassthrough = prefs.getBoolean("mem_lock", true);
        MemoryConfig.showOnlyValue = prefs.getBoolean("mem_show_only_value", false);
        MemoryConfig.showJavaHeap = prefs.getBoolean("mem_show_java", true);
        MemoryConfig.showNativeHeap = prefs.getBoolean("mem_show_native", true);
        MemoryConfig.showGraphics = prefs.getBoolean("mem_show_graphics", true);
        MemoryConfig.showTotal = prefs.getBoolean("mem_show_total", true);
        MemoryConfig.itemOrder = prefs.getString("mem_item_order", "java,native,graphics,total");
        MemoryConfig.bg.enabled = prefs.getBoolean("mem_bg_enabled", false);
        MemoryConfig.bg.color = prefs.getInt("mem_bg_color", 0xCC000000);
        MemoryConfig.bg.padding = prefs.getInt("mem_bg_padding", 8);
        MemoryConfig.bg.offsetX = prefs.getInt("mem_bg_offset_x", 0);
        MemoryConfig.bg.offsetY = prefs.getInt("mem_bg_offset_y", 0);
        MemoryConfig.bg.margin = prefs.getInt("mem_bg_margin", 0);
        MemoryConfig.bg.radius = prefs.getInt("mem_bg_radius", 0);
        MemoryConfig.updateInterval = readFloatPref(prefs, "mem_update_interval", 1f);

        CrosshairConfig.enabled = prefs.getBoolean("crosshair_enabled", false);
        CrosshairConfig.styleIndex = prefs.getInt("crosshair_style", 1);
        CrosshairConfig.size = readFloatPref(prefs, "crosshair_size", 32f);
        CrosshairConfig.opacity = prefs.getInt("crosshair_opacity", 100);
        CrosshairConfig.touchPassthrough = prefs.getBoolean("crosshair_lock", true);
        CrosshairConfig.safeArea = prefs.getBoolean("crosshair_safe_area", true);
        CrosshairConfig.colorEnabled = true;
        CrosshairConfig.color = prefs.getInt("crosshair_color", Color.WHITE);
        CrosshairConfig.rotation = prefs.getFloat("crosshair_rotation", 0f);
        CrosshairConfig.bg.enabled = prefs.getBoolean("crosshair_bg_enabled", false);
        CrosshairConfig.bg.color = prefs.getInt("crosshair_bg_color", 0xCC000000);
        CrosshairConfig.bg.padding = prefs.getInt("crosshair_bg_padding", 8);
        CrosshairConfig.bg.offsetX = prefs.getInt("crosshair_bg_offset_x", 0);
        CrosshairConfig.bg.offsetY = prefs.getInt("crosshair_bg_offset_y", 0);
        CrosshairConfig.bg.margin = prefs.getInt("crosshair_bg_margin", 0);
        CrosshairConfig.bg.radius = prefs.getInt("crosshair_bg_radius", 0);
    }

    private static float readFloatPref(SharedPreferences prefs, String key, float defaultVal) {
        try {
            return prefs.getFloat(key, defaultVal);
        } catch (ClassCastException e) {
            int old = prefs.getInt(key, (int) defaultVal);
            prefs.edit().remove(key).putFloat(key, old).apply();
            return old;
        }
    }
}
