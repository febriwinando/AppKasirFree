package tech.id.kasirapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Restaurant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditRestaurantActivity extends AppCompatActivity {

    private TextInputEditText edtNamaRestoran;
    private TextInputEditText edtPemilik;
    private TextInputEditText edtTelepon;
    private TextInputEditText edtEmail;
    private MaterialButton btnSimpan;

    private AppDatabase db;

    private long restaurantId;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_restaurant);

        initView();

        db = DatabaseClient.getDatabase(this);

        restaurantId =
                getIntent().getLongExtra(
                        "restaurant_id",
                        -1
                );

        if (restaurantId == -1) {

            Toast.makeText(
                    this,
                    "Data restoran tidak ditemukan",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        setupToolbar();

        loadRestaurant();

        btnSimpan.setOnClickListener(v -> {

            updateRestaurant();

        });
    }


    private void initView() {

        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        edtNamaRestoran =
                findViewById(R.id.edtNamaRestoran);

        edtPemilik =
                findViewById(R.id.edtPemilik);

        edtTelepon =
                findViewById(R.id.edtTelepon);

        edtEmail =
                findViewById(R.id.edtEmail);

        btnSimpan =
                findViewById(R.id.btnSimpan);
    }


    private void setupToolbar() {

        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> {

            finish();

        });
    }


    private void loadRestaurant() {

        executor.execute(() -> {

            Restaurant restaurant =
                    db.restaurantDao()
                            .getById(restaurantId);

            runOnUiThread(() -> {

                if (restaurant == null) {

                    Toast.makeText(
                            this,
                            "Restoran tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                    return;
                }


                edtNamaRestoran.setText(
                        restaurant.name
                );

                edtPemilik.setText(
                        restaurant.ownerName
                );

                edtTelepon.setText(
                        restaurant.phone
                );

                edtEmail.setText(
                        restaurant.email
                );
            });

        });
    }


    private void updateRestaurant() {

        String nama =
                edtNamaRestoran
                        .getText()
                        .toString()
                        .trim();

        String pemilik =
                edtPemilik
                        .getText()
                        .toString()
                        .trim();

        String telepon =
                edtTelepon
                        .getText()
                        .toString()
                        .trim();

        String email =
                edtEmail
                        .getText()
                        .toString()
                        .trim();



        // Validasi

        if (nama.isEmpty()) {

            edtNamaRestoran.setError(
                    "Nama restoran wajib diisi"
            );

            edtNamaRestoran.requestFocus();

            return;
        }


        btnSimpan.setEnabled(false);


        executor.execute(() -> {

            Restaurant restaurant =
                    db.restaurantDao()
                            .getById(restaurantId);

            if (restaurant == null) {

                runOnUiThread(() -> {

                    btnSimpan.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Restoran tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                });

                return;
            }


            // Update data lokal

            restaurant.name = nama;

            restaurant.ownerName = pemilik;

            restaurant.phone = telepon;

            restaurant.email = email;
            // Data berubah sehingga perlu
            // disinkronkan kembali

            restaurant.syncStatus = 0;


            db.restaurantDao().update(restaurant);


            runOnUiThread(() -> {

                syncFirebase(restaurant);

            });

        });
    }


    private void syncFirebase(Restaurant restaurant) {

        FirebaseRepository firebase =
                new FirebaseRepository();


        firebase.saveRestaurant(
                restaurant.firebaseId,
                restaurant.name,
                restaurant.ownerName,
                restaurant.phone,
                restaurant.email,
                restaurant.ownerId,
                restaurant.isActive,
                new FirebaseRepository.OnCompleteListener() {

                    @Override
                    public void success() {

                        executor.execute(() -> {

                            db.restaurantDao()
                                    .updateSyncStatus(
                                            restaurant.id,
                                            1
                                    );

                            runOnUiThread(() -> {

                                btnSimpan.setEnabled(true);

                                Toast.makeText(
                                        EditRestaurantActivity.this,
                                        "Restoran berhasil diperbarui",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                            });

                        });
                    }


                    @Override
                    public void failed(String error) {

                        executor.execute(() -> {

                            db.restaurantDao()
                                    .updateSyncStatus(
                                            restaurant.id,
                                            2
                                    );

                            runOnUiThread(() -> {

                                btnSimpan.setEnabled(true);

                                Toast.makeText(
                                        EditRestaurantActivity.this,
                                        "Tersimpan di perangkat, tetapi gagal sinkron ke server",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                            });

                        });
                    }

                }
        );
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();

    }
}