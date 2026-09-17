package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "menus")
public class Menu {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String firebaseId;

    public long branchId;

    public String name;

    public String type; // Makanan / Minuman

    public String category; // Snack, Coffee, etc.

    public String unit; // Porsi, Gelas, Pcs, dsb.

    public String sku; // Barcode / SKU

    public double costPrice; // Harga Modal (HPP)

    public double price; // Harga Jual

    public String description;

    public String imagePath;

    public int stock;

    public String status; // Aktif / Nonaktif

    public boolean isAvailable = true; // Tersedia / Habis

    public int syncStatus; // 0: Pending, 1: Success, 2: Failed
}