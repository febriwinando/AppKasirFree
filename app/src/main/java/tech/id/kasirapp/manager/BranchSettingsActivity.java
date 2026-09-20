package tech.id.kasirapp.manager;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.view.inputmethod.InputMethodManager;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.util.StatusHelper;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BranchSettingsActivity extends AppCompatActivity {

    private TextInputEditText edtNamaCabang;
    private TextInputEditText edtAlamat;
    private TextInputEditText edtTelepon;
    private TextInputEditText edtJamBuka;
    private TextInputEditText edtJamTutup;
    private TextInputEditText edtJumlahMeja;
    private TextInputEditText edtPajak;
    private TextInputEditText edtServiceCharge;

    private MaterialSwitch switchKirimDapur;
    private MaterialSwitch switchStokOtomatis;

    private MaterialButton btnSimpan;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_branch_settings
        );

        db = DatabaseClient.getDatabase(this);

        initView();

        setupToolbar();

        loadBranch();

        // Keyboard Handling
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        setupAutoScroll(scrollView);

        // Menerapkan Animasi
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        View container = findViewById(R.id.containerBranchSettings);
        if (container != null) container.startAnimation(slideUp);

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
        edtJumlahMeja = findViewById(R.id.edtJumlahMeja);
        edtPajak = findViewById(R.id.edtPajak);
        edtServiceCharge = findViewById(R.id.edtServiceCharge);
        switchKirimDapur = findViewById(R.id.switchKirimDapur);
        switchStokOtomatis = findViewById(R.id.switchStokOtomatis);

        btnSimpan = findViewById(R.id.btnSimpan);

        edtJamBuka.setOnClickListener(v -> showTimePicker(true));
        edtJamTutup.setOnClickListener(v -> showTimePicker(false));
        
        edtJamBuka.setOnTouchListener((v, event) -> { hideKeyboard(); return false; });
        edtJamTutup.setOnTouchListener((v, event) -> { hideKeyboard(); return false; });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupAutoScroll(NestedScrollView scrollView) {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    int scrollTo = v.getTop() - 100;
                    if (scrollTo < 0) scrollTo = 0;
                    scrollView.smoothScrollTo(0, scrollTo);
                }, 150);
            }
        };

        edtNamaCabang.setOnFocusChangeListener(focusListener);
        edtAlamat.setOnFocusChangeListener(focusListener);
        edtTelepon.setOnFocusChangeListener(focusListener);
        edtJumlahMeja.setOnFocusChangeListener(focusListener);
        edtPajak.setOnFocusChangeListener(focusListener);
        edtServiceCharge.setOnFocusChangeListener(focusListener);
    }

    private void showTimePicker(boolean jamBuka) {
        TextInputEditText target = jamBuka ? edtJamBuka : edtJamTutup;
        int hour = 8;
        int minute = 0;

        String value = target.getText() != null ? target.getText().toString() : "";
        if (!value.isEmpty()) {
            try {
                String[] waktu = value.split(":");
                hour = Integer.parseInt(waktu[0]);
                minute = Integer.parseInt(waktu[1]);
            } catch (Exception ignored) {}
        }

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(jamBuka ? "Pilih Jam Buka" : "Pilih Jam Tutup")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String waktu = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            target.setText(waktu);
        });

        picker.show(getSupportFragmentManager(), jamBuka ? "TIME_PICKER_BUKA" : "TIME_PICKER_TUTUP");
    }

    private void setupToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbar).setNavigationOnClickListener(v -> finish());
        }
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

                edtJumlahMeja.setText(branch.jumlahMeja == 0
                        ? "0"
                        : String.valueOf(branch.jumlahMeja));

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

                edtJumlahMeja.setText(String.valueOf(branch.jumlahMeja));

                edtPajak.setText(
                        branch.tax == 0.00
                                ? "0"
                                : String.valueOf(branch.tax)
                );

                edtServiceCharge.setText(
                        branch.serviceCharge == 0.00
                                ? "0"
                                : String.valueOf(branch.serviceCharge)
                );
                switchKirimDapur.setChecked(branch.sendToKitchen);
                switchStokOtomatis.setChecked(branch.automaticStock);

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
        // AMBIL DATA SWITCH
        // =========================================================

        boolean sendToKitchen =
                switchKirimDapur.isChecked();

        boolean automaticStock =
                switchStokOtomatis.isChecked();


        // =========================================================
        // NONAKTIFKAN TOMBOL
        // =========================================================

        btnSimpan.setEnabled(false);


        // =========================================================
        // PROSES
        // =========================================================

        double pajak = 0;
        try {
            pajak = Double.parseDouble(edtPajak.getText().toString().trim());
        } catch (Exception ignored) {}

        double serviceCharge = 0;
        try {
            serviceCharge = Double.parseDouble(edtServiceCharge.getText().toString().trim());
        } catch (Exception ignored) {}

        int jumlahMeja = 0;
        try {
            jumlahMeja = Integer.parseInt(edtJumlahMeja.getText().toString().trim());
        } catch (Exception ignored) {}

        double finalPajak = pajak;
        double finalServiceCharge = serviceCharge;
        int finalJumlahMeja = jumlahMeja;
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

                    StatusHelper.showError(
                            this,
                            "Gagal",
                            "Cabang tidak ditemukan",
                            null
                    );

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

            branch.jumlahMeja =
                    finalJumlahMeja;


            // =====================================================
            // OPERASIONAL
            // =====================================================

            branch.sendToKitchen =
                    sendToKitchen;

            branch.automaticStock =
                    automaticStock;


            // =====================================================
            // UPDATE FIREBASE
            // =====================================================

            runOnUiThread(() -> {
                btnSimpan.setEnabled(false);
                StatusHelper.showLoading(this, "Commiting system protocols...");
            });

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
                    branch.jumlahMeja,
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
                                StatusHelper.hideLoading();
                                StatusHelper.showSuccess(BranchSettingsActivity.this, "Berhasil", "Pengaturan cabang berhasil disimpan", () -> finish());

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
                                StatusHelper.hideLoading();
                                StatusHelper.showError(BranchSettingsActivity.this, "Gagal", "Disimpan di perangkat. Sinkronisasi ke server gagal.", () -> finish());

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