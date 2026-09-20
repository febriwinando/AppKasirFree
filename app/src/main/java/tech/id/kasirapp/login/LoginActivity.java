package tech.id.kasirapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
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
import tech.id.kasirapp.R;
import tech.id.kasirapp.dashboard.DashboardCashierActivity;
import tech.id.kasirapp.dashboard.DashboardKitchenActivity;
import tech.id.kasirapp.dashboard.DashboardManagerActivity;
import tech.id.kasirapp.dashboard.DashboardOwnerActivity;
import tech.id.kasirapp.dashboard.DashboardWaiterActivity;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.data.local.entity.Owner;
import tech.id.kasirapp.data.local.entity.Manager;
import tech.id.kasirapp.data.local.entity.Waiter;
import tech.id.kasirapp.register.RegisterOwnerActivity;
import tech.id.kasirapp.util.StatusHelper;
import tech.id.kasirapp.util.SyncHelper;

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
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_login
        );

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        txtRegistrasi = findViewById(R.id.txtRegistrasi);

        // Menangani Insets agar form tidak tertutup keyboard (Edge-to-Edge)
        View scrollView = findViewById(R.id.scrollView);
        if (scrollView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                v.setPadding(0, insets.top, 0, insets.bottom);
                return windowInsets;
            });
            setupAutoScroll((NestedScrollView) scrollView);
        }

        // Menerapkan Animasi Future Google
        Animation entrance = AnimationUtils.loadAnimation(this, R.anim.anim_liquid_entrance);
        
        View cardLogin = findViewById(R.id.cardLogin);
        if (cardLogin != null) cardLogin.startAnimation(entrance);

        View logoContainer = findViewById(R.id.logoContainer);
        if (logoContainer != null) logoContainer.startAnimation(entrance);

        View tvAppTitle = findViewById(R.id.tvAppTitle);
        if (tvAppTitle != null) tvAppTitle.startAnimation(entrance);

        if (txtRegistrasi != null) txtRegistrasi.startAnimation(entrance);

        if (txtRegistrasi != null) {
            txtRegistrasi.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterOwnerActivity.class)));
        }

        firestore = FirebaseFirestore.getInstance();
        db = DatabaseClient.getDatabase(this);

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (username.isEmpty()) {
                edtUsername.setError(getString(R.string.err_username_required));
                edtUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError(getString(R.string.err_password_required));
                edtPassword.requestFocus();
                return;
            }

            prosesLogin(username, password);
        });
    }

    private void setupAutoScroll(NestedScrollView scrollView) {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    int scrollTo = v.getTop() - 200;
                    if (scrollTo < 0) scrollTo = 0;
                    scrollView.smoothScrollTo(0, scrollTo);
                }, 150);
            }
        };

        edtUsername.setOnFocusChangeListener(focusListener);
        edtPassword.setOnFocusChangeListener(focusListener);
    }

    private void prosesLogin(String username, String password) {
        btnLogin.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> cekOwner(username, password));
    }

    private void cekOwner(String username, String password) {
        firestore.collection("owners")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        prosesLoginOwner(task.getResult().getDocuments().get(0), password);
                    } else {
                        cekManager(username, password);
                    }
                });
    }

    private void cekManager(String username, String password) {
        firestore.collection("managers")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        prosesLoginManager(task.getResult().getDocuments().get(0), password);
                    } else {
                        cekWaiter(username, password);
                    }
                });
    }

    private void cekWaiter(String username, String password) {
        firestore.collection("waiters")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        prosesLoginWaiter(task.getResult().getDocuments().get(0), password);
                    } else {
                        cekKasir(username, password);
                    }
                });
    }

    private void cekKasir(String username, String password) {
        firestore.collection("cashiers")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        prosesLoginKasir(task.getResult().getDocuments().get(0), password);
                    } else {
                        cekDapur(username, password);
                    }
                });
    }

    private void cekDapur(String username, String password) {
        firestore.collection("kitchen_staff")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        prosesLoginDapur(task.getResult().getDocuments().get(0), password);
                    } else {
                        loginGagal(getString(R.string.msg_login_failed));
                    }
                });
    }

    private void prosesLoginOwner(DocumentSnapshot doc, String password) {
        String dbPassword = doc.getString("password");
        if (verifikasiPassword(password, dbPassword)) {
            Owner owner = new Owner();
            owner.firebaseId = doc.getId();
            owner.name = doc.getString("name");
            owner.username = doc.getString("username");
            owner.email = doc.getString("email");
            owner.phone = doc.getString("phone");
            owner.role = "owner";
            owner.syncStatus = 1;

            executor.execute(() -> {
                long id = db.ownerDao().insert(owner);
                AppSession session = new AppSession();
                session.userId = id;
                session.ownerId = id;
                session.uuid = owner.firebaseId;
                session.isLoggedIn = true;
                session.role = "OWNER";
                
                runOnUiThread(() -> progress.setIndeterminate(true));
                new SyncHelper(this).syncOwnerData(owner.firebaseId, id, () -> {
                    simpanSession(session, DashboardOwnerActivity.class);
                });
            });
        } else {
            loginGagal(getString(R.string.msg_login_failed));
        }
    }

    private void prosesLoginManager(DocumentSnapshot doc, String password) {
        String dbPassword = doc.getString("password");
        if (verifikasiPassword(password, dbPassword)) {
            Manager manager = new Manager();
            manager.firebaseId = doc.getId();
            manager.branchFirebaseId = doc.getString("branchFirebaseId");
            manager.branchId = getLongSafe(doc, "branchId");
            manager.name = doc.getString("name");
            manager.username = doc.getString("username");
            manager.phone = doc.getString("phone");
            manager.role = "manager";
            manager.syncStatus = 1;

            executor.execute(() -> {
                long id = db.managerDao().insert(manager);
                AppSession session = new AppSession();
                session.userId = id;
                session.branchId = manager.branchId;
                session.uuid = manager.firebaseId;
                session.isLoggedIn = true;
                session.role = "MANAGER";

                runOnUiThread(() -> progress.setIndeterminate(true));
                new SyncHelper(this).syncBranchData(manager.branchId, () -> {
                    simpanSession(session, DashboardManagerActivity.class);
                });
            });
        } else {
            loginGagal(getString(R.string.msg_login_failed));
        }
    }

    private void prosesLoginWaiter(DocumentSnapshot doc, String password) {
        String dbPassword = doc.getString("password");
        if (verifikasiPassword(password, dbPassword)) {
            Waiter waiter = new Waiter();
            waiter.firebaseId = doc.getId();
            waiter.branchFirebaseId = doc.getString("branchFirebaseId");
            waiter.branchId = getLongSafe(doc, "branchId");
            waiter.name = doc.getString("name");
            waiter.username = doc.getString("username");
            waiter.phone = doc.getString("phone");
            waiter.nik = doc.getString("nik");
            waiter.employeeNumber = doc.getString("employeeNumber");
            waiter.role = "waiter";
            waiter.syncStatus = 1;

            executor.execute(() -> {
                long id = db.waiterDao().insert(waiter);
                AppSession session = new AppSession();
                session.userId = id;
                session.branchId = waiter.branchId;
                session.uuid = waiter.firebaseId;
                session.isLoggedIn = true;
                session.role = "WAITER";

                runOnUiThread(() -> progress.setIndeterminate(true));
                new SyncHelper(this).syncBranchData(waiter.branchId, () -> {
                    simpanSession(session, DashboardWaiterActivity.class);
                });
            });
        } else {
            loginGagal(getString(R.string.msg_login_failed));
        }
    }

    private void prosesLoginKasir(DocumentSnapshot doc, String password) {
        String dbPassword = doc.getString("password");
        if (verifikasiPassword(password, dbPassword)) {
            AppSession session = new AppSession();
            session.branchId = getLongSafe(doc, "branchId");
            session.uuid = doc.getId();
            session.isLoggedIn = true;
            session.role = "CASHIER";
            simpanSession(session, DashboardCashierActivity.class);
        } else {
            loginGagal(getString(R.string.msg_login_failed));
        }
    }

    private void prosesLoginDapur(DocumentSnapshot doc, String password) {
        String dbPassword = doc.getString("password");
        if (verifikasiPassword(password, dbPassword)) {
            AppSession session = new AppSession();
            session.branchId = getLongSafe(doc, "branchId");
            session.uuid = doc.getId();
            session.isLoggedIn = true;
            session.role = "KITCHEN";
            simpanSession(session, DashboardKitchenActivity.class);
        } else {
            loginGagal(getString(R.string.msg_login_failed));
        }
    }

    private boolean verifikasiPassword(String plain, String hashed) {
        try {
            return BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified;
        } catch (Exception e) {
            return false;
        }
    }

    private void simpanSession(AppSession session, Class<?> target) {
        db.sessionDao().insert(session);
        navigateToDashboard(target);
    }

    private void navigateToDashboard(Class<?> target) {
        runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loginGagal(String pesan) {
        runOnUiThread(() -> {
            btnLogin.setEnabled(true);
            progress.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, pesan, Toast.LENGTH_SHORT).show();
        });
    }

    private long getLongSafe(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) return ((Number) val).longValue();
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}