package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "branches")
public class Branch {

    @PrimaryKey(autoGenerate = true)
    public long id;
    // ID dokumen Firestore
    public String firebaseId;

    // Relasi ke restoran
    public long restaurantFirebaseId;

    // Informasi cabang
    public String name;
    public long restaurantId;
    public String address;

    public String phone;

    public String openTime;

    public String closeTime;

    // Cabang utama
    public boolean isMain;


    // =========================================================
    // PENGATURAN TRANSAKSI
    // =========================================================

    // Pajak dalam persen
    // Contoh: 11.0 = 11%
    public double tax;

    // Service charge dalam persen
    // Contoh: 5.0 = 5%
    public double serviceCharge;

    // Jumlah meja pada cabang
    public int jumlahMeja;

    // =========================================================
    // METODE PENJUALAN
    // =========================================================

    // Dine In
    public boolean dineIn;
    // Take Away
    public boolean takeAway;
    // Delivery
    public boolean delivery;
    // =========================================================
    // PENGATURAN OPERASIONAL
    // =========================================================
    // Pesanan dikirim ke dapur
    public boolean sendToKitchen;
    // Stok otomatis berkurang
    public boolean automaticStock;

    // Boleh stok menjadi minus
    public boolean allowNegativeStock;
    // =========================================================
    // SYNCHRONIZATION
    // =========================================================

    /*
        0 = Belum Sync
        1 = Sudah Sync
        2 = Gagal Sync
    */
    public int syncStatus;

}