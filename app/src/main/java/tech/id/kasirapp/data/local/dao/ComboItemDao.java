package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import tech.id.kasirapp.data.local.entity.ComboItem;

@Dao
public interface ComboItemDao {
    @Insert
    void insertAll(List<ComboItem> items);

    @Query("SELECT * FROM combo_items WHERE comboId = :comboId")
    List<ComboItem> getByCombo(long comboId);

    @Query("DELETE FROM combo_items WHERE comboId = :comboId")
    void deleteByCombo(long comboId);
}