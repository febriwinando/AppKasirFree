package tech.id.kasirapp;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Branch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BranchSettingsActivity extends AppCompatActivity {

    private TextInputEditText edtNamaCabang;
    private TextInputEditText edtAlamat;
    private TextInputEditText edtTelepon;
    private TextInputEditText edtJamBuka;
    private TextInputEditText edtJamTutup;

    private TextInputEditText edtPajak;
    private TextInputEditText edtServiceCharge;

    private MaterialSwitch switchDineIn;
    private MaterialSwitch switchTakeAway;
    private MaterialSwitch switchDelivery;

    private MaterialSwitch switchKirimDapur;
    private MaterialSwitch switchStokOtomatis;
    private MaterialSwitch switchStokNegatif;

    private MaterialButton btnSimpan;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_branch_settings
        );

        db = DatabaseClient.getDatabase(this);

        initView();

        loadBranch();

        btnSimpan.setOnClickListener(
                v -> simpanPengaturan()
        );
    }

    private void initView() {

        edtNamaCabang = findViewById(R.id.edtNamaCabang);
        edtAlamat = findViewById(R.id.edtAlamat);
        edtTelepon = findViewById(R.id.edtTelepon);
        edtJamBuka = findViewById(R.id.edtJamBuka);
        edtJamTutup = findViewById(R.id.edtJamTutup);
        edtPajak = findViewById(R.id.edtPajak);
        edtServiceCharge = findViewById(R.id.edtServiceCharge);
        switchDineIn = findViewById(R.id.switchDineIn);
        switchTakeAway = findViewById(R.id.switchTakeAway);
        switchDelivery = findViewById(R.id.switchDelivery);
        switchKirimDapur = findViewById(R.id.switchKirimDapur);
        switchStokOtomatis = findViewById(R.id.switchStokOtomatis);
        switchStokNegatif = findViewById(R.id.switchStokNegatif);

        btnSimpan = findViewById(R.id.btnSimpan);


    }

    private void loadBranch() {

        executor.execute(() -> {

            AppSession session =
                    db.sessionDao().getSession();

            if (session == null ||
                    !session.isLoggedIn ||
                    !"MANAGER".equals(session.role)) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Session manager tidak valid",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                });

                return;
            }

            branchId =
                    session.branchId;

            Branch branch =
                    db.branchDao()
                            .getById(branchId);

            runOnUiThread(() -> {

                if (branch == null) {

                    Toast.makeText(
                            this,
                            "Data cabang tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                    return;
                }

                edtNamaCabang.setText(
                        branch.name
                );

                edtAlamat.setText(
                        branch.address == null
                                ? ""
                                : branch.address
                );

                edtTelepon.setText(
                        branch.phone == null
                                ? ""
                                : branch.phone
                );

                edtJamBuka.setText(
                        branch.openTime == null
                                ? ""
                                : branch.openTime
                );

                edtJamTutup.setText(
                        branch.closeTime == null
                                ? ""
                                : branch.closeTime
                );

                /*
                 * Untuk sementara nilai default.
                 *
                 * Nanti mengambil dari
                 * BranchSettings entity.
                 */

                edtPajak.setText("0");

                edtServiceCharge.setText("0");

                switchDineIn.setChecked(true);

                switchTakeAway.setChecked(true);

                switchDelivery.setChecked(false);

                switchKirimDapur.setChecked(true);

                switchStokOtomatis.setChecked(true);

                switchStokNegatif.setChecked(false);

            });

        });
    }

    private void simpanPengaturan() {

        String nama =
                edtNamaCabang
                        .getText()
                        .toString()
                        .trim();

        String alamat =
                edtAlamat
                        .getText()
                        .toString()
                        .trim();

        String telepon =
                edtTelepon
                        .getText()
                        .toString()
                        .trim();

        String jamBuka =
                edtJamBuka
                        .getText()
                        .toString()
                        .trim();

        String jamTutup =
                edtJamTutup
                        .getText()
                        .toString()
                        .trim();


        // =========================================================
        // VALIDASI
        // =========================================================

        if (nama.isEmpty()) {

            edtNamaCabang.setError(
                    "Nama cabang wajib diisi"
            );

            edtNamaCabang.requestFocus();

            return;
        }


        if (jamBuka.isEmpty()) {

            edtJamBuka.setError(
                    "Jam buka wajib diisi"
            );

            edtJamBuka.requestFocus();

            return;
        }


        if (jamTutup.isEmpty()) {

            edtJamTutup.setError(
                    "Jam tutup wajib diisi"
            );

            edtJamTutup.requestFocus();

            return;
        }


        // =========================================================
        // PAJAK
        // =========================================================

        double pajak = 0;

        String pajakText =
                edtPajak
                        .getText()
                        .toString()
                        .trim();

        if (!pajakText.isEmpty()) {

            try {

                pajak =
                        Double.parseDouble(
                                pajakText
                        );

            } catch (NumberFormatException e) {

                edtPajak.setError(
                        "Nilai pajak tidak valid"
                );

                edtPajak.requestFocus();

                return;
            }
        }


        // =========================================================
        // SERVICE CHARGE
        // =========================================================

        double serviceCharge = 0;

        String serviceText =
                edtServiceCharge
                        .getText()
                        .toString()
                        .trim();

        if (!serviceText.isEmpty()) {

            try {

                serviceCharge =
                        Double.parseDouble(
                                serviceText
                        );

            } catch (NumberFormatException e) {

                edtServiceCharge.setError(
                        "Nilai service charge tidak valid"
                );

                edtServiceCharge.requestFocus();

                return;
            }
        }


        // =========================================================
        // BATASI NILAI PERSENTASE
        // =========================================================

        if (pajak < 0 || pajak > 100) {

            edtPajak.setError(
                    "Pajak harus antara 0 sampai 100%"
            );

            edtPajak.requestFocus();

            return;
        }


        if (serviceCharge < 0 ||
                serviceCharge > 100) {

            edtServiceCharge.setError(
                    "Service charge harus antara 0 sampai 100%"
            );

            edtServiceCharge.requestFocus();

            return;
        }


        // =========================================================
        // AMBIL DATA SWITCH
        // =========================================================

        boolean dineIn =
                switchDineIn.isChecked();

        boolean takeAway =
                switchTakeAway.isChecked();

        boolean delivery =
                switchDelivery.isChecked();


        boolean sendToKitchen =
                switchKirimDapur.isChecked();

        boolean automaticStock =
                switchStokOtomatis.isChecked();

        boolean allowNegativeStock =
                switchStokNegatif.isChecked();


        // =========================================================
        // NONAKTIFKAN TOMBOL
        // =========================================================

        btnSimpan.setEnabled(false);


        // =========================================================
        // PROSES
        // =========================================================

        double finalPajak = pajak;
        double finalServiceCharge = serviceCharge;
        executor.execute(() -> {

            Branch branch =
                    db.branchDao()
                            .getById(branchId);


            // =====================================================
            // CEK CABANG
            // =====================================================

            if (branch == null) {

                runOnUiThread(() -> {

                    btnSimpan.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Cabang tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                });

                return;
            }


            // =====================================================
            // UPDATE OBJECT BRANCH
            // =====================================================

            branch.name =
                    nama;

            branch.address =
                    alamat;

            branch.phone =
                    telepon;

            branch.openTime =
                    jamBuka;

            branch.closeTime =
                    jamTutup;


            // =====================================================
            // UPDATE PENGATURAN
            // =====================================================

            branch.tax =
                    finalPajak;

            branch.serviceCharge =
                    finalServiceCharge;


            // =====================================================
            // METODE PENJUALAN
            // =====================================================

            branch.dineIn =
                    dineIn;

            branch.takeAway =
                    takeAway;

            branch.delivery =
                    delivery;


            // =====================================================
            // OPERASIONAL
            // =====================================================

            branch.sendToKitchen =
                    sendToKitchen;

            branch.automaticStock =
                    automaticStock;

            branch.allowNegativeStock =
                    allowNegativeStock;


            // =====================================================
            // UPDATE FIREBASE
            // =====================================================

            FirebaseRepository firebase =
                    new FirebaseRepository();


            firebase.updateBranch(

                    branch.firebaseId,

                    branch.name,
                    branch.address,
                    branch.phone,

                    branch.openTime,
                    branch.closeTime,

                    branch.isMain,

                    branch.tax,
                    branch.serviceCharge,

                    branch.dineIn,
                    branch.takeAway,
                    branch.delivery,

                    branch.sendToKitchen,
                    branch.automaticStock,
                    branch.allowNegativeStock,

                    new FirebaseRepository.OnCompleteListener() {

                        @Override
                        public void success() {

                            // =====================================
                            // FIREBASE BERHASIL
                            // =====================================

                            branch.syncStatus = 1;


                            db.branchDao()
                                    .update(branch);


                            runOnUiThread(() -> {

                                btnSimpan.setEnabled(true);

                                Toast.makeText(
                                        BranchSettingsActivity.this,
                                        "Pengaturan cabang berhasil disimpan",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                            });
                        }


                        @Override
                        public void failed(
                                String error
                        ) {

                            // =====================================
                            // FIREBASE GAGAL
                            // =====================================

                            /*
                             * Tetap simpan perubahan
                             * ke Room.
                             */

                            branch.syncStatus = 2;


                            db.branchDao()
                                    .update(branch);


                            runOnUiThread(() -> {

                                btnSimpan.setEnabled(true);

                                Toast.makeText(
                                        BranchSettingsActivity.this,
                                        "Disimpan di perangkat. Sinkronisasi ke server gagal.",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                            });
                        }
                    }
            );

        });
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();

    }
}