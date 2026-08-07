package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Owner;

public class RegisterOwnerActivity extends AppCompatActivity {

    TextInputEditText edtNamaOwner, edtUsername, edtEmail, edtNoHp, edtPassword, edtKonfirmasiPassword;
    MaterialButton btnRegister;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onStart() {
        super.onStart();
        checkSession();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        edtNamaOwner = findViewById(R.id.edtNamaOwner);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtNoHp = findViewById(R.id.edtNoHp);
        edtPassword = findViewById(R.id.edtPassword);
        edtKonfirmasiPassword = findViewById(R.id.edtKonfirmasiPassword);
        btnRegister = findViewById(R.id.btnRegister);


        btnRegister.setOnClickListener(v -> registerOwner());
    }

    private void registerOwner() {

        String nama = edtNamaOwner.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String konfirmasi = edtKonfirmasiPassword.getText().toString();
        String email = edtEmail.getText().toString().trim();
        String phone = edtNoHp.getText().toString().trim();

        if(nama.isEmpty()){
            edtNamaOwner.setError("Nama wajib diisi");
            return;
        }

        if(username.isEmpty()){
            edtUsername.setError("Username wajib diisi");
            return;
        }

        if(password.isEmpty()){
            edtPassword.setError("Password wajib diisi");
            return;
        }

        if(!password.equals(konfirmasi)){
            edtKonfirmasiPassword.setError("Password tidak sama");
            return;
        }

        Owner owner = new Owner();
        owner.uuid = UUID.randomUUID().toString();
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

        uploadOwner(id);

    }
    private void uploadOwner(long id) {

        Owner owner = DatabaseClient
                .getDatabase(this)
                .ownerDao()
                .getById(id);



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("owners")
                .add(owner)
                .addOnSuccessListener(documentReference -> {

                    // Simpan document ID Firestore
                    owner.firebaseId = documentReference.getId();
                    owner.syncStatus = 1;

                    DatabaseClient
                            .getDatabase(this)
                            .ownerDao()
                            .update(owner);

                    AppSession session = new AppSession();
                    session.ownerId = owner.id;
                    session.uuid = owner.uuid;
                    session.isLoggedIn = true;

                    DatabaseClient
                            .getDatabase(this)
                            .sessionDao()
                            .insert(session);

                    Toast.makeText(this,
                            "Registrasi berhasil",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(
                            RegisterOwnerActivity.this,
                            DashboardOwnerActivity.class);

                    intent.putExtra("owner_id", owner.id);

                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Data lokal berhasil disimpan, namun gagal sinkron ke Firebase",
                            Toast.LENGTH_LONG).show();

                    Log.e("Firestore", e.getMessage());

                });

    }
    private void checkSession(){

        executor.execute(() -> {


            AppSession session = DatabaseClient
                    .getDatabase(RegisterOwnerActivity.this)
                    .sessionDao()
                    .getSession();


            runOnUiThread(() -> {


                if(session != null && session.isLoggedIn){


                    Intent intent = new Intent(
                            RegisterOwnerActivity.this,
                            DashboardOwnerActivity.class
                    );


                    intent.putExtra(
                            "owner_id",
                            session.ownerId
                    );


                    startActivity(intent);

                    finish();


                }else{


                    // Belum login
                    // tampilkan form register

                }


            });


        });

    }

}