package tech.id.kasirapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "combo_items")
public class ComboItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long comboId;
    public long menuId;
    public String menuName;
    public int quantity;
}