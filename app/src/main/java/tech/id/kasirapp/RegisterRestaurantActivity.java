package tech.id.kasirapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Restaurant;


public class RegisterRestaurantActivity extends AppCompatActivity {
    TextInputEditText edtNamaRestoran;
    TextInputEditText edtPemilik;
    TextInputEditText edtTelepon;
    TextInputEditText edtEmail;
    TextInputEditText edtAlamat;
    MaterialButton btnLanjutCabang;
    AppSession session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_restaurant);

        session = DatabaseClient
                .getDatabase(RegisterRestaurantActivity.this)
                .sessionDao()
                .getSession();

        edtNamaRestoran = findViewById(R.id.edtNamaRestoran);
        edtPemilik = findViewById(R.id.edtPemilik);
        edtTelepon = findViewById(R.id.edtTelepon);
        edtEmail = findViewById(R.id.edtEmail);
        edtAlamat = findViewById(R.id.edtAlamat);
        btnLanjutCabang = findViewById(R.id.btnLanjutCabang);

        btnLanjutCabang.setOnClickListener(v -> {
            if(
                    edtNamaRestoran.getText()
                            .toString()
                            .isEmpty()
            ){edtNamaRestoran.setError(
                        "Nama restoran wajib diisi"
                );
                return;

            }



            AppDatabase db =
                    DatabaseClient
                            .getDatabase(this);



            Restaurant restaurant =
                    new Restaurant();



            restaurant.name =
                    edtNamaRestoran
                            .getText()
                            .toString();



            restaurant.ownerName =
                    edtPemilik
                            .getText()
                            .toString();



            restaurant.phone =
                    edtTelepon
                            .getText()
                            .toString();



            restaurant.email =
                    edtEmail
                            .getText()
                            .toString();



            restaurant.address =
                    edtAlamat
                            .getText()
                            .toString();



            restaurant.createdAt =
                    System.currentTimeMillis();



    /*
        buat ID Firebase
    */
            String firebaseId = java.util.UUID.randomUUID().toString();
            restaurant.firebaseId = firebaseId;
            restaurant.syncStatus = 0;

            long restaurantId = db.restaurantDao().insert(restaurant);




            FirebaseRepository firebase =
                    new FirebaseRepository();



            firebase.saveRestaurant(

                    firebaseId,
                    restaurant.name,
                    restaurant.ownerName,
                    restaurant.phone,
                    restaurant.email,
                    restaurant.address,
                    restaurant.ownerId = session.ownerId,
                    restaurant.isActive = true,
                    new FirebaseRepository.OnCompleteListener(){

                        @Override
                        public void success(){
                            restaurant.syncStatus=1;
                            db.restaurantDao()
                                    .updateSyncStatus(
                                            restaurantId,
                                            1
                                    );
                            bukaCabang(
                                    restaurantId
                            );
                        }

                        @Override
                        public void failed(
                                String error
                        ){
                            bukaCabang(
                                    restaurantId
                            );
                        }

                    }
            );

        });

    }

    private void bukaCabang(long id){
        Intent intent =
                new Intent(
                        this,
                        RegisterBranchActivity.class
                );


        intent.putExtra(
                "restaurant_id",
                id
        );


        startActivity(intent);


    }

}