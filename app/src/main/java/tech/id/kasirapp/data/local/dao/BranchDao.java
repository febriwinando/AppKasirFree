package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import tech.id.kasirapp.data.local.entity.Branch;
@Dao
public interface BranchDao {
    @Insert
    long insert(Branch branch);
    @Query("UPDATE branches SET syncStatus=:status WHERE id=:id")
    void updateSyncStatus(long id, int status);

    @Query("UPDATE branches SET firebaseId=:firebaseId,syncStatus=:status WHERE id=:id")
    void updateFirebase(long id, String firebaseId, int status);

}