package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diskon")
public class Diskon {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String firebaseId;

    public long branchId;

    public long restaurantId;

    public long ownerId;

    public String name;

    public String type; // GLOBAL, ITEM

    public double value;

    public boolean isPercentage; // true: %, false: Rp

    public boolean isActive;

    public long menuId; // 0 if GLOBAL

    public String menuName; // To display without join

    public int syncStatus;
}