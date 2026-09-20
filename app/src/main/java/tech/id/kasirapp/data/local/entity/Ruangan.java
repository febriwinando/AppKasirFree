package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ruangan")
public class Ruangan {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String firebaseId;

    public long branchId;

    public String name;

    public int tableCount;

    public int syncStatus; // 0: Pending, 1: Success, 2: Failed
}