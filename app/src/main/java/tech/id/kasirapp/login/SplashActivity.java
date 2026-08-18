package tech.id.kasirapp.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.dashboard.DashboardCashierActivity;
import tech.id.kasirapp.dashboard.DashboardKitchenActivity;
import tech.id.kasirapp.dashboard.DashboardManagerActivity;
import tech.id.kasirapp.dashboard.DashboardOwnerActivity;
import tech.id.kasirapp.dashboard.DashboardWaiterActivity;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.register.RegisterOwnerActivity;


public class SplashActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        checkSession();
    }

    private void checkSession() {

        executor.execute(() -> {

            AppSession session =
                    DatabaseClient
                            .getDatabase(SplashActivity.this)
                            .sessionDao()
                            .getSession();

            runOnUiThread(() -> {

                // =========================================
                // BELUM LOGIN
                // =========================================

                if (session == null || !session.isLoggedIn) {

                    Intent intent =
                            new Intent(
                                    SplashActivity.this,
                                    LoginActivity.class
                            );

                    startActivity(intent);

                    finish();

                    return;
                }


                // =========================================
                // SUDAH LOGIN
                // =========================================

                Intent intent;


                // =========================================
                // OWNER
                // =========================================

                if ("OWNER".equals(session.role)) {

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    DashboardOwnerActivity.class
                            );

                    intent.putExtra(
                            "owner_id",
                            session.ownerId
                    );

                    intent.putExtra(
                            "restaurant_id",
                            session.restaurantId
                    );

                    intent.putExtra(
                            "branch_id",
                            session.branchId
                    );

                }


                // =========================================
                // MANAGER
                // =========================================

                else if ("MANAGER".equals(session.role)) {

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    DashboardManagerActivity.class
                            );

                    intent.putExtra(
                            "owner_id",
                            session.ownerId
                    );

                    intent.putExtra(
                            "restaurant_id",
                            session.restaurantId
                    );

                    intent.putExtra(
                            "branch_id",
                            session.branchId
                    );

                }


                // =========================================
                // CASHIER
                // =========================================

                else if ("CASHIER".equals(session.role)) {

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    DashboardCashierActivity.class
                            );

                    intent.putExtra(
                            "branch_id",
                            session.branchId
                    );

                }


                // =========================================
                // WAITER
                // =========================================

                else if ("WAITER".equals(session.role)) {

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    DashboardWaiterActivity.class
                            );

                    intent.putExtra(
                            "branch_id",
                            session.branchId
                    );

                }


                // =========================================
                // KITCHEN
                // =========================================

                else if ("KITCHEN".equals(session.role)) {

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    DashboardKitchenActivity.class
                            );

                    intent.putExtra(
                            "branch_id",
                            session.branchId
                    );

                }


                // =========================================
                // ROLE TIDAK DIKENAL
                // =========================================

                else {

                    // Session tidak valid
                    DatabaseClient
                            .getDatabase(SplashActivity.this)
                            .sessionDao()
                            .logout();

                    intent =
                            new Intent(
                                    SplashActivity.this,
                                    RegisterOwnerActivity.class
                            );

                }


                startActivity(intent);

                // Splash tidak boleh kembali
                finish();

            });

        });
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

}