package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.KitchenStaff;

@Dao
public interface KitchenStaffDao {

    @Insert
    long insert(KitchenStaff kitchenStaff);

    @Update
    void update(KitchenStaff kitchenStaff);

    @Query(
            "SELECT * FROM kitchen_staff " +
                    "WHERE branchId = :branchId " +
                    "ORDER BY name ASC"
    )
    List<KitchenStaff> getByBranchId(long branchId);

    @Query(
            "SELECT * FROM kitchen_staff " +
                    "WHERE id = :id " +
                    "LIMIT 1"
    )
    KitchenStaff getById(long id);

    @Query(
            "SELECT * FROM kitchen_staff " +
                    "WHERE branchId = :branchId " +
                    "AND username = :username " +
                    "LIMIT 1"
    )
    KitchenStaff getByUsername(
            long branchId,
            String username
    );

    @Query(
            "SELECT COUNT(*) FROM kitchen_staff " +
                    "WHERE branchId = :branchId"
    )
    int countByBranchId(long branchId);

    @Query(
            "UPDATE kitchen_staff SET syncStatus = :status " +
                    "WHERE id = :id"
    )
    void updateSyncStatus(
            long id,
            int status
    );

    @Query(
            "DELETE FROM kitchen_staff WHERE id = :id"
    )
    void deleteById(long id);

    @Query(
            "DELETE FROM kitchen_staff WHERE branchId = :branchId"
    )
    void deleteByBranchId(long branchId);
}