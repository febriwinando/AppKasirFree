package tech.id.kasirapp.register;


import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import tech.id.kasirapp.util.StatusHelper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;
import java.util.UUID;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Restaurant;


public class RegisterBranchActivity extends AppCompatActivity {


    TextInputEditText edtNamaCabang;
    TextInputEditText edtAlamatCabang;
    TextInputEditText edtTeleponCabang;
    TextInputEditText edtJamBuka;
    TextInputEditText edtJamTutup;

    MaterialButton btnSimpan;
    TextView tvNamaRestoranHeader, tvNamaOwnerHeader;
    View cardCabangUtama;
    
    private long restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_branch);

        restaurantId = getIntent().getLongExtra("restaurant_id", 0);

        edtNamaCabang = findViewById(R.id.edtNamaCabang);
        edtAlamatCabang = findViewById(R.id.edtAlamatCabang);
        edtTeleponCabang = findViewById(R.id.edtTeleponCabang);
        edtJamBuka = findViewById(R.id.edtJamBuka);
        edtJamTutup = findViewById(R.id.edtJamTutup);
        btnSimpan = findViewById(R.id.btnSimpan);
        cardCabangUtama = findViewById(R.id.cardCabangUtama);

        tvNamaRestoranHeader = findViewById(R.id.tvNamaRestoranHeader);
        tvNamaOwnerHeader = findViewById(R.id.tvNamaOwnerHeader);

        setupToolbar();

        // Keyboard Handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerRegisterBranch), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(dpToPx(24), dpToPx(32), dpToPx(24), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        loadRestaurantInfo();
        checkMainBranch();

        btnSimpan.setOnClickListener(v -> {
            if (edtNamaCabang.getText().toString().trim().isEmpty()) {

                edtNamaCabang.setError("Nama cabang wajib diisi");

                return;

            }

            if (edtJamBuka.getText().toString().trim().isEmpty()) {

                edtJamBuka.setError("Jam buka wajib dipilih");

                return;

            }

            if (edtJamTutup.getText().toString().trim().isEmpty()) {

                edtJamTutup.setError("Jam tutup wajib dipilih");

                return;

            }

            simpanData();
        });

        edtJamBuka.setOnClickListener(v -> {
            showTimePicker(true);
        });

        edtJamTutup.setOnClickListener(v -> {
            showTimePicker(false);
        });
    }

    private void setupToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbar).setNavigationOnClickListener(v -> finish());
        }
    }

    private void showTimePicker(boolean jamBuka) {

        int hour = 8;
        int minute = 0;

        // Jika sebelumnya sudah dipilih,
        // gunakan waktu tersebut sebagai default
        TextInputEditText target =
                jamBuka ? edtJamBuka : edtJamTutup;

        if (target.getText() != null &&
                !target.getText().toString().isEmpty()) {

            String[] waktu =
                    target.getText().toString().split(":");

            try {
                hour = Integer.parseInt(waktu[0]);
                minute = Integer.parseInt(waktu[1]);
            } catch (Exception ignored) {
            }
        }

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(hour)
                        .setMinute(minute)
                        .setTitleText(
                                jamBuka
                                        ? "Pilih Jam Buka"
                                        : "Pilih Jam Tutup"
                        )
                        .setInputMode(
                                MaterialTimePicker.INPUT_MODE_CLOCK
                        )
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {

            String waktu = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    picker.getHour(),
                    picker.getMinute()
            );

            target.setText(waktu);
        });

        picker.show(
                getSupportFragmentManager(),
                jamBuka
                        ? "TIME_PICKER_BUKA"
                        : "TIME_PICKER_TUTUP"
        );
    }

    private void loadRestaurantInfo() {
        AppDatabase db = DatabaseClient.getDatabase(this);
        new Thread(() -> {
            Restaurant restaurant = db.restaurantDao().getById(restaurantId);
            runOnUiThread(() -> {
                if (restaurant != null) {
                    tvNamaRestoranHeader.setText(restaurant.name);
                    tvNamaOwnerHeader.setText("Pemilik: " + restaurant.ownerName);

                    // Menerapkan Animasi setelah data dimuat
                    Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

                    View cardForm = findViewById(R.id.cardForm);
                    if (cardForm != null) cardForm.startAnimation(slideUp);

                    View cardInfo = findViewById(R.id.cardInfo);
                    if (cardInfo != null) cardInfo.startAnimation(slideUp);

                    View imgHeader = findViewById(R.id.imgHeader);
                    if (imgHeader != null) imgHeader.startAnimation(fadeIn);

                    View tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
                    if (tvHeaderTitle != null) tvHeaderTitle.startAnimation(fadeIn);

                    View tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
                    if (tvHeaderSubtitle != null) tvHeaderSubtitle.startAnimation(fadeIn);
                }
            });
        }).start();
    }

    private void checkMainBranch() {
        AppDatabase db =
                DatabaseClient.getDatabase(this);

        new Thread(() -> {

            int jumlahCabangUtama =
                    db.branchDao()
                            .countMainBranch(restaurantId);

            runOnUiThread(() -> {

                if (jumlahCabangUtama > 0) {
                    cardCabangUtama.setVisibility(View.GONE);
                } else {
                    cardCabangUtama.setVisibility(View.VISIBLE);
                }

            });

        }).start();
    }
    private void simpanData() {

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        new Thread(() -> {
            Restaurant restaurant =
                    db.restaurantDao().getById(restaurantId);

            if (restaurant == null) {
                runOnUiThread(() -> StatusHelper.showError(
                        this,
                        "Kesalahan",
                        "Data restoran tidak ditemukan",
                        null
                ));
                return;
            }

            int jumlahCabangUtama =
                    db.branchDao()
                            .countMainBranch(restaurantId);

            boolean isMain = jumlahCabangUtama == 0;

            Branch branch = new Branch();
            branch.restaurantId = restaurantId;
            branch.name = edtNamaCabang.getText().toString().trim();
            branch.address = edtAlamatCabang.getText().toString().trim();
            branch.phone = edtTeleponCabang.getText().toString().trim();
            branch.openTime = edtJamBuka.getText().toString().trim();
            branch.closeTime = edtJamTutup.getText().toString().trim();
            branch.isMain = isMain;
            branch.syncStatus = 0;

            String firebaseId = UUID.randomUUID().toString();
            branch.firebaseId = firebaseId;

            long id = db.branchDao().insert(branch);

            runOnUiThread(() -> StatusHelper.showLoading(this, "Linking operational branch..."));

            FirebaseRepository firebase = new FirebaseRepository();
            firebase.saveBranch(
                    firebaseId,
                    restaurant.firebaseId,
                    branch.name,
                    branch.restaurantId,
                    branch.address,
                    branch.phone,
                    branch.openTime,
                    branch.closeTime,
                    branch.isMain,
                    new FirebaseRepository.OnCompleteListener() {
                        @Override
                        public void success() {
                            new Thread(() -> {
                                db.branchDao().updateSyncStatus(id, 1);
                                runOnUiThread(() -> {
                                    StatusHelper.hideLoading();
                                    StatusHelper.showSuccess(RegisterBranchActivity.this, "Berhasil", isMain ? "Cabang utama berhasil dibuat" : "Cabang berhasil ditambahkan", () -> finish());
                                });
                            }).start();
                        }

                        @Override
                        public void failed(String error) {
                            new Thread(() -> {
                                db.branchDao().updateSyncStatus(id, 2);
                                runOnUiThread(() -> {
                                    StatusHelper.hideLoading();
                                    StatusHelper.showError(RegisterBranchActivity.this, "Gagal", "Cabang disimpan lokal, sinkronisasi gagal", () -> finish());
                                });
                            }).start();
                        }
                    }
            );
        }).start();
    }
//
//    private void simpanData(){
//
//
//        AppDatabase db = DatabaseClient.getDatabase(this);
//        Branch branch = new Branch();
//
//        branch.restaurantId = getIntent().getLongExtra("restaurant_id", 0);
//        branch.name = edtNamaCabang.getText().toString();
//        branch.address = edtAlamatCabang.getText().toString();
//        branch.phone = edtTeleponCabang.getText().toString();
//        branch.openTime = edtJamBuka.getText().toString();
//
//        branch.closeTime = edtJamTutup.getText().toString();
//        branch.isMain=true;
//        branch.syncStatus=0;
//
//        String firebaseId = UUID.randomUUID().toString();
//
//        branch.firebaseId = firebaseId;
//        branch.syncStatus = 0;
//
//        long id = db.branchDao().insert(branch);
//        long restaurantId = getIntent().getLongExtra("restaurant_id", 0);
//
//        Restaurant restaurant = db.restaurantDao().getById(restaurantId);
//
//        if (restaurant == null) {
//            Toast.makeText(this, "Restaurant tidak ditemukan", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseRepository firebase =
//                new FirebaseRepository();
//
//        firebase.saveBranch(
//                firebaseId,
//                restaurant.firebaseId,
//                branch.name,
//                branch.address,
//                branch.phone,
//                branch.openTime,
//                branch.closeTime,
//                branch.isMain,
//                new FirebaseRepository.OnCompleteListener() {
//
//                    @Override
//                    public void success() {
//
//                        db.branchDao().updateSyncStatus(id, 1);
//                    }
//
//                    @Override
//                    public void failed(String error) {
//
//                        db.branchDao()
//                                .updateSyncStatus(id, 2);
//
//                        Toast.makeText(
//                                RegisterBranchActivity.this,
//                                "Cabang disimpan lokal, sinkronisasi gagal",
//                                Toast.LENGTH_LONG
//                        ).show();
//
//                        startActivity(
//                                new Intent(
//                                        RegisterBranchActivity.this,
//                                        DashboardOwnerActivity.class
//                                )
//                        );
//
//                        finish();
//                    }
//                }
//        );
//
//        Toast.makeText(
//                this,
//                "Restoran berhasil dibuat",
//                Toast.LENGTH_SHORT
//        ).show();
//
//
//        startActivity(
//                new Intent(
//                        this,
//                        DashboardOwnerActivity.class
//                )
//        );
//
//
//        finish();
//
//
//    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}