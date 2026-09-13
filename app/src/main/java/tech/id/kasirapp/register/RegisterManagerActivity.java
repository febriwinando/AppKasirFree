package tech.id.kasirapp.register;

import android.os.Bundle;
import android.widget.Toast;
import tech.id.kasirapp.util.StatusHelper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Manager;

public class RegisterManagerActivity extends AppCompatActivity {

    private TextInputEditText edtNama;
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private TextInputEditText edtKonfirmasiPassword;
    private TextInputEditText edtTelepon;

    private MaterialButton btnSimpan;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_register_manager
        );

        db = DatabaseClient.getDatabase(this);

        branchId =
                getIntent().getLongExtra(
                        "branch_id",
                        0
                );

        initView();

        checkManager();

        btnSimpan.setOnClickListener(
                v -> simpanManager()
        );
    }

    private void initView() {

        edtNama =
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

    private void checkManager() {
        executor.execute(() -> {
            Manager manager =
                    db.managerDao()
                            .getByBranchId(branchId);
            runOnUiThread(() -> {
                if (manager != null) {
                    StatusHelper.showError(
                            this,
                            "Kesalahan",
                            "Cabang ini sudah memiliki Manager",
                            () -> finish()
                    );
                }

            });

        });
    }

    private void simpanManager() {

        String nama =
                edtNama.getText()
                        .toString()
                        .trim();

        String username =
                edtUsername.getText()
                        .toString()
                        .trim();

        String password =
                edtPassword.getText()
                        .toString();

        String konfirmasi =
                edtKonfirmasiPassword
                        .getText()
                        .toString();

        String telepon =
                edtTelepon.getText()
                        .toString()
                        .trim();

        if (nama.isEmpty()) {

            edtNama.setError(
                    "Nama wajib diisi"
            );

            return;
        }

        if (username.isEmpty()) {

            edtUsername.setError(
                    "Username wajib diisi"
            );

            return;
        }

        if (password.isEmpty()) {

            edtPassword.setError(
                    "Password wajib diisi"
            );

            return;
        }

        if (!password.equals(konfirmasi)) {

            edtKonfirmasiPassword.setError(
                    "Password tidak sama"
            );

            return;
        }

        executor.execute(() -> {

            Branch branch =
                    db.branchDao()
                            .getById(branchId);

            if (branch == null) {

                runOnUiThread(() ->
                        StatusHelper.showError(
                                this,
                                "Kesalahan",
                                "Cabang tidak ditemukan",
                                null
                        )
                );

                return;
            }

            Manager existing =
                    db.managerDao()
                            .getByBranchId(branchId);

            if (existing != null) {

                runOnUiThread(() ->
                        StatusHelper.showError(
                                this,
                                "Kesalahan",
                                "Cabang ini sudah memiliki Manager",
                                null
                        )
                );

                return;
            }

            String firebaseId =
                    UUID.randomUUID().toString();

            String passwordHash =
                    BCrypt.withDefaults()
                            .hashToString(
                                    12,
                                    password.toCharArray()
                            );

            Manager manager =
                    new Manager();

            manager.firebaseId =
                    firebaseId;

            manager.branchId =
                    branchId;

            manager.name =
                    nama;

            manager.username =
                    username;

            manager.password =
                    passwordHash;

            manager.phone =
                    telepon;

            manager.syncStatus = 0;

            long managerId =
                    db.managerDao()
                            .insert(manager);

            FirebaseRepository firebase =
                    new FirebaseRepository();

            firebase.saveManager(
                    firebaseId,
                    branch.firebaseId,
                    branchId,
                    nama,
                    username,
                    passwordHash,
                    telepon,
                    new FirebaseRepository.OnCompleteListener() {

                        @Override
                        public void success() {

                            db.managerDao()
                                    .updateSyncStatus(
                                            managerId,
                                            1
                                    );

                            runOnUiThread(() -> {

                                StatusHelper.showSuccess(RegisterManagerActivity.this, "Berhasil", "Manager berhasil dibuat", () -> finish());

                            });
                        }

                        @Override
                        public void failed(
                                String error
                        ) {

                            db.managerDao()
                                    .updateSyncStatus(
                                            managerId,
                                            2
                                    );

                            runOnUiThread(() -> {

                                StatusHelper.showError(RegisterManagerActivity.this, "Gagal", "Manager disimpan lokal, sinkronisasi gagal", () -> finish());

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