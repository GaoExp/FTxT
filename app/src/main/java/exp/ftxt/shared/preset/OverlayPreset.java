package exp.ftxt.shared.preset;

import exp.ftxt.shared.ui.ShadowConfig;

/**
 * Model data untuk satu preset overlay.
 * Semua field public agar GSON bisa serialize/deserialize langsung.
 */
public class OverlayPreset {

    // ====================================================================
    // 1. POSISI & UKURAN
    // ====================================================================

    /** Posisi X ter-normalisasi (0.0 = kiri, 1.0 = kanan) */
    public float posX;
    /** Posisi Y ter-normalisasi (0.0 = atas, 1.0 = bawah) */
    public float posY;
    /** Ukuran teks/overlay dalam sp */
    public float size;

    // ====================================================================
    // 2. TAMPILAN TEKS — Warna utama overlay
    // ====================================================================

    /** Warna teks overlay dalam format ARGB (int 32-bit) */
    public int color;

    // ====================================================================
    // 3. KOMPONEN SHADOW
    // ====================================================================

    /** Konfigurasi shadow (enabled, color, blur, offsetX, offsetY) */
    public ShadowConfig shadow;

    // ====================================================================
    // 4. KOMPONEN BACKGROUND
    // ====================================================================

    /** Apakah background aktif */
    public boolean bgEnabled;
    /** Warna background dalam ARGB */
    public int bgColor;
    /** Ukuran padding background dalam px */
    public int bgPadding;
    /** Offset X background relatif terhadap teks */
    public int bgOffsetX;
    /** Offset Y background relatif terhadap teks */
    public int bgOffsetY;
    /** Margin background */
    public int bgMargin;
    /** Radius sudut background (rounded corner) */
    public int bgRadius;

    // ====================================================================
    // 5. ORIENTASI
    // ====================================================================

    /** Mode orientasi saat preset disimpan: "portrait" atau "landscape" */
    public String orientation;

    // ====================================================================
    // CONSTRUCTOR
    // ====================================================================

    /** Constructor default — diperlukan GSON */
    public OverlayPreset() {
        shadow = new ShadowConfig();
    }

    /**
     * Constructor lengkap — mengisi semua field preset.
     *
     * @param name        Nama preset
     * @param posX        Posisi X (0.0–1.0)
     * @param posY        Posisi Y (0.0–1.0)
     * @param size        Ukuran teks dalam sp
     * @param color       Warna teks ARGB
     * @param shadow      Objek ShadowConfig
     * @param bgEnabled   Status background
     * @param bgColor     Warna background ARGB
     * @param bgPadding   Padding background
     * @param bgOffsetX   Offset X background
     * @param bgOffsetY   Offset Y background
     * @param bgMargin    Margin background
     * @param bgRadius    Radius background
     * @param orientation "portrait" atau "landscape"
     */
    public OverlayPreset(
            float posX, float posY, float size,
            int color,
            ShadowConfig shadow,
            boolean bgEnabled, int bgColor, int bgPadding,
            int bgOffsetX, int bgOffsetY, int bgMargin, int bgRadius,
            String orientation
    ) {
        this.posX = posX;
        this.posY = posY;
        this.size = size;
        this.color = color;
        this.shadow = (shadow != null) ? shadow : new ShadowConfig();
        this.bgEnabled = bgEnabled;
        this.bgColor = bgColor;
        this.bgPadding = bgPadding;
        this.bgOffsetX = bgOffsetX;
        this.bgOffsetY = bgOffsetY;
        this.bgMargin = bgMargin;
        this.bgRadius = bgRadius;
        this.orientation = orientation;
    }
}
