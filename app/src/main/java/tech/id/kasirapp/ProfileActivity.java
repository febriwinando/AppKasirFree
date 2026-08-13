package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;


public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar;
    private TextView tvOwnerName;
    private TextView tvUsername;
    private TextView tvRole;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvRestaurantName;
    private TextView tvBranchName;
    MaterialCardView cardRestaurant;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();
        setupToolbar();
        loadProfile();

    }

    private void initView() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvBranchName = findViewById(R.id.tvBranchName);
        cardRestaurant = findViewById(R.id.cardRestaurant);


        cardRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pindahkeDaftarRestaurant = new Intent(ProfileActivity.this, RestaurantActivity.class);
                startActivity(pindahkeDaftarRestaurant);
            }
        });



    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

    }

    private void loadProfile() {

        // Sementara data dummy
        // Nanti diganti dengan data dari Room
        String name = "Nama Pemilik";
        String username = "@username";
        String email = "email@example.com";
        String phone = "08123456789";
        tvOwnerName.setText(name);
        tvUsername.setText(username);
        tvFullName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);
        tvAvatar.setText(
                name.substring(0, 1).toUpperCase()
        );

        tvRestaurantName.setText("Nama Restoran");
        tvBranchName.setText("Cabang Utama");

    }

}