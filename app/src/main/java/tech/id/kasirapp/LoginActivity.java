package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;

    private Button btnLogin;
    private ProgressBar progress;

    private FirebaseFirestore firestore;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    TextView txtRegistrasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_login
        );


        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        txtRegistrasi = findViewById(R.id.txtRegistrasi);

        txtRegistrasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterOwnerActivity.class));
            }
        });

        firestore = FirebaseFirestore.getInstance();

        db =
                DatabaseClient.getDatabase(this);


        btnLogin.setOnClickListener(v -> {

            String username =
                    edtUsername
                            .getText()
                            .toString()
                            .trim();

            String password =
                    edtPassword
                            .getText()
                            .toString();


            if (username.isEmpty()) {

                edtUsername.setError(
                        "Username wajib diisi"
                );

                edtUsername.requestFocus();

                return;
            }


            if (password.isEmpty()) {

                edtPassword.setError(
                        "Password wajib diisi"
                );

                edtPassword.requestFocus();

                return;
            }


            prosesLogin(
                    username,
                    password
            );

        });

    }


    private void prosesLogin(
            String username,
            String password
    ) {

        btnLogin.setEnabled(false);

        progress.setVisibility(
                View.VISIBLE
        );


        /*
         * Cari username di Firestore.
         *
         * Untuk sementara kita cek
         * masing-masing collection.
         */

        cekOwner(
                username,
                password
        );

    }


    // =========================================================
    // CEK OWNER
    // =========================================================

    private void cekOwner(
            String username,
            String password
    ) {

        firestore.collection("owners")
                .whereEqualTo(
                        "username",
                        username
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        result -> {

                            if (!result.isEmpty()) {

                                DocumentSnapshot doc =
                                        result.getDocuments()
                                                .get(0);

                                prosesLoginOwner(
                                        doc,
                                        password
                                );

                            } else {

                                cekManager(
                                        username,
                                        password
                                );

                            }

                        }
                )
                .addOnFailureListener(
                        e -> loginGagal(
                                "Gagal menghubungi server"
                        )
                );

    }


    // =========================================================
    // CEK MANAGER
    // =========================================================

    private void cekManager(
            String username,
            String password
    ) {

        firestore.collection("managers")
                .whereEqualTo(
                        "username",
                        username
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        result -> {

                            if (!result.isEmpty()) {

                                DocumentSnapshot doc =
                                        result.getDocuments()
                                                .get(0);

                                prosesLoginManager(
                                        doc,
                                        password
                                );

                            } else {

                                cekWaiter(
                                        username,
                                        password
                                );

                            }

                        }
                )
                .addOnFailureListener(
                        e -> loginGagal(
                                "Gagal menghubungi server"
                        )
                );

    }


    // =========================================================
    // CEK WAITER
    // =========================================================

    private void cekWaiter(
            String username,
            String password
    ) {

        firestore.collection("waiters")
                .whereEqualTo(
                        "username",
                        username
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        result -> {

                            if (!result.isEmpty()) {

                                DocumentSnapshot doc =
                                        result.getDocuments()
                                                .get(0);

                                prosesLoginWaiter(
                                        doc,
                                        password
                                );

                            } else {

                                cekKasir(
                                        username,
                                        password
                                );

                            }

                        }
                )
                .addOnFailureListener(
                        e -> loginGagal(
                                "Gagal menghubungi server"
                        )
                );

    }


    // =========================================================
    // CEK KASIR
    // =========================================================

    private void cekKasir(
            String username,
            String password
    ) {

        firestore.collection("cashiers")
                .whereEqualTo(
                        "username",
                        username
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        result -> {

                            if (!result.isEmpty()) {

                                DocumentSnapshot doc =
                                        result.getDocuments()
                                                .get(0);

                                prosesLoginKasir(
                                        doc,
                                        password
                                );

                            } else {

                                cekDapur(
                                        username,
                                        password
                                );

                            }

                        }
                )
                .addOnFailureListener(
                        e -> loginGagal(
                                "Gagal menghubungi server"
                        )
                );

    }


    // =========================================================
    // CEK DAPUR
    // =========================================================

    private void cekDapur(
            String username,
            String password
    ) {

        firestore.collection("kitchens")
                .whereEqualTo(
                        "username",
                        username
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        result -> {

                            if (!result.isEmpty()) {

                                DocumentSnapshot doc =
                                        result.getDocuments()
                                                .get(0);

                                prosesLoginDapur(
                                        doc,
                                        password
                                );

                            } else {

                                loginGagal(
                                        "Username atau password salah"
                                );

                            }

                        }
                )
                .addOnFailureListener(
                        e -> loginGagal(
                                "Gagal menghubungi server"
                        )
                );

    }

    private void prosesLoginOwner(
            DocumentSnapshot doc,
            String password
    ) {

        String passwordHash =
                doc.getString("password");


        if (!verifikasiPassword(
                password,
                passwordHash
        )) {

            loginGagal(
                    "Username atau password salah"
            );

            return;
        }


        long userId =
                getLong(doc, "id");


        String uuid =
                doc.getId();


        AppSession session =
                new AppSession();

        session.id = 1;

        session.userId =
                userId;

        session.uuid =
                uuid;

        session.role =
                "OWNER";

        session.ownerId =
                userId;

        session.restaurantId =
                0;

        session.branchId =
                0;

        session.isLoggedIn =
                true;


        simpanSession(
                session,
                DashboardOwnerActivity.class
        );
    }
    private void prosesLoginManager(
            DocumentSnapshot doc,
            String password
    ) {

        String passwordHash =
                doc.getString("password");


        if (!verifikasiPassword(
                password,
                passwordHash
        )) {

            loginGagal(
                    "Username atau password salah"
            );

            return;
        }


        AppSession session =
                new AppSession();

        session.id = 1;

        session.userId =
                getLong(doc, "id");

        session.uuid =
                doc.getId();

        session.role =
                "MANAGER";

        session.ownerId =
                getLong(doc, "ownerId");

        session.restaurantId =
                getLong(doc, "restaurantId");

        session.branchId =
                getLong(doc, "branchId");

        session.isLoggedIn =
                true;


        simpanSession(
                session,
                DashboardManagerActivity.class
        );
    }

    private void prosesLoginWaiter(
            DocumentSnapshot doc,
            String password
    ) {

        if (!verifikasiPassword(
                password,
                doc.getString("password")
        )) {

            loginGagal(
                    "Username atau password salah"
            );

            return;
        }


        AppSession session =
                new AppSession();

        session.id = 1;

        session.userId =
                getLong(doc, "id");

        session.uuid =
                doc.getId();

        session.role =
                "WAITER";

        session.ownerId =
                getLong(doc, "ownerId");

        session.restaurantId =
                getLong(doc, "restaurantId");

        session.branchId =
                getLong(doc, "branchId");

        session.isLoggedIn =
                true;


        simpanSession(
                session,
                DashboardWaiterActivity.class
        );
    }

    private void prosesLoginKasir(
            DocumentSnapshot doc,
            String password
    ) {

        if (!verifikasiPassword(
                password,
                doc.getString("password")
        )) {

            loginGagal(
                    "Username atau password salah"
            );

            return;
        }


        AppSession session =
                new AppSession();

        session.id = 1;

        session.userId =
                getLong(doc, "id");

        session.uuid =
                doc.getId();

        session.role =
                "KASIR";

        session.ownerId =
                getLong(doc, "ownerId");

        session.restaurantId =
                getLong(doc, "restaurantId");

        session.branchId =
                getLong(doc, "branchId");

        session.isLoggedIn =
                true;


        simpanSession(
                session,
                DashboardCashierActivity.class
        );
    }

    private void prosesLoginDapur(
            DocumentSnapshot doc,
            String password
    ) {

        if (!verifikasiPassword(
                password,
                doc.getString("password")
        )) {

            loginGagal(
                    "Username atau password salah"
            );

            return;
        }


        AppSession session =
                new AppSession();

        session.id = 1;

        session.userId =
                getLong(doc, "id");

        session.uuid =
                doc.getId();

        session.role =
                "DAPUR";

        session.ownerId =
                getLong(doc, "ownerId");

        session.restaurantId =
                getLong(doc, "restaurantId");

        session.branchId =
                getLong(doc, "branchId");

        session.isLoggedIn =
                true;


        simpanSession(
                session,
                DashboardKitchenActivity.class
        );
    }

    private boolean verifikasiPassword(
            String password,
            String passwordHash
    ) {

        if (passwordHash == null ||
                passwordHash.isEmpty()) {

            return false;
        }


        try {

            return BCrypt.verifyer()
                    .verify(
                            password.toCharArray(),
                            passwordHash
                    )
                    .verified;

        } catch (Exception e) {

            return false;
        }
    }

    private void simpanSession(
            AppSession session,
            Class<?> dashboard
    ) {

        executor.execute(() -> {

            db.sessionDao()
                    .insert(session);


            runOnUiThread(() -> {

                progress.setVisibility(
                        View.GONE
                );

                btnLogin.setEnabled(
                        true
                );


                Toast.makeText(
                        LoginActivity.this,
                        "Login berhasil",
                        Toast.LENGTH_SHORT
                ).show();


                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                dashboard
                        );


                startActivity(intent);

                finish();

            });

        });
    }

    private void loginGagal(
            String message
    ) {

        runOnUiThread(() -> {

            progress.setVisibility(
                    View.GONE
            );

            btnLogin.setEnabled(
                    true
            );

            Toast.makeText(
                    LoginActivity.this,
                    message,
                    Toast.LENGTH_SHORT
            ).show();

        });

    }

    private long getLong(
            DocumentSnapshot doc,
            String field
    ) {

        Long value = doc.getLong(field);

        return value != null
                ? value
                : 0;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();

    }
}