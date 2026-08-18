package tech.id.kasirapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Restaurant;

public class EditBranchActivity extends AppCompatActivity {

    private TextInputEditText edtNamaCabang;
    private TextInputEditText edtAlamatCabang;
    private TextInputEditText edtTeleponCabang;
    private TextInputEditText edtJamBuka;
    private TextInputEditText edtJamTutup;

    private MaterialButton btnSimpan;

    private long restaurantId;
    private long branchId;

    private Branch branch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_branch
        );

        restaurantId =
                getIntent().getLongExtra(
                        "restaurant_id",
                        0
                );

        branchId =
                getIntent().getLongExtra(
                        "branch_id",
                        0
                );

        initView();

        loadBranch();

        setupClick();
    }

    private void initView() {

        edtNamaCabang =
                findViewById(R.id.edtNamaCabang);

        edtAlamatCabang =
                findViewById(R.id.edtAlamatCabang);

        edtTeleponCabang =
                findViewById(R.id.edtTeleponCabang);

        edtJamBuka =
                findViewById(R.id.edtJamBuka);

        edtJamTutup =
                findViewById(R.id.edtJamTutup);

        btnSimpan =
                findViewById(R.id.btnSimpan);
    }

    private void loadBranch() {

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        new Thread(() -> {

            Branch data =
                    db.branchDao()
                            .getById(branchId);

            runOnUiThread(() -> {

                if (data == null) {

                    Toast.makeText(
                            this,
                            "Data cabang tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                    return;
                }

                branch = data;

                edtNamaCabang.setText(
                        data.name
                );

                edtAlamatCabang.setText(
                        data.address
                );

                edtTeleponCabang.setText(
                        data.phone
                );

                edtJamBuka.setText(
                        data.openTime
                );

                edtJamTutup.setText(
                        data.closeTime
                );
            });

        }).start();
    }

    private void setupClick() {

        edtJamBuka.setOnClickListener(v ->
                showTimePicker(true)
        );

        edtJamTutup.setOnClickListener(v ->
                showTimePicker(false)
        );

        btnSimpan.setOnClickListener(v -> {

            if (edtNamaCabang.getText()
                    .toString()
                    .trim()
                    .isEmpty()) {

                edtNamaCabang.setError(
                        "Nama cabang wajib diisi"
                );

                return;
            }

            if (edtJamBuka.getText()
                    .toString()
                    .trim()
                    .isEmpty()) {

                edtJamBuka.setError(
                        "Jam buka wajib dipilih"
                );

                return;
            }

            if (edtJamTutup.getText()
                    .toString()
                    .trim()
                    .isEmpty()) {

                edtJamTutup.setError(
                        "Jam tutup wajib dipilih"
                );

                return;
            }

            updateBranch();
        });
    }

    private void showTimePicker(boolean jamBuka) {

        TextInputEditText target =
                jamBuka
                        ? edtJamBuka
                        : edtJamTutup;

        int hour = 8;
        int minute = 0;

        String value =
                target.getText()
                        .toString();

        if (!value.isEmpty()) {

            try {

                String[] waktu =
                        value.split(":");

                hour =
                        Integer.parseInt(
                                waktu[0]
                        );

                minute =
                        Integer.parseInt(
                                waktu[1]
                        );

            } catch (Exception ignored) {
            }
        }

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(
                                TimeFormat.CLOCK_24H
                        )
                        .setHour(hour)
                        .setMinute(minute)
                        .setTitleText(
                                jamBuka
                                        ? "Pilih Jam Buka"
                                        : "Pilih Jam Tutup"
                        )
                        .setInputMode(
                                MaterialTimePicker.INPUT_MODE_CLOCK
                        )
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {

            String waktu =
                    String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            picker.getHour(),
                            picker.getMinute()
                    );

            target.setText(waktu);
        });

        picker.show(
                getSupportFragmentManager(),
                jamBuka
                        ? "EDIT_TIME_BUKA"
                        : "EDIT_TIME_TUTUP"
        );
    }

    private void updateBranch() {

        if (branch == null) {
            return;
        }

        branch.name =
                edtNamaCabang
                        .getText()
                        .toString()
                        .trim();

        branch.address =
                edtAlamatCabang
                        .getText()
                        .toString()
                        .trim();

        branch.phone =
                edtTeleponCabang
                        .getText()
                        .toString()
                        .trim();

        branch.openTime =
                edtJamBuka
                        .getText()
                        .toString()
                        .trim();

        branch.closeTime =
                edtJamTutup
                        .getText()
                        .toString()
                        .trim();

        // Tandai perlu sinkronisasi
        branch.syncStatus = 0;

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        new Thread(() -> {

            db.branchDao()
                    .update(branch);

            Restaurant restaurant =
                    db.restaurantDao()
                            .getById(
                                    restaurantId
                            );

            runOnUiThread(() -> {

                if (restaurant == null) {

                    Toast.makeText(
                            this,
                            "Restoran tidak ditemukan",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                syncToFirebase(
                        restaurant
                );
            });

        }).start();
    }

    private void syncToFirebase(
            Restaurant restaurant
    ) {

        FirebaseRepository firebase =
                new FirebaseRepository();

        firebase.saveBranch(
                branch.firebaseId,
                restaurant.firebaseId,
                branch.name,
                branch.restaurantId,
                branch.address,
                branch.phone,
                branch.openTime,
                branch.closeTime,
                branch.isMain,
                new FirebaseRepository.OnCompleteListener() {

                    @Override
                    public void success() {

                        AppDatabase db =
                                DatabaseClient
                                        .getDatabase(
                                                EditBranchActivity.this
                                        );

                        new Thread(() -> {

                            db.branchDao()
                                    .updateSyncStatus(
                                            branch.id,
                                            1
                                    );

                            runOnUiThread(() -> {

                                Toast.makeText(
                                        EditBranchActivity.this,
                                        "Cabang berhasil diperbarui",
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

                        Toast.makeText(
                                EditBranchActivity.this,
                                "Data tersimpan lokal, tetapi gagal sinkron ke Firebase",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                }
        );
    }
}