package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Menu;

@Dao
public interface MenuDao {

    @Insert
    long insert(Menu menu);

    @Update
    void update(Menu menu);

    @Delete
    void delete(Menu menu);

    @Query("SELECT * FROM menus WHERE branchId = :branchId")
    List<Menu> getByBranch(long branchId);

    @Query("SELECT * FROM menus WHERE id = :id LIMIT 1")
    Menu getById(long id);

    @Query("DELETE FROM menus WHERE branchId = :branchId")
    void deleteByBranch(long branchId);

    @Query("SELECT COUNT(*) FROM menus WHERE branchId = :branchId")
    int countByBranch(long branchId);
}