package tech.id.kasirapp.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Cashier;

@Dao
public interface CashierDao {

    @Insert
    long insert(Cashier cashier);

    @Update
    void update(Cashier cashier);

    @Query("SELECT * FROM cashiers " +
                    "WHERE branchId = :branchId " +
                    "ORDER BY name ASC")
    List<Cashier> getByBranchId(long branchId);

    @Query("SELECT * FROM cashiers " +
                    "WHERE id = :id " +
                    "LIMIT 1")
    Cashier getById(long id);

    @Query("SELECT * FROM cashiers " +
                    "WHERE branchId = :branchId " +
                    "AND username = :username " +
                    "LIMIT 1")
    Cashier getByUsername(
            long branchId,
            String username
    );

    @Query("SELECT COUNT(*) FROM cashiers " +
                    "WHERE branchId = :branchId")
    int countByBranchId(long branchId);

    @Query("UPDATE cashiers SET syncStatus = :status " +
                    "WHERE id = :id")
    void updateSyncStatus(
            long id,
            int status);

    @Query("DELETE FROM cashiers WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM cashiers WHERE branchId = :branchId")
    void deleteByBranchId(long branchId);

}