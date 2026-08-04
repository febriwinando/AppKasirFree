package tech.id.kasirapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Restaurant;

public class DashboardOwnerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_owner);

        AppDatabase db = DatabaseClient.getDatabase(this);

        Restaurant restaurant = db.restaurantDao().getRestaurant();

        if (restaurant != null) {
            Log.d("ROOM", "Nama = " + restaurant.name);
        } else {
            Log.d("ROOM", "Tidak ada data");
        }
    }
}