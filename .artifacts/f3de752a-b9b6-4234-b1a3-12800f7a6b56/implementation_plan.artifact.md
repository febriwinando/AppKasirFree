# Penambahan Menu ke Firestore di AddMenuActivity

Implementasi ini akan menyinkronkan data menu yang baru dibuat di `AddMenuActivity` ke koleksi Firestore berdasarkan `restaurantId` dan `branchId` yang diambil dari sesi pengguna.

## User Review Required

> [!IMPORTANT]
> - Pastikan koneksi internet tersedia saat melakukan penyimpanan jika ingin data langsung tersinkron ke Firestore.
> - Data akan disimpan ke koleksi `menus` di Firestore dengan struktur dokumen yang menyertakan metadata restoran dan cabang.

## Proposed Changes

### [Product Component]

#### [MODIFY] [AddMenuActivity.java](file:///Users/nebula/AndroidStudioProjects/KasirAPP/app/src/main/java/tech/id/kasirapp/product/AddMenuActivity.java)
- Inisialisasi `FirebaseFirestore`.
- Ambil data sesi (`AppSession`) dari database lokal untuk mendapatkan `restaurantId` dan `branchId`.
- Tambahkan logika penyimpanan ke Firestore di dalam metode `saveMenu()`.
- Update `syncStatus` di database lokal setelah sinkronisasi Firestore berhasil.

## Verification Plan

### Automated Tests
- Menjalankan aplikasi dan menambahkan menu baru.
- Memverifikasi entri baru di Firestore console pada koleksi `menus`.

### Manual Verification
- Melakukan login sebagai Manager atau Owner.
- Masuk ke fitur tambah menu.
- Mengisi data menu dan klik simpan.
- Cek apakah muncul pesan sukses dan verifikasi data di Firestore.
