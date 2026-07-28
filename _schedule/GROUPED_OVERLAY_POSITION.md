# Grouped Overlay Position

## Konsep
Beberapa overlay bisa digabung posisinya tanpa mengubah struktur modul yang sudah ada. User memilih kombinasi overlay yang ingin digabung, dan saat satu digeser, yang lain ikut.

## Contoh Penggunaan
- BatteryPercentage + BatteryTemperature digabung → selalu berdekatan
- BatteryCurrent sendirian → posisi bebas
- NetworkStats + Clock digabung → selalu berdekatan

## Cara Kerja

### 1. Group Definition
User membuat "group" di UI:
- Pilih beberapa modul untuk masuk ke satu group
- Beri nama group (opsional, misal: "Battery Stats")
- Set posisi awal group

### 2. Linked Position
Saat user menggeser salah satu overlay dalam group:
- Semua overlay lain dalam group yang sama ikut bergerak
- Offset antar overlay dalam group tetap (tidak berubah)
- Posisi tersimpan sebagai posisi group, bukan posisi individual

### 3. Storage
Simpan di SharedPreferences atau database:
```
group_battery = {
    modules: ["battery_percentage", "battery_temperature"],
    offsetX: [0, 0],    // offset dari posisi group
    offsetY: [0, 40],   // offset dari posisi group
    posX: 0.75,
    posY: 0.85
}
```

### 4. UI
- Di panel posisi setiap modul, tambahkan dropdown "Group"
- Opsi: "No Group", "Battery Stats", "Create New Group"
- Di MainActivity, tambahkan menu "Manage Groups" untuk edit/hapus group

## Kelebihan
- Tidak perlu refactor modul yang sudah ada
- Fleksibel: user pilih kombinasi mana yang digabung
- Offset antar overlay bisa diatur
- Bisa diubah kapan saja (pisah/gabung)

## Kompleksitas
- Sedang: perlu UI baru untuk manage groups
- Sedang: perlu update drag handler untuk handle group
- Rendah: tidak mengubah struktur modul

## File yang Perlu Ditambah
- `GroupManager.java` — Kelola group dan offset
- `group_config.xml` — UI manage groups
- Update `OverlayDragHandler` — Handle drag untuk group
- Update `PositionController` — Dropdown group selection
