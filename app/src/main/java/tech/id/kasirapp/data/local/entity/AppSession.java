package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_session")
public class AppSession {

    @PrimaryKey
    public int id = 1;

    // Owner
    public long ownerId;

    public String uuid;

    // Login
    public boolean isLoggedIn;

    // Restoran aktif
    public long restaurantId;

    // Cabang aktif
    public long branchId;

    // Role pengguna
    public String role;
}