package tech.id.kasirapp.data.local.entity;



import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "restaurants")

public class Restaurant {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String firebaseId;
    public String name;
    public String ownerName;
    public String phone;
    public String email;
    public String address;
    public long createdAt;

    /*
        0 = belum sync
        1 = sudah sync
        2 = gagal sync
    */
    public int syncStatus;
}