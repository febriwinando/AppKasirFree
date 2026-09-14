package tech.id.kasirapp.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.login.LoginActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Owner;
import tech.id.kasirapp.data.local.entity.Restaurant;


public class ProfileActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvAvatar;
    private TextView tvOwnerName;
    private TextView tvUsername;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvRestaurantName;
    private MaterialCardView cardRestaurant;
    private AppDatabase db;
    MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
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
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
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
        executor.execute(() -> {
            AppSession session = db.sessionDao().getSession();
            if (session == null) return;

            Owner owner = db.ownerDao().getById(session.ownerId);
            Restaurant restaurant = db.restaurantDao().getRestaurant();

            runOnUiThread(() -> {
                if (owner != null) {
                    tvOwnerName.setText(owner.name != null ? owner.name : "-");
                    tvUsername.setText(owner.username != null ? "@" + owner.username : "@username");
                    tvFullName.setText(owner.name != null ? owner.name : "-");
                    tvEmail.setText(owner.email != null ? owner.email : "-");
                    tvPhone.setText(owner.phone != null ? owner.phone : "-");
                    
                    String avatarText = "U";
                    if (owner.name != null && !owner.name.isEmpty()) {
                        avatarText = owner.name.substring(0, 1).toUpperCase();
                    }
                    tvAvatar.setText(avatarText);
                }

                if (restaurant != null) {
                    tvRestaurantName.setText(restaurant.name);
                }

                // Menerapkan Animasi dengan ritme yang lebih baik
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

                tvAvatar.startAnimation(fadeIn);
                tvOwnerName.startAnimation(fadeIn);
                tvUsername.startAnimation(fadeIn);
                
                // Animasi bertahap untuk card
                View appBarLayout = findViewById(R.id.appBarLayout);
                if (appBarLayout != null) appBarLayout.startAnimation(fadeIn);

                View cardInfo = findViewById(R.id.cardInfo);
                if (cardInfo != null) cardInfo.startAnimation(slideUp);

                if (cardRestaurant != null) cardRestaurant.startAnimation(slideUp);
                
                // Mencari main card info (dia tidak punya ID, tapi kita bisa menemukannya)
                // Namun untuk keamanan, kita biarkan saja atau beri ID di XML nanti jika perlu.
                // Untuk sekarang, kita animasikan yang ada.
            });
        });
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