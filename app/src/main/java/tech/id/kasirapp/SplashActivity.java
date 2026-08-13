package tech.id.kasirapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;


public class SplashActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        checkSession();
    }

    private void checkSession() {
        executor.execute(() -> {
            AppSession session = DatabaseClient
                    .getDatabase(SplashActivity.this)
                    .sessionDao()
                    .getSession();

            runOnUiThread(() -> {
                if (session != null && session.isLoggedIn) {
                    // Sudah login
                    Intent intent = new Intent(
                            SplashActivity.this,
                            DashboardOwnerActivity.class

                    );
                    intent.putExtra(
                            "owner_id",
                            session.ownerId
                    );

                    startActivity(intent);

                } else {

                    // Belum login
                    Intent intent = new Intent(
                            SplashActivity.this,
                            RegisterOwnerActivity.class
                    );
                    startActivity(intent);

                }
                // Splash tidak boleh kembali lagi
                finish();

            });

        });

    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

}