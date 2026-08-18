package tech.id.kasirapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.owner.BranchSettingsActivity;
import tech.id.kasirapp.CashierActivity;
import tech.id.kasirapp.EmployeeActivity;
import tech.id.kasirapp.kitchenstaf.KitchenActivity;
import tech.id.kasirapp.LoginActivity;
import tech.id.kasirapp.OrderActivity;
import tech.id.kasirapp.product.ProductActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.ReportActivity;
import tech.id.kasirapp.StockActivity;
import tech.id.kasirapp.WaiterActivity;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Restaurant;

public class DashboardManagerActivity extends AppCompatActivity {

    private TextView tvNamaManager;
    private TextView tvNamaRestoran;
    private TextView tvNamaCabang;
    private TextView tvAlamatCabang;

    private TextView tvJumlahPesanan;
    private TextView tvJumlahProduk;
    private TextView tvJumlahKasir;
    private TextView tvJumlahWaiter;

    private MaterialCardView cardPesanan;
    private MaterialCardView cardProduk;
    private MaterialCardView cardStok;
    private MaterialCardView cardKasir;
    private MaterialCardView cardWaiter;
    private MaterialCardView cardDapur;
    private MaterialCardView cardLaporan;
    private MaterialCardView cardPengaturan, cardPegawai;

    private MaterialButton btnLogout;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private long branchId;
    private long restaurantId;
    private long ownerId;
    private long managerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_dashboard_manager
        );

        db = DatabaseClient.getDatabase(this);

        initView();

        loadSession();
    }

    private void initView() {

        tvNamaManager =
                findViewById(R.id.tvNamaManager);

        tvNamaRestoran =
                findViewById(R.id.tvNamaRestoran);

        tvNamaCabang =
                findViewById(R.id.tvNamaCabang);

        tvAlamatCabang =
                findViewById(R.id.tvAlamatCabang);

        tvJumlahPesanan =
                findViewById(R.id.tvJumlahPesanan);

        tvJumlahProduk =
                findViewById(R.id.tvJumlahProduk);

        tvJumlahKasir =
                findViewById(R.id.tvJumlahKasir);

        tvJumlahWaiter =
                findViewById(R.id.tvJumlahWaiter);

        cardPesanan =
                findViewById(R.id.cardPesanan);

        cardProduk =
                findViewById(R.id.cardProduk);

        cardStok =
                findViewById(R.id.cardStok);

        cardKasir =
                findViewById(R.id.cardKasir);

        cardWaiter =
                findViewById(R.id.cardWaiter);

        cardDapur =
                findViewById(R.id.cardDapur);

        cardLaporan =
                findViewById(R.id.cardLaporan);

        cardPengaturan =
                findViewById(R.id.cardPengaturan);

        cardPegawai = findViewById(R.id.cardPegawai);

        btnLogout =
                findViewById(R.id.btnLogout);

        setupClick();
    }

    // =========================================================
    // SESSION
    // =========================================================

    private void loadSession() {

        executor.execute(() -> {

            AppSession session =
                    db.sessionDao().getSession();

            if (session == null ||
                    !session.isLoggedIn ||
                    !"MANAGER".equals(session.role)) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            DashboardManagerActivity.this,
                            "Session tidak valid",
                            Toast.LENGTH_SHORT
                    ).show();

                    kembaliKeLogin();

                });

                return;
            }

            managerId =
                    session.userId;

            ownerId =
                    session.ownerId;

            restaurantId =
                    session.restaurantId;

            branchId =
                    session.branchId;

            loadBranchData();

        });
    }

    // =========================================================
    // DATA CABANG
    // =========================================================

    private void loadBranchData() {

        executor.execute(() -> {

            Branch branch =
                    db.branchDao()
                            .getById(branchId);

            Restaurant restaurant =
                    db.restaurantDao()
                            .getById(restaurantId);

            runOnUiThread(() -> {

                if (branch == null) {

                    Toast.makeText(
                            DashboardManagerActivity.this,
                            "Data cabang tidak ditemukan",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                if (restaurant != null) {

                    tvNamaRestoran.setText(
                            restaurant.name
                    );

                }

                tvNamaCabang.setText(
                        branch.name
                );

                tvAlamatCabang.setText(
                        branch.address == null
                                ? ""
                                : branch.address
                );

                loadManagerName();

            });
        });
    }

    // =========================================================
    // NAMA MANAGER
    // =========================================================

    private void loadManagerName() {

        executor.execute(() -> {

            // Jika ManagerDao Anda sudah memiliki getById()
            tech.id.kasirapp.data.local.entity.Manager manager =
                    db.managerDao()
                            .getById(managerId);

            runOnUiThread(() -> {

                if (manager != null) {

                    tvNamaManager.setText(
                            "Halo, " + manager.name
                    );

                } else {

                    tvNamaManager.setText(
                            "Halo, Manager"
                    );
                }

            });
        });
    }

    // =========================================================
    // CLICK
    // =========================================================

    private void setupClick() {

        cardPesanan.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            OrderActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardProduk.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            ProductActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardStok.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            StockActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardKasir.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            CashierActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardWaiter.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            WaiterActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardDapur.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            KitchenActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardLaporan.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            ReportActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            startActivity(intent);
        });

        cardPegawai.setOnClickListener(v -> {

            Intent intent =

                    new Intent(

                            this,

                            EmployeeActivity.class

                    );
            intent.putExtra(
                    "branch_id",
                    branchId
            );

            intent.putExtra(
                    "restaurant_id",
                    restaurantId
            );

            startActivity(intent);

        });

        cardPengaturan.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            BranchSettingsActivity.class
                    );

            intent.putExtra(
                    "branch_id",
                    branchId
            );

            intent.putExtra(
                    "restaurant_id",
                    restaurantId
            );

            startActivity(intent);
        });

        btnLogout.setOnClickListener(
                v -> logout()
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private void logout() {

        executor.execute(() -> {

            db.sessionDao().logout();

            runOnUiThread(() -> {

                Intent intent =
                        new Intent(
                                DashboardManagerActivity.this,
                                LoginActivity.class
                        );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);

                finish();

            });
        });
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void kembaliKeLogin() {

        Intent intent =
                new Intent(
                        this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}