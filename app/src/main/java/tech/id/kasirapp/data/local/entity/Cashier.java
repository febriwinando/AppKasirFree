package tech.id.kasirapp.data.local.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cashiers")
public class Cashier {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // ID dokumen Firebase
    public String firebaseId;

    // Cabang tempat kasir bekerja
    public long branchId;

    // Identitas
    public String name;

    public String username;

    public String password;

    public String phone;

    // Status sinkronisasi
    // 0 = belum sync
    // 1 = berhasil sync
    // 2 = gagal sync

    public int syncStatus;
}
