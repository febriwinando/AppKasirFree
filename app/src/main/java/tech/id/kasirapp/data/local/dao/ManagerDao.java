package tech.id.kasirapp.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import tech.id.kasirapp.data.local.entity.Manager;

@Dao
public interface ManagerDao {

    @Insert
    long insert(Manager manager);

    @Query("SELECT * FROM managers WHERE branchId = :branchId LIMIT 1")
    Manager getByBranchId(long branchId);

    @Query("SELECT COUNT(*) FROM managers WHERE branchId = :branchId")
    int countByBranchId(long branchId);

    @Query("UPDATE managers SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(
            long id,
            int status
    );

    @Query("SELECT * FROM managers WHERE id = :id LIMIT 1")
    Manager getById(long id);

    @Update
    void update(Manager manager);



    @Query("DELETE FROM managers WHERE branchId = :branchId")
    void deleteByBranchId(long branchId);
}