package tech.id.kasirapp.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Waiter;

@Dao
public interface WaiterDao {

    @Insert
    long insert(Waiter waiter);

    @Update
    void update(Waiter waiter);

    @Query(
            "SELECT * FROM waiters " +
                    "WHERE branchId = :branchId " +
                    "ORDER BY name ASC"
    )
    List<Waiter> getByBranchId(long branchId);

    @Query(
            "SELECT * FROM waiters " +
                    "WHERE id = :id " +
                    "LIMIT 1"
    )
    Waiter getById(long id);

    @Query("SELECT * FROM waiters " +
                    "WHERE branchId = :branchId " +
                    "AND username = :username " +
                    "LIMIT 1")
    Waiter getByUsername(
            long branchId,
            String username
    );

    @Query("SELECT COUNT(*) FROM waiters WHERE branchId = :branchId")
    int countByBranchId(long branchId);

    @Query("UPDATE waiters SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(
            long id,
            int status
    );

    @Query("DELETE FROM waiters WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM waiters WHERE branchId = :branchId")
    void deleteByBranchId(long branchId);

}
