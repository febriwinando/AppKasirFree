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



import tech.id.kasirapp.owner.ProfileActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Restaurant;
import tech.id.kasirapp.owner.RestaurantActivity;

public class DashboardOwnerActivity extends AppCompatActivity {



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

        View cardMenuRestaurant = findViewById(R.id.cardMenuRestaurant);
        if (cardMenuRestaurant != null) {
            cardMenuRestaurant.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardOwnerActivity.this, RestaurantActivity.class);
                startActivity(intent);
            });
        }

        View cardHeaderProfile = findViewById(R.id.cardHeaderProfile);
        if (cardHeaderProfile != null) {
            cardHeaderProfile.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardOwnerActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}