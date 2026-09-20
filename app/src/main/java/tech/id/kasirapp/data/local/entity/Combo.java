package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "combos")
public class Combo {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String firebaseId;
    public long branchId;
    public String name;
    public double price;
    public String description;
    public String status; // Aktif / Nonaktif
    public int syncStatus;
}