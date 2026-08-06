package tech.id.kasirapp;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Map<String, Object> owner = new HashMap<>();
//        owner.put("name", "Febri");
//        owner.put("username", "febri");
//        owner.put("email", "test@gmail.com");

        db.collection("owners")
                .add(owner)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "ID : " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", e.getMessage());
                });

    }
}