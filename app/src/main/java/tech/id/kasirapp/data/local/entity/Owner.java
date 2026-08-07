package tech.id.kasirapp.data.local.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "owners")
public class Owner {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // ID yang sama antara Room dan Firebase
    @ColumnInfo(name = "uuid")
    public String uuid;

    @ColumnInfo(name = "firebase_id")
    public String firebaseId;

    public String name;

    public String username;

    public String password;

    public String email;

    public String phone;

    /*
        0 = Belum Sync
        1 = Sudah Sync
        2 = Gagal Sync
    */
    @ColumnInfo(name = "sync_status")
    public int syncStatus;

}