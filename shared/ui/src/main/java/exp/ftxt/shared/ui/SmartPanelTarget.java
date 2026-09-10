package exp.ftxt.shared.ui;

/**
 * Kontrak untuk modul overlay yang bisa dikontrol oleh Smart Panel.
 * Semua posisi dalam bentuk normalisasi (0–1 fraksi layar).
 */
public interface SmartPanelTarget {

    /** Nama tampil, mis. "Crosshair", "FPS Display". */
    String getTitle();

    /** Status aktif (running) modul. */
    boolean isEnabled();

    // ── Posisi ──

    /** Posisi X normalisasi (0 = kiri, 1 = kanan). */
    float getPosX();

    /** Posisi Y normalisasi (0 = atas, 1 = bawah). */
    float getPosY();

    /** Geser posisi relatif (delta normalisasi). */
    void moveBy(float dxNorm, float dyNorm);

    /** Kembalikan posisi ke tengah layar. */
    void resetPosition();

    // ── Lock ──

    /** true = touch passthrough aktif (posisi tidak bisa digeser langsung). */
    boolean isPositionLocked();

    void setPositionLocked(boolean locked);

    // ── Kontrol spesifik (opsional, default tidak didukung) ──

    default boolean canSetOpacity() { return false; }
    default int getOpacity() { return 100; }
    default void setOpacity(int percent) {}

    default boolean canSetSize() { return false; }
    default float getSize() { return 0f; }
    default void setSize(float size) {}

    default boolean canResetConfig() { return false; }
    default void resetConfig() {}
}
