package tech.id.kasirapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Owner;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText edtNamaOwner, edtUsername, edtEmail, edtNoHp, edtPassword, edtKonfirmasiPassword;
    MaterialButton btnRegister;
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

        owner.name = nama;
        owner.username = username;
//        owner.password = password;
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

    private void uploadOwner(long id){

        Owner owner = DatabaseClient
                .getDatabase(this)
                .ownerDao()
                .getById(id);

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("owners");

        String firebaseId = ref.push().getKey();

        HashMap<String,Object> data = new HashMap<>();

        data.put("firebaseId",firebaseId);
        data.put("name",owner.name);
        data.put("username",owner.username);
        data.put("email",owner.email);
        data.put("phone",owner.phone);

        ref.child(firebaseId)
                .setValue(data)
                .addOnSuccessListener(unused -> {

                    DatabaseClient
                            .getDatabase(this)
                            .ownerDao()
                            .updateFirebase(id,firebaseId,1);

                    Toast.makeText(
                            this,
                            "Registrasi berhasil",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    DatabaseClient
                            .getDatabase(this)
                            .ownerDao()
                            .updateSyncStatus(id,2);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                });

    }
}