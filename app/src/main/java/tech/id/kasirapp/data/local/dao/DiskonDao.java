package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Diskon;

@Dao
public interface DiskonDao {

    @Insert
    long insert(Diskon diskon);

    @Update
    void update(Diskon diskon);

    @Delete
    void delete(Diskon diskon);

    @Query("SELECT * FROM diskon WHERE branchId = :branchId")
    List<Diskon> getByBranch(long branchId);

    @Query("SELECT * FROM diskon WHERE id = :id LIMIT 1")
    Diskon getById(long id);

    @Query("SELECT * FROM diskon WHERE branchId = :branchId AND type = 'GLOBAL' AND isActive = 1 LIMIT 1")
    Diskon getActiveGlobal(long branchId);

    @Query("DELETE FROM diskon WHERE branchId = :branchId")
    void deleteByBranch(long branchId);
}