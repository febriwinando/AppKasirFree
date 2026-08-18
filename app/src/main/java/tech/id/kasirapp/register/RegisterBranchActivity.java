package tech.id.kasirapp.register;


import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

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
    String namaRestoran;
    String pemilik;
    private MaterialCardView cardCabangUtama;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_branch);

        namaRestoran = getIntent().getStringExtra("nama_restoran");
        pemilik = getIntent().getStringExtra("pemilik");
        edtNamaCabang = findViewById(R.id.edtNamaCabang);
        edtAlamatCabang = findViewById(R.id.edtAlamatCabang);
        edtTeleponCabang = findViewById(R.id.edtTeleponCabang);
        edtJamBuka = findViewById(R.id.edtJamBuka);
        edtJamTutup = findViewById(R.id.edtJamTutup);
        btnSimpan = findViewById(R.id.btnSimpan);
        cardCabangUtama = findViewById(R.id.cardCabangUtama);
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
                    java.util.Locale.getDefault(),
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

    private void checkMainBranch() {

        long restaurantId =
                getIntent().getLongExtra(
                        "restaurant_id",
                        0
                );

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        new Thread(() -> {

            int jumlahCabangUtama =
                    db.branchDao()
                            .countMainBranch(restaurantId);

            runOnUiThread(() -> {

                if (jumlahCabangUtama > 0) {

                    // Sudah ada cabang utama
                    cardCabangUtama.setVisibility(
                            View.GONE
                    );

                } else {

                    // Belum ada cabang utama
                    cardCabangUtama.setVisibility(
                            View.VISIBLE
                    );

                }

            });

        }).start();
    }
    private void simpanData() {

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        long restaurantId =
                getIntent().getLongExtra(
                        "restaurant_id",
                        0
                );

        Restaurant restaurant =
                db.restaurantDao().getById(restaurantId);

        if (restaurant == null) {

            Toast.makeText(
                    this,
                    "Data restoran tidak ditemukan",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Cek apakah restoran sudah memiliki
         * cabang utama.
         */
        int jumlahCabangUtama =
                db.branchDao()
                        .countMainBranch(restaurantId);

        /*
         * Jika belum ada cabang utama,
         * cabang ini menjadi cabang utama.
         *
         * Jika sudah ada,
         * cabang ini bukan cabang utama.
         */
        boolean isMain = jumlahCabangUtama == 0;


        Branch branch = new Branch();

        branch.restaurantId = restaurantId;

        branch.name =
                edtNamaCabang
                        .getText()
                        .toString()
                        .trim();

        branch.address =
                edtAlamatCabang
                        .getText()
                        .toString()
                        .trim();

        branch.phone =
                edtTeleponCabang
                        .getText()
                        .toString()
                        .trim();

        branch.openTime =
                edtJamBuka
                        .getText()
                        .toString()
                        .trim();

        branch.closeTime =
                edtJamTutup
                        .getText()
                        .toString()
                        .trim();

        branch.isMain = isMain;

        branch.syncStatus = 0;

        String firebaseId =
                UUID.randomUUID().toString();

        branch.firebaseId = firebaseId;


        /*
         * Simpan ke Room
         */
        long id =
                db.branchDao()
                        .insert(branch);


        /*
         * Simpan ke Firebase
         */
        FirebaseRepository firebase =
                new FirebaseRepository();

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

                        db.branchDao()
                                .updateSyncStatus(
                                        id,
                                        1
                                );

                        runOnUiThread(() -> {

                            Toast.makeText(
                                    RegisterBranchActivity.this,
                                    isMain
                                            ? "Cabang utama berhasil dibuat"
                                            : "Cabang berhasil ditambahkan",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();

                        });
                    }

                    @Override
                    public void failed(String error) {

                        db.branchDao()
                                .updateSyncStatus(
                                        id,
                                        2
                                );

                        runOnUiThread(() -> {

                            Toast.makeText(
                                    RegisterBranchActivity.this,
                                    "Cabang disimpan lokal, sinkronisasi gagal",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();

                        });
                    }
                }
        );
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
}