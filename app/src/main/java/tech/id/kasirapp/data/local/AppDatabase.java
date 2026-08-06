package tech.id.kasirapp.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import tech.id.kasirapp.data.local.dao.*;
import tech.id.kasirapp.data.local.entity.*;

@Database(entities={
        Restaurant.class,
        Branch.class,
        Owner.class},
        version=2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RestaurantDao restaurantDao();
    public abstract BranchDao branchDao();
    public abstract OwnerDao ownerDao();
}