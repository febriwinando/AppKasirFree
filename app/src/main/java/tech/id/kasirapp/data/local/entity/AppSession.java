package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_session")
public class AppSession {

    @PrimaryKey
    public int id = 1;

    public long userId;

    public String uuid;

    public String role;

    public long ownerId;

    public long restaurantId;

    public long branchId;

    public String ownerFirebaseId;

    public String restaurantFirebaseId;

    public String branchFirebaseId;

    public boolean isLoggedIn;
}