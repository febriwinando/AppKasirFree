package tech.id.kasirapp.manager;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import tech.id.kasirapp.R;

public class ManagerSettingsActivity extends AppCompatActivity {

    private MaterialCardView cardAturMeja;
    private LinearLayout rowDiskon;
    private LinearLayout rowPaketCombo;
    private LinearLayout rowPromo;
    private LinearLayout rowPrinterSetup;
    private LinearLayout rowShiftKasir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_settings);

        initView();
        setupToolbar();
        setupClickListeners();

        // Menerapkan animasi slide_up pada root view agar terlihat premium
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (findViewById(R.id.toolbar) != null) {
            findViewById(R.id.toolbar).startAnimation(slideUp);
        }
    }

    private void initView() {
        cardAturMeja = findViewById(R.id.cardAturMeja);
        rowDiskon = findViewById(R.id.rowDiskon);
        rowPaketCombo = findViewById(R.id.rowPaketCombo);
        rowPromo = findViewById(R.id.rowPromo);
        rowPrinterSetup = findViewById(R.id.rowPrinterSetup);
        rowShiftKasir = findViewById(R.id.rowShiftKasir);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupClickListeners() {
        cardAturMeja.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Manajemen Meja Segera Hadir!", Toast.LENGTH_SHORT).show()
        );

        rowDiskon.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Pengaturan Diskon Segera Hadir!", Toast.LENGTH_SHORT).show()
        );

        rowPaketCombo.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Menu Paket Combo Segera Hadir!", Toast.LENGTH_SHORT).show()
        );

        rowPromo.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Promo & Voucher Segera Hadir!", Toast.LENGTH_SHORT).show()
        );

        rowPrinterSetup.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Konfigurasi Printer Segera Hadir!", Toast.LENGTH_SHORT).show()
        );

        rowShiftKasir.setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Kontrol Shift Kasir Segera Hadir!", Toast.LENGTH_SHORT).show()
        );
    }
}