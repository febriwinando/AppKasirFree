package tech.id.kasirapp.owner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Manager;

public class EditManagerActivity extends AppCompatActivity {

    private TextInputEditText edtNamaManager;
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private TextInputEditText edtKonfirmasiPassword;
    private TextInputEditText edtTelepon;

    private MaterialButton btnSimpan;

    private AppDatabase db;

    private Manager manager;

    private long managerId;
    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_manager
        );

        db = DatabaseClient.getDatabase(this);

        initView();

        managerId =
                getIntent().getLongExtra(
                        "manager_id",
                        0
                );

        branchId =
                getIntent().getLongExtra(
                        "branch_id",
                        0
                );

        loadManager();

        btnSimpan.setOnClickListener(
                v -> updateManager()
        );
    }

    // =========================================================
    // INIT VIEW
    // =========================================================

    private void initView() {

        edtNamaManager =
                findViewById(
                        R.id.edtNamaManager
                );

        edtUsername =
                findViewById(
                        R.id.edtUsername
                );

        edtPassword =
                findViewById(
                        R.id.edtPassword
                );

        edtKonfirmasiPassword =
                findViewById(
                        R.id.edtKonfirmasiPassword
                );

        edtTelepon =
                findViewById(
                        R.id.edtTelepon
                );

        btnSimpan =
                findViewById(
                        R.id.btnSimpan
                );
    }

    // =========================================================
    // LOAD MANAGER
    // =========================================================

    private void loadManager() {

        new Thread(() -> {

            manager =
                    db.managerDao()
                            .getById(managerId);

            if (manager == null && branchId > 0) {

                manager =
                        db.managerDao()
                                .getByBranchId(
                                        branchId
                                );
            }

            runOnUiThread(() -> {

                if (manager == null) {

                    Toast.makeText(
                            EditManagerActivity.this,
                            "Data manager tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                    return;
                }

                tampilkanDataManager();

            });

        }).start();
    }

    // =========================================================
    // TAMPILKAN DATA
    // =========================================================

    private void tampilkanDataManager() {

        edtNamaManager.setText(
                manager.name
        );

        edtUsername.setText(
                manager.username
        );

        edtTelepon.setText(
                manager.phone == null
                        ? ""
                        : manager.phone
        );

        /*
         * Password sengaja tidak ditampilkan.
         *
         * Karena password di database adalah
         * hasil hash BCrypt.
         */

        edtPassword.setText("");

        edtKonfirmasiPassword.setText("");

        btnSimpan.setText(
                "SIMPAN PERUBAHAN"
        );
    }

    // =========================================================
    // UPDATE MANAGER
    // =========================================================

    private void updateManager() {

        String nama =
                edtNamaManager
                        .getText()
                        .toString()
                        .trim();

        String username =
                edtUsername
                        .getText()
                        .toString()
                        .trim();

        String password =
                edtPassword
                        .getText()
                        .toString();

        String konfirmasiPassword =
                edtKonfirmasiPassword
                        .getText()
                        .toString();

        String telepon =
                edtTelepon
                        .getText()
                        .toString()
                        .trim();

        // =====================================================
        // VALIDASI
        // =====================================================

        if (nama.isEmpty()) {

            edtNamaManager.setError(
                    "Nama manager wajib diisi"
            );

            edtNamaManager.requestFocus();

            return;
        }

        if (username.isEmpty()) {

            edtUsername.setError(
                    "Username wajib diisi"
            );

            edtUsername.requestFocus();

            return;
        }

        /*
         * Password boleh kosong.
         *
         * Kosong = password lama tetap digunakan.
         *
         * Diisi = password akan diganti.
         */

        if (!password.isEmpty()) {

            if (password.length() < 6) {

                edtPassword.setError(
                        "Password minimal 6 karakter"
                );

                edtPassword.requestFocus();

                return;
            }

            if (!password.equals(
                    konfirmasiPassword
            )) {

                edtKonfirmasiPassword.setError(
                        "Konfirmasi password tidak sama"
                );

                edtKonfirmasiPassword.requestFocus();

                return;
            }
        }

        // =====================================================
        // UPDATE
        // =====================================================

        btnSimpan.setEnabled(false);

        new Thread(() -> {

            /*
             * Update data dasar
             */

            manager.name =
                    nama;

            manager.username =
                    username;

            manager.phone =
                    telepon;

            /*
             * Jika password diisi,
             * buat hash password baru.
             */

            if (!password.isEmpty()) {

                manager.password =
                        BCrypt.withDefaults()
                                .hashToString(
                                        12,
                                        password.toCharArray()
                                );
            }

            /*
             * Tandai belum sync.
             */

            manager.syncStatus = 0;

            db.managerDao()
                    .update(manager);

            runOnUiThread(() -> {

                updateFirebase();

            });

        }).start();
    }

    // =========================================================
    // UPDATE FIREBASE
    // =========================================================

    private void updateFirebase() {

        FirebaseRepository firebase =
                new FirebaseRepository();

        firebase.updateManager(
                manager.firebaseId,
                manager.branchId,
                manager.name,
                manager.username,
                manager.password,
                manager.phone,
                new FirebaseRepository.OnCompleteListener() {

                    @Override
                    public void success() {

                        new Thread(() -> {

                            db.managerDao()
                                    .updateSyncStatus(
                                            manager.id,
                                            1
                                    );

                            runOnUiThread(() -> {

                                Toast.makeText(
                                        EditManagerActivity.this,
                                        "Manager berhasil diperbarui",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                            });

                        }).start();
                    }

                    @Override
                    public void failed(
                            String error
                    ) {

                        new Thread(() -> {

                            db.managerDao()
                                    .updateSyncStatus(
                                            manager.id,
                                            2
                                    );

                            runOnUiThread(() -> {

                                Toast.makeText(
                                        EditManagerActivity.this,
                                        "Manager diperbarui lokal, tetapi sinkronisasi gagal",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                            });

                        }).start();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }
}