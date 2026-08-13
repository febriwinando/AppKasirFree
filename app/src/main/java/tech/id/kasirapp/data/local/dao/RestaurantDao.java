package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tech.id.kasirapp.data.local.entity.Restaurant;


@Dao
public interface RestaurantDao {
    @Insert
    long insert(Restaurant restaurant);
    @Query("SELECT * FROM restaurants LIMIT 1")
    Restaurant getRestaurant();
    @Query("UPDATE restaurants SET syncStatus=:status WHERE id=:id")
    void updateSyncStatus(
            long id,
            int status
    );
    @Query("SELECT * FROM restaurants WHERE id=:id LIMIT 1")
    Restaurant getById(long id);

    @Update
    void update(Restaurant restaurant);

    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    List<Restaurant> getAll();

    @Query("SELECT * FROM restaurants WHERE ownerId = :ownerId AND isActive = 1 ORDER BY name ASC")
    List<Restaurant> getActiveRestaurants(
            String ownerId
    );

    @Delete
    void delete(Restaurant restaurant);


}