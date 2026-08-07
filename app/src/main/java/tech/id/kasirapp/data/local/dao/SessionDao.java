package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import tech.id.kasirapp.data.local.entity.AppSession;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppSession session);
    @Query("SELECT * FROM app_session WHERE id = 1")
    AppSession getSession();
    @Query("DELETE FROM app_session")
    void logout();

}