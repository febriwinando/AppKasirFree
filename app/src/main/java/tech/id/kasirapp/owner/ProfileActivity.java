package tech.id.kasirapp.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.LoginActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.RestaurantActivity;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;


public class ProfileActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
    private AppDatabase db;
    MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        db = DatabaseClient.getDatabase(this);

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
        btnLogout = findViewById(R.id.btnLogout);

        cardRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pindahkeDaftarRestaurant = new Intent(ProfileActivity.this, RestaurantActivity.class);
                startActivity(pindahkeDaftarRestaurant);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
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

    private void logout() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Keluar dari akun?")
                .setMessage(
                        "Anda akan keluar dari akun ini pada perangkat."
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .setPositiveButton(
                        "Keluar",
                        (dialog, which) -> {

                            executor.execute(() -> {

                                // Hanya hapus session
                                db.sessionDao().logout();

                                runOnUiThread(() -> {

                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Berhasil keluar",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    Intent intent =
                                            new Intent(
                                                    ProfileActivity.this,
                                                    LoginActivity.class
                                            );

                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);

                                });

                            });

                        }
                )
                .show();
    }
}