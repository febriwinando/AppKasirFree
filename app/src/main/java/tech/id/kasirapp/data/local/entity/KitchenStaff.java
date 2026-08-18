package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "kitchen_staff")
public class KitchenStaff {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String firebaseId;
    // ID Firebase cabang
    public String branchFirebaseId;
    public long branchId;
    public String name;
    public String username;
    public String password;
    public String phone;
    public int syncStatus;
}