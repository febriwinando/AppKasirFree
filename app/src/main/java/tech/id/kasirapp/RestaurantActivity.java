package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;


public class RestaurantActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialButton btnAddRestaurant;
    private LinearLayout branchMain1;
    private LinearLayout branchSecond1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_restaurant);

        initView();

        setupToolbar();

        setupClick();

    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);

        btnAddRestaurant =

                findViewById(R.id.btnAddRestaurant);

        branchMain1 =

                findViewById(R.id.branchMain1);

        branchSecond1 =

                findViewById(R.id.branchSecond1);

    }

    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(v -> {

            finish();

        });

    }

    private void setupClick() {

        // Tambah restoran

        btnAddRestaurant.setOnClickListener(v -> {

            Intent intent = new Intent(

                    RestaurantActivity.this,

                    RegisterRestaurantActivity.class

            );

            startActivity(intent);

        });

        // Pilih Cabang Utama

        branchMain1.setOnClickListener(v -> {

            selectBranch(

                    1,

                    1

            );

        });

        // Pilih Cabang Medan

        branchSecond1.setOnClickListener(v -> {

            selectBranch(

                    1,

                    2

            );

        });

    }

    private void selectBranch(

            long restaurantId,

            long branchId

    ) {

        // Nanti kita simpan cabang aktif
        // ke AppSession / ActiveBranch table.
        // Contoh:
        //
        // restaurantId = 1
        // branchId = 2
        //
        // Kemudian Dashboard akan menggunakan
        // branchId tersebut untuk mengambil
        // produk, order, stok dan laporan.
    }

}