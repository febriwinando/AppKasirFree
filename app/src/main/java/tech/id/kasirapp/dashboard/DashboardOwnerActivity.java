package tech.id.kasirapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tech.id.kasirapp.owner.ProfileActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Restaurant;

public class DashboardOwnerActivity extends AppCompatActivity {



    BottomNavigationView bottomNav;
    TextView tvGreeting, tvRestaurantName, txtOmzet, txtTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_owner);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        txtOmzet = findViewById(R.id.txtOmzet);
        txtTransaksi = findViewById(R.id.txtTransaksi);

        AppDatabase db = DatabaseClient.getDatabase(this);
        Restaurant restaurant = db.restaurantDao().getRestaurant();

        if (restaurant != null) {
            tvRestaurantName.setText(restaurant.name);
        }

        // Menerapkan Animasi Future Google
        Animation entrance = AnimationUtils.loadAnimation(this, R.anim.anim_liquid_entrance);

        View cardSummary = findViewById(R.id.cardSummary);
        if (cardSummary != null) cardSummary.startAnimation(entrance);

        View gridStats = findViewById(R.id.gridStats);
        if (gridStats != null) gridStats.startAnimation(entrance);

        View cardChart = findViewById(R.id.cardChart);
        if (cardChart != null) cardChart.startAnimation(entrance);

        if (tvGreeting != null) tvGreeting.startAnimation(entrance);
        if (tvRestaurantName != null) tvRestaurantName.startAnimation(entrance);

        View imgAvatar = findViewById(R.id.imgAvatar);
        if (imgAvatar != null) imgAvatar.startAnimation(entrance);

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