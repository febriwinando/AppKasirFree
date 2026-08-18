package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "branches")
public class Branch {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String firebaseId;
    public long restaurantFirebaseId;
    public long restaurantId;
    public String name;
    public String address;
    public String phone;
    public String openTime;
    public String closeTime;
    public boolean isMain;
    public int syncStatus;

}