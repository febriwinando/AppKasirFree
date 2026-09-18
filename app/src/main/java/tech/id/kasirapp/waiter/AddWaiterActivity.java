package tech.id.kasirapp.waiter;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Waiter;
import tech.id.kasirapp.util.StatusHelper;

public class AddWaiterActivity extends AppCompatActivity {

    private ImageView imgWaiter, imgKtp;
    private TextInputEditText edtFullName, edtNik, edtPhone, edtAddress, edtEmployeeNumber, edtUsername, edtPassword;
    private AutoCompleteTextView spinnerEducation, spinnerStatus;
    private MaterialButton btnSave;
    
    private Uri selectedImageUri, selectedKtpUri;
    private AppDatabase db;
    private FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId;
    private long restaurantId;
    private long waiterId = -1;
    private Waiter existingWaiter;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgWaiter.setImageURI(uri);
                    imgWaiter.setImageTintList(null);
                }
            }
    );

    private final ActivityResultLauncher<String> pickKtpLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedKtpUri = uri;
                    imgKtp.setImageURI(uri);
                    imgKtp.setImageTintList(null);
                    // Hide the overlay text if needed, or just let the image cover it
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waiter);

        db = DatabaseClient.getDatabase(this);
        firestore = FirebaseFirestore.getInstance();
        branchId = getIntent().getLongExtra("branch_id", 0);
        waiterId = getIntent().getLongExtra("waiter_id", -1);

        initView();
        setupToolbar();
        setupEducationSpinner();
        
        loadBranchInfo();
        setupStatusSpinner();
        if (waiterId != -1) {
            checkAndLoadEditData();
        }

        View appBarLayout = findViewById(R.id.appBarLayout);
        if (appBarLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Menambahkan padding bawah sesuai tinggi keyboard (ime) + navigasi sistem
            v.setPadding(0, 0, 0, ime.bottom + systemBars.bottom);
            
            return insets;
        });
    }

    private void initView() {
        imgWaiter = findViewById(R.id.imgWaiter);
        imgKtp = findViewById(R.id.imgKtp);
        edtFullName = findViewById(R.id.edtFullName);
        edtNik = findViewById(R.id.edtNik);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtEmployeeNumber = findViewById(R.id.edtEmployeeNumber);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        spinnerEducation = findViewById(R.id.spinnerEducation);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);

        if (waiterId != -1) {
            btnSave.setText("Simpan Perubahan");
        } else {
            edtEmployeeNumber.setText("Menghasilkan...");
        }

        findViewById(R.id.cardPhoto).setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        findViewById(R.id.cardPhotoKtp).setOnClickListener(v -> pickKtpLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveWaiter());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (waiterId != -1) {
            toolbar.setTitle("Edit Waiter");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void checkAndLoadEditData() {
        executor.execute(() -> {
            existingWaiter = db.waiterDao().getById(waiterId);
            if (existingWaiter != null) {
                runOnUiThread(() -> {
                    edtFullName.setText(existingWaiter.name != null ? existingWaiter.name : "");
                    edtNik.setText(existingWaiter.nik != null ? existingWaiter.nik : "");
                    edtPhone.setText(existingWaiter.phone != null ? existingWaiter.phone : "");
                    edtAddress.setText(existingWaiter.address != null ? existingWaiter.address : "");
                    edtEmployeeNumber.setText(existingWaiter.employeeNumber != null ? existingWaiter.employeeNumber : "");
                    edtUsername.setText(existingWaiter.username != null ? existingWaiter.username : "");
                    // Password field biarkan kosong saat edit untuk keamanan, 
                    // hanya diisi jika ingin mengubah password.
                    edtPassword.setText("");
                    
                    spinnerStatus.setText(existingWaiter.isActive ? "Aktif" : "Tidak Aktif", false);

                    if (existingWaiter.photoPath != null && !existingWaiter.photoPath.isEmpty()) {
                        try {
                            imgWaiter.setImageURI(Uri.parse(existingWaiter.photoPath));
                            imgWaiter.setImageTintList(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    if (existingWaiter.ktpPhotoPath != null && !existingWaiter.ktpPhotoPath.isEmpty()) {
                        try {
                            imgKtp.setImageURI(Uri.parse(existingWaiter.ktpPhotoPath));
                            imgKtp.setImageTintList(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Data waiter tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupEducationSpinner() {
        String[] educationLevels = {"SMP", "SMA", "S1", "S2", "S3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, educationLevels);
        spinnerEducation.setAdapter(adapter);
    }

    private void setupStatusSpinner() {
        String[] statuses = {"Aktif", "Tidak Aktif"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerStatus.setAdapter(adapter);
        spinnerStatus.setText(statuses[0], false); // Default Aktif
    }

    private void loadBranchInfo() {
        executor.execute(() -> {
            // Jika branchId masih 0, coba ambil dari session
            if (branchId == 0) {
                AppSession session = db.sessionDao().getSession();
                if (session != null) {
                    branchId = session.branchId;
                }
            }

            Branch branch = db.branchDao().getById(branchId);
            long rId = 0;
            
            if (branch != null) {
                rId = branch.restaurantId;
            } else {
                // Fallback ke session jika branch null
                AppSession session = db.sessionDao().getSession();
                if (session != null) {
                    rId = session.restaurantId;
                    if (branchId == 0) branchId = session.branchId;
                }
            }

            restaurantId = rId;
            
            if (waiterId == -1) {
                int count = db.waiterDao().countByBranchId(branchId);
                String generatedCode = String.format(Locale.getDefault(), "RES%d-BR%d-W%03d", restaurantId, branchId, count + 1);
                
                runOnUiThread(() -> {
                    edtEmployeeNumber.setText(generatedCode);
                    Toast.makeText(this, "Nomor pegawai berhasil dibuat: " + generatedCode, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveWaiter() {
        String name = edtFullName.getText().toString().trim();
        String nik = edtNik.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String education = spinnerEducation.getText().toString().trim();
        String employeeNumber = edtEmployeeNumber.getText().toString().trim();
        String username = edtUsername.getText().toString().trim().toLowerCase();
        String password = edtPassword.getText().toString().trim();

        if (name.isEmpty() || nik.isEmpty() || phone.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi data wajib (Nama, NIK, HP, Username)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (waiterId == -1 && password.isEmpty()) {
            edtPassword.setError("Password wajib diisi untuk waiter baru");
            return;
        }

        if (waiterId != -1) {
            StatusHelper.showConfirm(this, "Simpan Perubahan", "Apakah Anda yakin ingin menyimpan perubahan data waiter ini?", () -> {
                executeSave(name, nik, phone, address, education, employeeNumber, username, password);
            });
        } else {
            executeSave(name, nik, phone, address, education, employeeNumber, username, password);
        }
    }

    private void executeSave(String name, String nik, String phone, String address, String education, String employeeNumber, String username, String password) {
        StatusHelper.showLoading(this, getString(R.string.msg_loading_save));

        executor.execute(() -> {
            Waiter waiter;
            if (waiterId != -1) {
                if (existingWaiter != null) {
                    waiter = existingWaiter;
                } else {
                    // Coba ambil lagi jika sebelumnya gagal
                    waiter = db.waiterDao().getById(waiterId);
                }
                
                if (waiter == null) {
                    runOnUiThread(() -> {
                        StatusHelper.hideLoading();
                        Toast.makeText(this, "Gagal memperbarui: Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
            } else {
                waiter = new Waiter();
            }

            waiter.branchId = branchId;
            waiter.name = capitalizeWords(name);
            waiter.nik = nik;
            waiter.phone = phone;
            waiter.address = capitalizeSentence(address);
            waiter.education = education;
            waiter.employeeNumber = employeeNumber;
            waiter.username = username;
            
            // Hash password if it's new or changed (not empty)
            if (!password.isEmpty()) {
                waiter.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            } else if (waiterId == -1) {
                // Should not happen due to validation above
                waiter.password = BCrypt.withDefaults().hashToString(12, "123456".toCharArray());
            }
            // else: password tetap yang lama (karena tidak diubah)

            waiter.isActive = "Aktif".equals(spinnerStatus.getText().toString());
            waiter.role = "waiter";
            
            if (selectedImageUri != null) {
                waiter.photoPath = saveImageToInternalStorage(selectedImageUri, "profile");
            }
            
            if (selectedKtpUri != null) {
                waiter.ktpPhotoPath = saveImageToInternalStorage(selectedKtpUri, "ktp");
            }
            
            if (waiterId == -1) {
                waiter.syncStatus = 0;
                long id = db.waiterDao().insert(waiter);
                waiter.id = id;
            } else {
                waiter.syncStatus = 0; // Mark for re-sync
                db.waiterDao().update(waiter);
            }

            // =========================
            // SYNC TO FIRESTORE
            // =========================
            
            String waiterUuid = waiter.firebaseId != null ? waiter.firebaseId : UUID.randomUUID().toString();
            waiter.firebaseId = waiterUuid;

            Map<String, Object> waiterData = new HashMap<>();
            waiterData.put("id", waiter.id);
            waiterData.put("firebaseId", waiterUuid);
            waiterData.put("name", waiter.name);
            waiterData.put("nik", waiter.nik);
            waiterData.put("phone", waiter.phone);
            waiterData.put("address", waiter.address);
            waiterData.put("education", waiter.education);
            waiterData.put("employeeNumber", waiter.employeeNumber);
            waiterData.put("username", waiter.username);
            waiterData.put("password", waiter.password);
            waiterData.put("isActive", waiter.isActive);
            waiterData.put("role", waiter.role);
            waiterData.put("branchId", waiter.branchId);
            waiterData.put("photoPath", waiter.photoPath);
            waiterData.put("ktpPhotoPath", waiter.ktpPhotoPath);
            
            // Get additional info from session for better filtering in Firestore
            AppSession session = db.sessionDao().getSession();
            if (session != null) {
                waiterData.put("restaurantId", session.restaurantId);
                waiterData.put("ownerId", session.ownerId);
            }

            firestore.collection("waiters")
                    .document(waiterUuid)
                    .set(waiterData)
                    .addOnSuccessListener(aVoid -> {
                        executor.execute(() -> {
                            waiter.syncStatus = 1;
                            db.waiterDao().update(waiter);
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });

            runOnUiThread(() -> {
                StatusHelper.hideLoading();
                StatusHelper.showSuccess(
                        this,
                        getString(R.string.dialog_success),
                        getString(R.string.msg_success_save),
                        this::finish
                );
            });
        });
    }

    private String saveImageToInternalStorage(Uri uri, String type) {
        if (uri == null) return null;
        if (!"content".equals(uri.getScheme())) {
            return uri.toString();
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(getFilesDir(), "waiter_" + type + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri.toString();
        }
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String capitalizeSentence(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : str.toLowerCase().toCharArray()) {
            if (Character.isLetter(c) && capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
                if (c == '.' || c == '!' || c == '?') {
                    capitalizeNext = true;
                }
            }
        }
        return sb.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}