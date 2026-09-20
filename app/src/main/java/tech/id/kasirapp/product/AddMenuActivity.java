package tech.id.kasirapp.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.util.StatusHelper;

public class AddMenuActivity extends AppCompatActivity {

    // =========================
    // FORM
    // =========================

    private TextInputEditText edtMenuName;
    private TextInputEditText edtSKU;
    private TextInputEditText edtPrice;
    private TextInputEditText edtDescription;

    private AutoCompleteTextView spinnerUnit;
    private AutoCompleteTextView spinnerMenuType;
    private AutoCompleteTextView spinnerCategory;
    private AutoCompleteTextView spinnerStatus;
    private AutoCompleteTextView spinnerAvailability;

    // =========================
    // UI
    // =========================

    private ImageView imgMenu;
    private View imgLogoOverlay;
    private MaterialCardView cardImage;
    private MaterialButton btnSave;

    // =========================
    // DATABASE
    // =========================

    private AppDatabase db;
    private FirebaseFirestore firestore;
    private AppSession session;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private long branchId;
    private long menuId = -1;
    private Menu existingMenu;

    // =========================
    // IMAGE
    // =========================

    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            selectedImageUri =
                                    result.getData().getData();

                            imgMenu.setImageURI(selectedImageUri);
                            imgMenu.setAlpha(1.0f);

                            if (imgLogoOverlay != null) {
                                imgLogoOverlay.setVisibility(View.GONE);
                            }
                        }
                    }
            );

    // =========================
    // ON CREATE
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_menu);

        // Database
        db = DatabaseClient.getDatabase(this);

        // Firestore
        firestore = FirebaseFirestore.getInstance();

        // Branch ID
        branchId = getIntent()
                .getLongExtra("branch_id", 0);

        // Menu ID for Edit mode
        menuId = getIntent().getLongExtra("menu_id", -1);

        // Load Session
        loadSession();

        initView();

        setupSpinners();

        setupWindowInsets();

        // Check if edit mode
        if (menuId != -1) {
            checkAndLoadEditData();
        }
    }

    private void loadSession() {
        executor.execute(() -> {
            session = db.sessionDao().getSession();
        });
    }

    // =========================
    // INIT VIEW
    // =========================

    private void initView() {

        edtMenuName = findViewById(R.id.edtMenuName);
        edtSKU = findViewById(R.id.edtSKU);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);

        generateAutoSKU();

        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerMenuType = findViewById(R.id.spinnerMenuType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerAvailability =
                findViewById(R.id.spinnerAvailability);

        imgMenu = findViewById(R.id.imgMenu);
        cardImage = findViewById(R.id.cardImage);
        btnSave = findViewById(R.id.btnSave);

        imgLogoOverlay =
                findViewById(R.id.imgLogoOverlay);

        // =========================
        // TOOLBAR
        // =========================

        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        if (toolbar != null) {
            if (menuId != -1) {
                toolbar.setTitle("Edit Menu");
                toolbar.inflateMenu(R.menu.menu_delete);
                toolbar.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        showDeleteConfirmation();
                        return true;
                    }
                    return false;
                });
            }
            toolbar.setNavigationOnClickListener(
                    v -> finish()
            );
        }

        // =========================
        // IMAGE PICKER
        // =========================

        cardImage.setOnClickListener(v -> openImagePicker());

        // =========================
        // SAVE
        // =========================

        btnSave.setOnClickListener(v -> saveMenu());
    }

    private void showDeleteConfirmation() {
        StatusHelper.showConfirm(this, "Hapus Menu", "Apakah Anda yakin ingin menghapus menu ini? Data di server dan gambar juga akan dihapus.", () -> {
            if (existingMenu != null) {
                deleteMenu();
            }
        });
    }

    private void deleteMenu() {
        StatusHelper.showLoading(this, "Deleting menu and assets...");
        
        FirebaseRepository repo = new FirebaseRepository();
        
        // 1. Delete image from Storage
        repo.deleteImage(existingMenu.imagePath, new FirebaseRepository.OnCompleteListener() {
            @Override
            public void success() {
                // 2. Delete from Firestore
                repo.deleteMenu(existingMenu.firebaseId, new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void success() {
                        // 3. Delete from Local Room
                        executor.execute(() -> {
                            db.menuDao().delete(existingMenu);
                            
                            // 4. Delete local image file
                            if (existingMenu.imagePath != null && !existingMenu.imagePath.startsWith("http")) {
                                try {
                                    File file = new File(Uri.parse(existingMenu.imagePath).getPath());
                                    if (file.exists()) file.delete();
                                } catch (Exception ignored) {}
                            }
                            
                            runOnUiThread(() -> {
                                StatusHelper.hideLoading();
                                StatusHelper.showSuccess(AddMenuActivity.this, "Berhasil", "Menu telah dihapus secara permanen", AddMenuActivity.this::finish);
                            });
                        });
                    }

                    @Override
                    public void failed(String error) {
                        runOnUiThread(() -> {
                            StatusHelper.hideLoading();
                            Toast.makeText(AddMenuActivity.this, "Gagal menghapus di server: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void failed(String error) {
                runOnUiThread(() -> {
                    StatusHelper.hideLoading();
                    Toast.makeText(AddMenuActivity.this, "Gagal menghapus aset gambar: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // =========================
    // WINDOW INSETS
    // =========================

    private void setupWindowInsets() {

        View scrollView =
                findViewById(R.id.scrollView);

        if (scrollView == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                scrollView,
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.ime()
                            );

                    v.setPadding(
                            0,
                            0,
                            0,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    // =========================
    // LOAD EDIT DATA
    // =========================

    private void checkAndLoadEditData() {
        executor.execute(() -> {
            existingMenu = db.menuDao().getById(menuId);
            if (existingMenu != null) {
                runOnUiThread(() -> {
                    edtMenuName.setText(existingMenu.name);
                    edtSKU.setText(existingMenu.sku);
                    edtPrice.setText(String.valueOf((long) existingMenu.price));
                    edtDescription.setText(existingMenu.description);

                    spinnerUnit.setText(existingMenu.unit, false);
                    spinnerMenuType.setText(existingMenu.type, false);
                    spinnerCategory.setText(existingMenu.category, false);
                    spinnerStatus.setText(existingMenu.status != null ? existingMenu.status : "Aktif", false);
                    spinnerAvailability.setText(existingMenu.isAvailable ? "Tersedia" : "Habis", false);

                    if (existingMenu.imagePath != null) {
                        try {
                            imgMenu.setImageURI(Uri.parse(existingMenu.imagePath));
                            imgMenu.setAlpha(1.0f);
                            if (imgLogoOverlay != null) {
                                imgLogoOverlay.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // =========================
    // IMAGE PICKER
    // =========================

    private void openImagePicker() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        intent.setType("image/*");

        imagePickerLauncher.launch(intent);
    }

    // =========================
    // SPINNER
    // =========================

    private void setupSpinners() {

        // =========================
        // SATUAN
        // =========================

        String[] units = {
                "Porsi",
                "Gelas",
                "Pcs",
                "Botol",
                "Bungkus",
                "Mangkok",
                "Piring",
                "Cangkir",
                "Kotak",
                "Potong",
                "Tusuk",
                "Buah",
                "Pack",
                "Paket"
        };

        ArrayAdapter<String> unitAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        units
                );

        spinnerUnit.setAdapter(unitAdapter);

        spinnerUnit.setText(
                units[0],
                false
        );

        // =========================
        // JENIS MENU
        // =========================

        String[] types = {
                "Makanan",
                "Minuman"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        types
                );

        spinnerMenuType.setAdapter(typeAdapter);

        // Default: Makanan
        spinnerMenuType.setText(
                types[0],
                false
        );

        // Load kategori makanan
        updateCategorySpinner("Makanan");

        spinnerMenuType.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selectedType =
                            types[position];

                    updateCategorySpinner(
                            selectedType
                    );
                }
        );

        // =========================
        // STATUS
        // =========================

        String[] statuses = {
                "Aktif",
                "Tidak Aktif"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        statuses
                );

        spinnerStatus.setAdapter(
                statusAdapter
        );

        spinnerStatus.setText(
                statuses[0],
                false
        );

        // =========================
        // KETERSEDIAAN
        // =========================

        String[] availability = {
                "Tersedia",
                "Tidak Tersedia"
        };

        ArrayAdapter<String> availabilityAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        availability
                );

        spinnerAvailability.setAdapter(
                availabilityAdapter
        );

        spinnerAvailability.setText(
                availability[0],
                false
        );
    }

    // =========================
    // CATEGORY
    // =========================

    private void updateCategorySpinner(
            String type
    ) {

        String[] categories;

        if ("Makanan".equals(type)) {

            categories = new String[]{

                    "Makanan Utama",
                    "Ayam",
                    "Daging",
                    "Seafood",
                    "Ikan",
                    "Sate",
                    "Mie & Pasta",
                    "Nasi",
                    "Sup & Soto",
                    "Sayuran",
                    "Lauk & Tambahan",
                    "Snack",
                    "Gorengan",
                    "Roti & Sandwich",
                    "Burger",
                    "Pizza",
                    "Salad",
                    "Dessert",
                    "Kue & Pastry",
                    "Menu Anak"
            };

        } else {

            categories = new String[]{

                    "Air Mineral",
                    "Teh",
                    "Kopi",
                    "Kopi Susu",
                    "Cokelat",
                    "Susu",
                    "Jus",
                    "Smoothies",
                    "Milkshake",
                    "Soda",
                    "Mocktail",
                    "Minuman Tradisional",
                    "Minuman Buah",
                    "Minuman Segar",
                    "Minuman Dingin",
                    "Minuman Hangat"
            };
        }

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,
                        categories
                );

        spinnerCategory.setAdapter(
                categoryAdapter
        );

        // Pilih kategori pertama
        if (categories.length > 0) {

            spinnerCategory.setText(
                    categories[0],
                    false
            );
        }
    }

    // =========================
    // SAVE MENU
    // =========================

    private void saveMenu() {

        // =========================
        // GET TEXT
        // =========================

        String name =
                getText(edtMenuName);

        String sku =
                getText(edtSKU);

        String type =
                spinnerMenuType.getText()
                        .toString()
                        .trim();

        String category =
                spinnerCategory.getText()
                        .toString()
                        .trim();

        String status =
                spinnerStatus.getText()
                        .toString()
                        .trim();

        String availability =
                spinnerAvailability.getText()
                        .toString()
                        .trim();

        String unit =
                spinnerUnit.getText()
                        .toString()
                        .trim();

        String priceStr =
                getText(edtPrice);

        String desc =
                getText(edtDescription);

        // =========================
        // VALIDATION
        // =========================

        if (name.isEmpty()) {

            edtMenuName.setError(
                    "Nama menu wajib diisi"
            );

            edtMenuName.requestFocus();

            return;
        }

        if (type.isEmpty()) {

            Toast.makeText(
                    this,
                    "Pilih jenis menu",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (category.isEmpty()) {

            Toast.makeText(
                    this,
                    "Pilih kategori menu",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (priceStr.isEmpty()) {

            edtPrice.setError(
                    "Harga jual wajib diisi"
            );

            edtPrice.requestFocus();

            return;
        }

        // =========================
        // PARSE NUMBER
        // =========================

        double price;

        try {

            price =
                    Double.parseDouble(
                            priceStr
                    );

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Format harga atau stok tidak valid",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =========================
        // VALIDATE PRICE
        // =========================

        if (price < 0) {

            edtPrice.setError(
                    "Harga tidak boleh negatif"
            );

            return;
        }

        // =========================
        // LOADING
        // =========================

        StatusHelper.showLoading(
                this,
                getString(R.string.msg_loading_save)
        );

        // =========================
        // DATABASE
        // =========================

        executor.execute(() -> {

            try {

                // Pastikan session sudah terload
                if (session == null) {
                    session = db.sessionDao().getSession();
                }

                Menu menu = (menuId != -1 && existingMenu != null) ? existingMenu : new Menu();

                menu.branchId =
                        branchId;

                menu.name =
                        name;

                menu.sku =
                        sku;

                menu.unit =
                        unit;

                menu.type =
                        type;

                menu.category =
                        category;

                menu.status =
                        status;

                menu.costPrice =
                        0;

                menu.price =
                        price;

                menu.description =
                        desc;

                menu.stock =
                        0;

                if (selectedImageUri != null) {
                    menu.imagePath = saveImageToInternalStorage(selectedImageUri);
                }

                menu.isAvailable =
                        "Tersedia".equals(
                                availability
                        );

                // Belum tersinkronisasi
                menu.syncStatus = 0;

                if (menuId != -1) {
                    db.menuDao().update(menu);
                } else {
                    long id = db.menuDao().insert(menu);
                    menu.id = id;
                }

                // =========================
                // SYNC TO FIREBASE (STORAGE THEN FIRESTORE)
                // =========================

                String menuUuid = menu.firebaseId != null ? menu.firebaseId : UUID.randomUUID().toString();
                menu.firebaseId = menuUuid;

                long resId = (session != null) ? session.restaurantId : 0;
                long ownId = (session != null) ? session.ownerId : 0;

                FirebaseRepository repo = new FirebaseRepository();
                final Menu finalMenu = menu;

                repo.uploadImage("menus", menuUuid, menu.imagePath, new FirebaseRepository.OnImageUploadListener() {
                    @Override
                    public void success(String downloadUrl) {
                        if (!downloadUrl.isEmpty()) finalMenu.imagePath = downloadUrl;
                        saveMenuToFirestore(repo, finalMenu, resId, ownId);
                    }

                    @Override
                    public void failed(String error) {
                        saveMenuToFirestore(repo, finalMenu, resId, ownId);
                    }
                });

                // =========================
                // SUCCESS
                // =========================

                runOnUiThread(() -> {

                    StatusHelper.hideLoading();

                    StatusHelper.showSuccess(
                            this,
                            getString(R.string.dialog_success),
                            getString(R.string.msg_success_save),
                            this::finish
                    );
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    StatusHelper.hideLoading();
                    Toast.makeText(this, "Gagal menyimpan menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveMenuToFirestore(FirebaseRepository repo, Menu menu, long resId, long ownId) {
        repo.saveMenu(menu.firebaseId, menu.branchId, menu.name, menu.type, menu.category, menu.unit, menu.sku, menu.costPrice, menu.price, menu.description, menu.imagePath, menu.stock, menu.status, menu.isAvailable, menu.syncStatus, resId, ownId, new FirebaseRepository.OnCompleteListener() {
            @Override
            public void success() {
                executor.execute(() -> {
                    menu.syncStatus = 1;
                    db.menuDao().update(menu);
                });
            }

            @Override
            public void failed(String error) {
                executor.execute(() -> {
                    menu.syncStatus = 2;
                    db.menuDao().update(menu);
                });
            }
        });
    }

    // =========================
    // SKU GENERATOR
    // =========================

    private void generateAutoSKU() {
        if (edtSKU != null && (edtSKU.getText() == null || edtSKU.getText().toString().isEmpty())) {
            String prefix = "PRD";
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
            String random = String.valueOf((int) (Math.random() * 900) + 100);
            String autoSKU = prefix + timestamp + random;
            edtSKU.setText(autoSKU);
        }
    }

    // =========================
    // SAVE IMAGE HELPER
    // =========================

    private String saveImageToInternalStorage(Uri uri) {
        if (uri == null) return null;
        if (!"content".equals(uri.getScheme())) {
            return uri.toString();
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(getFilesDir(), "menu_image_" + System.currentTimeMillis() + ".jpg");
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

    // =========================
    // GET TEXT HELPER
    // =========================

    private String getText(
            TextInputEditText editText
    ) {

        if (editText == null
                || editText.getText() == null) {

            return "";
        }

        return editText.getText()
                .toString()
                .trim();
    }

    // =========================
    // DESTROY
    // =========================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}