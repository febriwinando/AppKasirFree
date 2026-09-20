package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Ruangan;

@Dao
public interface RuanganDao {

    @Insert
    long insert(Ruangan ruangan);

    @Update
    void update(Ruangan ruangan);

    @Delete
    void delete(Ruangan ruangan);

    @Query("SELECT * FROM ruangan WHERE branchId = :branchId")
    List<Ruangan> getByBranch(long branchId);

    @Query("SELECT * FROM ruangan WHERE id = :id LIMIT 1")
    Ruangan getById(long id);

    @Query("DELETE FROM ruangan WHERE branchId = :branchId")
    void deleteByBranch(long branchId);
}