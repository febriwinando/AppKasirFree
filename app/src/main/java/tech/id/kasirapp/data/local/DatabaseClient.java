package tech.id.kasirapp.data.local;
import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {
    private static AppDatabase database;
    public static AppDatabase getDatabase(Context context){
        if(database==null){
            database = Room.databaseBuilder(context, AppDatabase.class, "kasir.db").allowMainThreadQueries().build();
        }

        return database;
    }
}