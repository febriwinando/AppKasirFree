package tech.id.kasirapp.manager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Combo;
import tech.id.kasirapp.data.local.entity.ComboItem;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.util.StatusHelper;

public class AddComboActivity extends AppCompatActivity {

    private TextInputEditText edtComboName, edtComboPrice;
    private AutoCompleteTextView spinnerStatus;
    private MaterialButton btnAddMenuItem, btnSave;
    private RecyclerView rvItems;
    private ComboItemsAdapter adapter;

    private List<ComboItem> selectedItems = new ArrayList<>();
    private List<Menu> availableMenus = new ArrayList<>();
    
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId, restaurantId, ownerId;
    private long comboId = -1;
    private Combo existingCombo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_combo);

        db = DatabaseClient.getDatabase(this);
        comboId = getIntent().getLongExtra("combo_id", -1);

        initView();
        setupToolbar();
        setupRecyclerView();
        loadSessionAndData();
        setupAutoScroll(findViewById(R.id.scrollView));
    }

    private void initView() {
        edtComboName = findViewById(R.id.edtComboName);
        edtComboPrice = findViewById(R.id.edtComboPrice);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnAddMenuItem = findViewById(R.id.btnAddMenuItem);
        btnSave = findViewById(R.id.btnSave);
        rvItems = findViewById(R.id.rvComboItems);

        String[] statuses = {"Aktif", "Nonaktif"};
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
        spinnerStatus.setText(statuses[0], false);
        spinnerStatus.setOnTouchListener((v, event) -> { hideKeyboard(); v.performClick(); return true; });

        btnAddMenuItem.setOnClickListener(v -> showMenuSelectionDialog());
        btnSave.setOnClickListener(v -> saveCombo());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (comboId != -1) toolbar.setTitle("Edit Paket Combo");
    }

    private void setupRecyclerView() {
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComboItemsAdapter();
        rvItems.setAdapter(adapter);
    }

    private void loadSessionAndData() {
        executor.execute(() -> {
            AppSession session = db.sessionDao().getSession();
            if (session != null) {
                branchId = session.branchId;
                restaurantId = session.restaurantId;
                ownerId = session.ownerId;
                availableMenus = db.menuDao().getByBranch(branchId);
                
                if (comboId != -1) {
                    existingCombo = db.comboDao().getById(comboId);
                    selectedItems = db.comboItemDao().getByCombo(comboId);
                    runOnUiThread(() -> {
                        edtComboName.setText(existingCombo.name);
                        edtComboPrice.setText(String.valueOf((int)existingCombo.price));
                        spinnerStatus.setText(existingCombo.status, false);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    private void showMenuSelectionDialog() {
        if (availableMenus.isEmpty()) {
            Toast.makeText(this, "Tidak ada menu tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] menuNames = new String[availableMenus.size()];
        for (int i = 0; i < availableMenus.size(); i++) {
            menuNames[i] = availableMenus.get(i).name;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Menu untuk Paket")
                .setItems(menuNames, (dialog, which) -> {
                    Menu selected = availableMenus.get(which);
                    addMenuToCombo(selected);
                })
                .show();
    }

    private void addMenuToCombo(Menu menu) {
        for (ComboItem item : selectedItems) {
            if (item.menuId == menu.id) {
                item.quantity++;
                adapter.notifyDataSetChanged();
                return;
            }
        }

        ComboItem newItem = new ComboItem();
        newItem.menuId = menu.id;
        newItem.menuName = menu.name;
        newItem.quantity = 1;
        selectedItems.add(newItem);
        adapter.notifyDataSetChanged();
    }

    private void saveCombo() {
        String name = edtComboName.getText().toString().trim();
        String priceStr = edtComboPrice.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Lengkapi data paket", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Tambahkan minimal 1 menu ke paket", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        String status = spinnerStatus.getText().toString();

        StatusHelper.showLoading(this, "Menyimpan paket combo...");
        executor.execute(() -> {
            Combo combo = (existingCombo != null) ? existingCombo : new Combo();
            combo.branchId = branchId;
            if (combo.firebaseId == null) combo.firebaseId = UUID.randomUUID().toString();
            combo.name = name;
            combo.price = price;
            combo.status = status;
            combo.syncStatus = 0;

            long id;
            if (existingCombo == null) {
                id = db.comboDao().insert(combo);
                combo.id = id;
            } else {
                db.comboDao().update(combo);
                id = combo.id;
                db.comboItemDao().deleteByCombo(id);
            }

            for (ComboItem item : selectedItems) item.comboId = id;
            db.comboItemDao().insertAll(selectedItems);

            // Sync to Firestore
            List<Map<String, Object>> firestoreItems = new ArrayList<>();
            for (ComboItem item : selectedItems) {
                Map<String, Object> m = new HashMap<>();
                m.put("menuId", item.menuId);
                m.put("menuName", item.menuName);
                m.put("quantity", item.quantity);
                firestoreItems.add(m);
            }

            new FirebaseRepository().saveCombo(combo.firebaseId, branchId, combo.name, combo.price, 
                    combo.description, combo.status, firestoreItems, 1, restaurantId, ownerId, new FirebaseRepository.OnCompleteListener() {
                @Override
                public void success() {
                    executor.execute(() -> {
                        combo.syncStatus = 1;
                        db.comboDao().update(combo);
                        finish();
                    });
                }
                @Override
                public void failed(String error) {
                    runOnUiThread(() -> {
                        StatusHelper.hideLoading();
                        Toast.makeText(AddComboActivity.this, "Gagal sinkron: " + error, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });

            runOnUiThread(() -> {
                StatusHelper.hideLoading();
                Toast.makeText(this, "Paket disimpan", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private class ComboItemsAdapter extends RecyclerView.Adapter<ComboItemsAdapter.ItemVH> {
        @NonNull
        @Override
        public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_combo_menu_selection, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemVH holder, int position) {
            ComboItem item = selectedItems.get(position);
            holder.tvName.setText(item.menuName);
            holder.tvQty.setText(String.valueOf(item.quantity));

            holder.btnPlus.setOnClickListener(v -> {
                item.quantity++;
                notifyItemChanged(position);
            });
            holder.btnMinus.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    item.quantity--;
                    notifyItemChanged(position);
                }
            });
            holder.btnDelete.setOnClickListener(v -> {
                selectedItems.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() { return selectedItems.size(); }

        class ItemVH extends RecyclerView.ViewHolder {
            TextView tvName, tvQty;
            ImageButton btnPlus, btnMinus, btnDelete;
            public ItemVH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMenuName);
                tvQty = itemView.findViewById(R.id.tvQuantity);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        edtComboName.setOnFocusChangeListener(focusListener);
        edtComboPrice.setOnFocusChangeListener(focusListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}