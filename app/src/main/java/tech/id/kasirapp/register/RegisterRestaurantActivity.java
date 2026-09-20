package tech.id.kasirapp.register;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import tech.id.kasirapp.util.StatusHelper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Owner;
import tech.id.kasirapp.data.local.entity.Restaurant;


public class RegisterRestaurantActivity extends AppCompatActivity {
    TextInputEditText edtNamaRestoran;
    TextInputEditText edtPemilik;
    TextInputEditText edtTelepon;
    TextInputEditText edtEmail;
    MaterialButton btnLanjutCabang;
    AppSession session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_restaurant);

        AppDatabase db = DatabaseClient.getDatabase(this);

        session = db.sessionDao().getSession();

        edtNamaRestoran = findViewById(R.id.edtNamaRestoran);
        edtPemilik = findViewById(R.id.edtPemilik);
        edtTelepon = findViewById(R.id.edtTelepon);
        edtEmail = findViewById(R.id.edtEmail);
        btnLanjutCabang = findViewById(R.id.btnLanjutCabang);

        setupToolbar();

        // Keyboard Handling
        View scrollView = findViewById(R.id.scrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        // Menerapkan Animasi
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        View cardForm = findViewById(R.id.cardForm);
        if (cardForm != null) cardForm.startAnimation(slideUp);

        View imgHeader = findViewById(R.id.imgHeader);
        if (imgHeader != null) imgHeader.startAnimation(fadeIn);

        View tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.startAnimation(fadeIn);

        View tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.startAnimation(fadeIn);

        loadOwnerData(db);

        btnLanjutCabang.setOnClickListener(v -> {
            if(
                    edtNamaRestoran.getText()
                            .toString()
                            .isEmpty()
            ){edtNamaRestoran.setError(
                        "Nama restoran wajib diisi"
                );
                return;

            }



            Restaurant restaurant =
                    new Restaurant();



            restaurant.name =
                    edtNamaRestoran
                            .getText()
                            .toString();



            restaurant.ownerName =
                    edtPemilik
                            .getText()
                            .toString();



            restaurant.phone =
                    edtTelepon
                            .getText()
                            .toString();

            restaurant.email =
                    edtEmail
                            .getText()
                            .toString();

            restaurant.createdAt =
                    System.currentTimeMillis();



    /*
        buat ID Firebase
    */
            String firebaseId = UUID.randomUUID().toString();
            restaurant.firebaseId = firebaseId;
            restaurant.syncStatus = 0;

            long restaurantId = db.restaurantDao().insert(restaurant);

            StatusHelper.showLoading(this, "Initializing business node...");

            FirebaseRepository firebase =
                    new FirebaseRepository();



            firebase.saveRestaurant(
                    firebaseId,
                    restaurant.name,
                    restaurant.ownerName,
                    restaurant.phone,
                    restaurant.email,
                    restaurant.ownerId = session.ownerId,
                    restaurant.isActive = true,
                    restaurant.createdAt,
                    restaurant.syncStatus,
                    new FirebaseRepository.OnCompleteListener(){

                        @Override
                        public void success(){
                            restaurant.syncStatus=1;
                            db.restaurantDao()
                                    .updateSyncStatus(
                                            restaurantId,
                                            1
                                    );

                            runOnUiThread(() -> {
                                StatusHelper.hideLoading();
                                StatusHelper.showSuccess(RegisterRestaurantActivity.this, "Berhasil", "Restoran berhasil disimpan", () -> finish());
                            });
                        }

                        @Override
                        public void failed(
                                String error
                        ){
                            runOnUiThread(() -> {
                                StatusHelper.hideLoading();
                                StatusHelper.showError(RegisterRestaurantActivity.this, "Gagal", "Restoran disimpan lokal, sinkronisasi gagal", () -> finish());
                            });
                        }

                    }
            );

        });

    }

    private void setupToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbar).setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadOwnerData(AppDatabase db) {
        new Thread(() -> {
            if (session != null) {
                Owner owner = db.ownerDao().getById(session.ownerId);
                runOnUiThread(() -> {
                    if (owner != null) {
                        edtPemilik.setText(owner.name);
                        edtEmail.setText(owner.email);
                        edtTelepon.setText(owner.phone);
                    }
                });
            }
        }).start();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}