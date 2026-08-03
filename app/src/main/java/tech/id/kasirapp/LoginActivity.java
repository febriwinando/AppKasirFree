package tech.id.kasirapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;


public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtUsername, edtPassword;
    Button btnLogin;
    ProgressBar progress;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);

        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);

        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> {

            String username =
                    edtUsername.getText()
                            .toString()
                            .trim();
            String password =
                    edtPassword.getText()
                            .toString()
                            .trim();

            if(username.isEmpty()){
                edtUsername.setError(
                        "Username wajib diisi"
                );

                return;

            }

            if(password.isEmpty()){
                edtPassword.setError(
                        "Password wajib diisi"
                );

                return;
            }

            prosesLogin(username,password);
        });
    }

    private void prosesLogin(
            String username,
            String password
    ){

        btnLogin.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        // simulasi request API
        new Handler().postDelayed(() -> {
            progress.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            if(username.equals("admin") && password.equals("123456")){

                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();

                // pindah dashboard
                /*
                startActivity(
                    new Intent(
                      this,
                      DashboardActivity.class
                    )
                );
                */

            }else{
                Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show();
            }
        },1500);

    }

}