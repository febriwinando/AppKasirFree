package tech.id.kasirapp.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import tech.id.kasirapp.data.local.entity.Owner;


@Dao
public interface OwnerDao {

    @Insert
    long insert(Owner owner);

    @Query("SELECT * FROM owners LIMIT 1")
    Owner getOwner();

    @Query("SELECT * FROM owners WHERE username=:username LIMIT 1")
    Owner getByUsername(String username);

    @Query("SELECT * FROM owners WHERE id=:id LIMIT 1")
    Owner getById(long id);

    @Query("UPDATE owners SET syncStatus=:status WHERE id=:id")
    void updateSyncStatus(long id,int status);

    @Query("UPDATE owners SET firebaseId=:firebaseId,syncStatus=:status WHERE id=:id")
    void updateFirebase(long id,String firebaseId,int status);

}