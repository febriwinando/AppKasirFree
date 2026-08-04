package tech.id.kasirapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;


public class RegisterBranchActivity extends AppCompatActivity {


    TextInputEditText edtNamaCabang;
    TextInputEditText edtAlamatCabang;
    TextInputEditText edtTeleponCabang;
    TextInputEditText edtJamBuka;
    TextInputEditText edtJamTutup;


    MaterialButton btnSimpan;



    String namaRestoran;
    String pemilik;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_branch);

        namaRestoran = getIntent().getStringExtra("nama_restoran");
        pemilik = getIntent().getStringExtra("pemilik");
        edtNamaCabang = findViewById(R.id.edtNamaCabang);
        edtAlamatCabang = findViewById(R.id.edtAlamatCabang);
        edtTeleponCabang = findViewById(R.id.edtTeleponCabang);
        edtJamBuka = findViewById(R.id.edtJamBuka);
        edtJamTutup = findViewById(R.id.edtJamTutup);
        btnSimpan = findViewById(R.id.btnSimpan);

        btnSimpan.setOnClickListener(v -> {
            if(edtNamaCabang.getText().toString().isEmpty()){
                edtNamaCabang.setError("Nama cabang wajib diisi");
                return;
            }

            simpanData();
        });
    }

    private void simpanData(){


        AppDatabase db =
                DatabaseClient
                        .getDatabase(this);



        Branch branch =
                new Branch();



        branch.restaurantId =
                getIntent()
                        .getLongExtra(
                                "restaurant_id",
                                0
                        );



        branch.name =
                edtNamaCabang
                        .getText()
                        .toString();



        branch.address =
                edtAlamatCabang
                        .getText()
                        .toString();



        branch.phone =
                edtTeleponCabang
                        .getText()
                        .toString();



        branch.openTime =
                edtJamBuka
                        .getText()
                        .toString();



        branch.closeTime =
                edtJamTutup
                        .getText()
                        .toString();



        branch.isMain=true;


        branch.syncStatus=0;



        long id =
                db.branchDao()
                        .insert(branch);




        Toast.makeText(
                this,
                "Restoran berhasil dibuat",
                Toast.LENGTH_SHORT
        ).show();



        startActivity(
                new Intent(
                        this,
                        DashboardOwnerActivity.class
                )
        );


        finish();


    }
//    private void simpanData(){
//        AppDatabase db = DatabaseClient.getDatabase(this);
//        Branch branch = new Branch();
//        branch.restaurantId = getIntent().getLongExtra("restaurant_id", 0);
//        branch.name = edtNamaCabang.getText().toString();
//        branch.address = edtAlamatCabang.getText().toString();
//        branch.phone = edtTeleponCabang.getText().toString();
//        branch.openTime = edtJamBuka.getText().toString();
//        branch.closeTime = edtJamTutup.getText().toString();
//        branch.isMain=true;
//        db.branchDao().insert(branch);
//
//        Toast.makeText(
//                this,
//                "Restoran berhasil dibuat",
//                Toast.LENGTH_SHORT
//        ).show();
//
//
//        Intent intent = new Intent(this, DashboardOwnerActivity.class);
//        startActivity(intent);
//        finish();
//    }
}