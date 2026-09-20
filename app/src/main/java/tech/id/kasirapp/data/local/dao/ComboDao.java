package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import tech.id.kasirapp.data.local.entity.Combo;

@Dao
public interface ComboDao {
    @Insert
    long insert(Combo combo);

    @Update
    void update(Combo combo);

    @Delete
    void delete(Combo combo);

    @Query("SELECT * FROM combos WHERE branchId = :branchId")
    List<Combo> getByBranch(long branchId);

    @Query("SELECT * FROM combos WHERE id = :id LIMIT 1")
    Combo getById(long id);
}