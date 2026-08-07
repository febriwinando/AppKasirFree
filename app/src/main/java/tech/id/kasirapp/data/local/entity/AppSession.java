package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_session")
public class AppSession {
    @PrimaryKey
    public int id = 1;
    public long ownerId;
    public String uuid;
    public boolean isLoggedIn;

}