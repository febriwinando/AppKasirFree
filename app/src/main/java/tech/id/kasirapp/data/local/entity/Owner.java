package tech.id.kasirapp.data.local.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "owners")
public class Owner {

    @PrimaryKey(autoGenerate = true)
    public long id;

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
    public int syncStatus;

}