package tech.id.kasirapp.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import tech.id.kasirapp.util.StatusHelper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.dashboard.DashboardOwnerActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Owner;

public class RegisterOwnerActivity extends AppCompatActivity {
    TextInputEditText edtNamaOwner, edtUsername, edtEmail, edtNoHp, edtPassword, edtKonfirmasiPassword;
    MaterialButton btnRegister;
    TextView txtLogin;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkSession();
    }

    private void registerOwner() {
        String nama = edtNamaOwner.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String konfirmasi = edtKonfirmasiPassword.getText().toString();
        String email = edtEmail.getText().toString().trim();
        String phone = edtNoHp.getText().toString().trim();

        if(nama.isEmpty()){
            edtNamaOwner.setError(getString(R.string.err_name_required));
            return;
        }

        if(username.isEmpty()){
            edtUsername.setError(getString(R.string.err_username_required));
            return;
        }

        if(password.isEmpty()){
            edtPassword.setError(getString(R.string.err_password_required));
            return;
        }

        if(!password.equals(konfirmasi)){
            edtKonfirmasiPassword.setError(getString(R.string.err_password_mismatch));
            return;
        }

        Owner owner = new Owner();
        owner.firebaseId = UUID.randomUUID().toString();
        owner.name = nama;
        owner.username = username;
        owner.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        owner.email = email;
        owner.phone = phone;
        owner.syncStatus = 0;

        long id = DatabaseClient
                .getDatabase(this)
                .ownerDao()
                .insert(owner);
        
        StatusHelper.showLoading(this, "Creating your profile...");
        uploadOwner(id);
    }

    private void uploadOwner(long id) {
        executor.execute(() -> {
            Owner owner = DatabaseClient
                    .getDatabase(this)
                    .ownerDao()
                    .getById(id);

            if (owner == null) return;

            FirebaseRepository firebase = new FirebaseRepository();
            firebase.saveOwner(
                    owner.firebaseId,
                    owner.name,
                    owner.username,
                    owner.email,
                    owner.password,
                    owner.phone,
                    new FirebaseRepository.OnCompleteListener() {
                        @Override
                        public void success() {
                            owner.syncStatus = 1;
                            DatabaseClient.getDatabase(RegisterOwnerActivity.this)
                                    .ownerDao()
                                    .update(owner);

                            AppSession session = new AppSession();
                            session.ownerId = owner.id;
                            session.uuid = owner.firebaseId;
                            session.isLoggedIn = true;
                            session.role = "OWNER";

                            DatabaseClient.getDatabase(RegisterOwnerActivity.this)
                                    .sessionDao()
                                    .insert(session);

                            runOnUiThread(() -> {
                                StatusHelper.hideLoading();
                                StatusHelper.showSuccess(RegisterOwnerActivity.this, "Registrasi Berhasil", "Akun Owner Anda telah berhasil didaftarkan.", () -> {
                                    Intent intent = new Intent(
                                            RegisterOwnerActivity.this,
                                            DashboardOwnerActivity.class);
                                    intent.putExtra("owner_id", owner.id);
                                    startActivity(intent);
                                    finish();
                                });
                            });
                        }

                        @Override
                        public void failed(String error) {
                            owner.syncStatus = 2;
                            DatabaseClient.getDatabase(RegisterOwnerActivity.this)
                                    .ownerDao()
                                    .update(owner);

                            runOnUiThread(() -> {
                                StatusHelper.hideLoading();
                                StatusHelper.showError(RegisterOwnerActivity.this, "Registrasi Gagal", "Data lokal berhasil disimpan, namun gagal sinkron ke Firebase: " + error, null);
                            });
                        }
                    }
            );
        });
    }
    private void checkSession() {
        executor.execute(() -> {

            AppSession session = DatabaseClient
                    .getDatabase(RegisterOwnerActivity.this)
                    .sessionDao()
                    .getSession();

            runOnUiThread(() -> {

                if (session != null && session.isLoggedIn) {

                    Intent intent = new Intent(
                            RegisterOwnerActivity.this,
                            DashboardOwnerActivity.class
                    );

                    intent.putExtra("owner_id", session.ownerId);

                    startActivity(intent);
                    finish();

                } else {

                    // Session tidak ada → baru tampilkan form register
                    setContentView(R.layout.activity_register);

                    // Inisialisasi komponen register di sini
                    initRegisterForm();
                }
            });
        });
    }

    private void initRegisterForm() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        edtNamaOwner = findViewById(R.id.edtNamaOwner);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtNoHp = findViewById(R.id.edtNoHp);
        edtPassword = findViewById(R.id.edtPassword);
        edtKonfirmasiPassword = findViewById(R.id.edtKonfirmasiPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        btnRegister.setOnClickListener(v -> registerOwner());
        txtLogin.setOnClickListener(v -> finish());

        setupToolbar();

        // Menangani Insets agar form tidak tertutup keyboard (Edge-to-Edge)
        View scrollView = findViewById(R.id.scrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        // Menerapkan Animasi Future Google
        Animation entrance = AnimationUtils.loadAnimation(this, R.anim.anim_liquid_entrance);

        View cardRegister = findViewById(R.id.cardRegister);
        if (cardRegister != null) cardRegister.startAnimation(entrance);
        
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.startAnimation(entrance);
    }
    private void setupToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbar).setNavigationOnClickListener(v -> finish());
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}