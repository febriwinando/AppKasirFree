package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Restaurant;

public class DashboardOwnerActivity extends AppCompatActivity {



    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_owner);

        AppDatabase db = DatabaseClient.getDatabase(this);
        Restaurant restaurant = db.restaurantDao().getRestaurant();

        if (restaurant != null) {
            Log.d("ROOM", "Nama = " + restaurant.name);
        } else {
            Log.d("ROOM", "Tidak ada data");
        }
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {

                // Sudah di Dashboard
                return true;

            } else if (id == R.id.order) {

//                Intent intent = new Intent(
//                        DashboardOwnerActivity.this,
//                        OrderActivity.class
//                );
//
//                startActivity(intent);
                return true;

            } else if (id == R.id.report) {

//                Intent intent = new Intent(
//                        DashboardOwnerActivity.this,
//                        ReportActivity.class
//                );
//
//                startActivity(intent);
                return true;

            } else if (id == R.id.profile) {

                Intent intent = new Intent(
                        DashboardOwnerActivity.this,
                        ProfileActivity.class
                );

                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}