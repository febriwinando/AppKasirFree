package tech.id.kasirapp.data.local.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "owners")
public class Owner {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // ID dokumen / UUID Firebase
    public String firebaseId;

    public String name;

    public String username;

    public String password;

    public String email;

    public String phone;

    public int syncStatus;
}