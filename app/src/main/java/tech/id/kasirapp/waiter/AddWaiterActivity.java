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
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
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
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waiter);

        db = DatabaseClient.getDatabase(this);
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
            v.setPadding(0, 0, 0, ime.bottom + systemBars.bottom);
            return insets;
        });

        setupAutoScroll(findViewById(R.id.scrollView));
    }

    private void setupAutoScroll(NestedScrollView scrollView) {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    int scrollTo = v.getTop() - 100;
                    if (scrollTo < 0) scrollTo = 0;
                    scrollView.smoothScrollTo(0, scrollTo);
                }, 150);
            }
        };

        edtFullName.setOnFocusChangeListener(focusListener);
        edtNik.setOnFocusChangeListener(focusListener);
        edtPhone.setOnFocusChangeListener(focusListener);
        edtAddress.setOnFocusChangeListener(focusListener);
        edtUsername.setOnFocusChangeListener(focusListener);
        edtPassword.setOnFocusChangeListener(focusListener);
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
            toolbar.inflateMenu(R.menu.menu_delete);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    showDeleteConfirmation();
                    return true;
                }
                return false;
            });
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showDeleteConfirmation() {
        StatusHelper.showConfirm(this, "Hapus Waiter", "Apakah Anda yakin ingin menghapus data pelayan ini? Foto profil dan data di server juga akan dihapus.", () -> {
            if (existingWaiter != null) {
                deleteWaiter();
            }
        });
    }

    private void deleteWaiter() {
        StatusHelper.showLoading(this, "Deleting waiter and assets...");
        
        FirebaseRepository repo = new FirebaseRepository();
        
        // 1. Delete Profile Image
        repo.deleteImage(existingWaiter.photoPath, new FirebaseRepository.OnCompleteListener() {
            @Override
            public void success() {
                // 2. Delete KTP Image
                repo.deleteImage(existingWaiter.ktpPhotoPath, new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void success() {
                        // 3. Delete from Firestore
                        repo.deleteWaiter(existingWaiter.firebaseId, new FirebaseRepository.OnCompleteListener() {
                            @Override
                            public void success() {
                                // 4. Delete from Room
                                executor.execute(() -> {
                                    db.waiterDao().deleteById(existingWaiter.id);
                                    
                                    // 5. Delete local files
                                    deleteLocalFile(existingWaiter.photoPath);
                                    deleteLocalFile(existingWaiter.ktpPhotoPath);
                                    
                                    runOnUiThread(() -> {
                                        StatusHelper.hideLoading();
                                        StatusHelper.showSuccess(AddWaiterActivity.this, "Berhasil", "Data pelayan telah dihapus", AddWaiterActivity.this::finish);
                                    });
                                });
                            }

                            @Override
                            public void failed(String error) {
                                runOnUiThread(() -> {
                                    StatusHelper.hideLoading();
                                    Toast.makeText(AddWaiterActivity.this, "Gagal hapus di server: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void failed(String error) {
                        runOnUiThread(() -> {
                            StatusHelper.hideLoading();
                            Toast.makeText(AddWaiterActivity.this, "Gagal hapus data KTP: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void failed(String error) {
                runOnUiThread(() -> {
                    StatusHelper.hideLoading();
                    Toast.makeText(AddWaiterActivity.this, "Gagal hapus foto profil: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteLocalFile(String path) {
        if (path != null && !path.startsWith("http")) {
            try {
                File file = new File(Uri.parse(path).getPath());
                if (file.exists()) file.delete();
            } catch (Exception ignored) {}
        }
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
                    edtPassword.setText("");
                    spinnerStatus.setText(existingWaiter.isActive ? "Aktif" : "Tidak Aktif", false);

                    if (existingWaiter.photoPath != null && !existingWaiter.photoPath.isEmpty()) {
                        try {
                            imgWaiter.setImageURI(Uri.parse(existingWaiter.photoPath));
                            imgWaiter.setImageTintList(null);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    if (existingWaiter.ktpPhotoPath != null && !existingWaiter.ktpPhotoPath.isEmpty()) {
                        try {
                            imgKtp.setImageURI(Uri.parse(existingWaiter.ktpPhotoPath));
                            imgKtp.setImageTintList(null);
                        } catch (Exception e) { e.printStackTrace(); }
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
        spinnerEducation.setOnTouchListener((v, event) -> { hideKeyboard(); v.performClick(); return true; });
    }

    private void setupStatusSpinner() {
        String[] statuses = {"Aktif", "Tidak Aktif"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerStatus.setAdapter(adapter);
        spinnerStatus.setText(statuses[0], false);
        spinnerStatus.setOnTouchListener((v, event) -> { hideKeyboard(); v.performClick(); return true; });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadBranchInfo() {
        executor.execute(() -> {
            if (branchId == 0) {
                AppSession session = db.sessionDao().getSession();
                if (session != null) branchId = session.branchId;
            }
            Branch branch = db.branchDao().getById(branchId);
            if (branch != null) restaurantId = branch.restaurantId;
            
            if (waiterId == -1) {
                int count = db.waiterDao().countByBranchId(branchId);
                String generatedCode = String.format(Locale.getDefault(), "RES%d-BR%d-W%03d", restaurantId, branchId, count + 1);
                runOnUiThread(() -> edtEmployeeNumber.setText(generatedCode));
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
            Toast.makeText(this, "Mohon lengkapi data wajib", Toast.LENGTH_SHORT).show();
            return;
        }

        if (waiterId == -1 && password.isEmpty()) {
            edtPassword.setError("Password wajib diisi");
            return;
        }

        if (waiterId != -1) {
            StatusHelper.showConfirm(this, "Simpan", "Simpan perubahan data waiter?", () -> executeSave(name, nik, phone, address, education, employeeNumber, username, password));
        } else {
            executeSave(name, nik, phone, address, education, employeeNumber, username, password);
        }
    }

    private void executeSave(String name, String nik, String phone, String address, String education, String employeeNumber, String username, String password) {
        StatusHelper.showLoading(this, getString(R.string.msg_loading_save));

        executor.execute(() -> {
            Waiter waiter = (waiterId != -1 && existingWaiter != null) ? existingWaiter : new Waiter();
            waiter.branchId = branchId;
            waiter.name = name;
            waiter.nik = nik;
            waiter.phone = phone;
            waiter.address = address;
            waiter.education = education;
            waiter.employeeNumber = employeeNumber;
            waiter.username = username;
            
            if (!password.isEmpty()) {
                waiter.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            }

            waiter.isActive = "Aktif".equals(spinnerStatus.getText().toString());
            if (selectedImageUri != null) waiter.photoPath = saveImageToInternalStorage(selectedImageUri, "profile");
            if (selectedKtpUri != null) waiter.ktpPhotoPath = saveImageToInternalStorage(selectedKtpUri, "ktp");
            
            waiter.syncStatus = 0;
            if (waiterId == -1) {
                waiter.id = db.waiterDao().insert(waiter);
            } else {
                db.waiterDao().update(waiter);
            }

            // Sync to Firebase (Storage then Firestore)
            String waiterUuid = waiter.firebaseId != null ? waiter.firebaseId : UUID.randomUUID().toString();
            waiter.firebaseId = waiterUuid;

            AppSession session = db.sessionDao().getSession();
            long resId = session != null ? session.restaurantId : 0;
            long ownId = session != null ? session.ownerId : 0;

            FirebaseRepository repo = new FirebaseRepository();
            final Waiter finalWaiter = waiter;

            repo.uploadImage("waiters/profile", waiterUuid, waiter.photoPath, new FirebaseRepository.OnImageUploadListener() {
                @Override
                public void success(String profileUrl) {
                    if (!profileUrl.isEmpty()) finalWaiter.photoPath = profileUrl;
                    repo.uploadImage("waiters/ktp", waiterUuid, finalWaiter.ktpPhotoPath, new FirebaseRepository.OnImageUploadListener() {
                        @Override
                        public void success(String ktpUrl) {
                            if (!ktpUrl.isEmpty()) finalWaiter.ktpPhotoPath = ktpUrl;
                            saveToFirestore(repo, finalWaiter, resId, ownId);
                        }
                        @Override
                        public void failed(String e) { saveToFirestore(repo, finalWaiter, resId, ownId); }
                    });
                }
                @Override
                public void failed(String e) { saveToFirestore(repo, finalWaiter, resId, ownId); }
            });

            runOnUiThread(() -> {
                StatusHelper.hideLoading();
                StatusHelper.showSuccess(this, "Berhasil", "Data waiter berhasil disimpan", this::finish);
            });
        });
    }

    private void saveToFirestore(FirebaseRepository repo, Waiter waiter, long resId, long ownId) {
        repo.saveWaiter(waiter.firebaseId, waiter.branchFirebaseId != null ? waiter.branchFirebaseId : "", waiter.branchId, waiter.name, waiter.username, waiter.password, waiter.phone, waiter.nik, waiter.address, waiter.education, waiter.photoPath, waiter.ktpPhotoPath, waiter.employeeNumber, waiter.role, waiter.isActive, waiter.syncStatus, resId, ownId, new FirebaseRepository.OnCompleteListener() {
            @Override
            public void success() {
                executor.execute(() -> {
                    waiter.syncStatus = 1;
                    db.waiterDao().update(waiter);
                });
            }
            @Override
            public void failed(String error) {
                executor.execute(() -> {
                    waiter.syncStatus = 2;
                    db.waiterDao().update(waiter);
                });
            }
        });
    }

    private String saveImageToInternalStorage(Uri uri, String type) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "waiter_" + type + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream os = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) os.write(buf, 0, r);
            os.close(); is.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) { return uri.toString(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}