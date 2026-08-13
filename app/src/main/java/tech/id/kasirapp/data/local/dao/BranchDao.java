package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import tech.id.kasirapp.data.local.entity.Branch;


@Dao
public interface BranchDao {

    @Insert
    long insert(Branch branch);

    @Update
    void update(Branch branch);

    @Query(" SELECT * FROM branches WHERE restaurantId = :restaurantId ORDER BY isMain DESC, name ASC ")
    List<Branch> getByRestaurant(
            long restaurantId
    );

    @Query("SELECT * FROM branches WHERE id = :id  LIMIT 1")
    Branch getById(long id);

    @Query("UPDATE branches SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(
            long id,
            int status
    );

    @Query("DELETE FROM branches WHERE id = :id")
    void deleteById(long id);

    @Query("UPDATE branches SET firebaseId=:firebaseId,syncStatus=:status WHERE id=:id")
    void updateFirebase(long id, String firebaseId, int status);

    @Query("DELETE FROM branches WHERE restaurantId = :restaurantId")
    void deleteByRestaurant(long restaurantId);

    @Query("SELECT COUNT(*) FROM branches WHERE restaurantId = :restaurantId AND isMain = 1")
    int countMainBranch(long restaurantId);

    @Query("SELECT * FROM branches WHERE restaurantId = :restaurantId")
    List<Branch> getByRestaurantId(long restaurantId);
}