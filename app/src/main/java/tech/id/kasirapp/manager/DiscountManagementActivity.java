package tech.id.kasirapp.manager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
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
import tech.id.kasirapp.data.local.entity.Diskon;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.util.StatusHelper;

public class DiscountManagementActivity extends AppCompatActivity {

    private MaterialSwitch switchDiscountMode;
    private MaterialCardView cardGlobalForm;
    private LinearLayout layoutItemWise;
    private RecyclerView rvMenu;
    
    private TextInputEditText edtGlobalName, edtGlobalValue, edtSearchMenu;
    private AutoCompleteTextView spinnerGlobalUnit;
    private MaterialButton btnSaveGlobal;

    private MenuDiscountAdapter adapter;
    private List<Menu> menuList = new ArrayList<>();
    private List<Menu> filteredMenuList = new ArrayList<>();
    private Map<Long, Diskon> itemDiscounts = new HashMap<>();
    private Diskon activeGlobalDiscount = null;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId, restaurantId, ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_management);

        db = DatabaseClient.getDatabase(this);
        
        initView();
        setupToolbar();
        setupRecyclerView();
        loadSessionAndData();
    }

    private void initView() {
        switchDiscountMode = findViewById(R.id.switchDiscountMode);
        cardGlobalForm = findViewById(R.id.cardGlobalForm);
        layoutItemWise = findViewById(R.id.layoutItemWise);
        rvMenu = findViewById(R.id.rvMenuForDiscount);
        
        edtGlobalName = findViewById(R.id.edtGlobalName);
        edtGlobalValue = findViewById(R.id.edtGlobalValue);
        edtSearchMenu = findViewById(R.id.edtSearchMenu);
        spinnerGlobalUnit = findViewById(R.id.spinnerGlobalUnit);
        btnSaveGlobal = findViewById(R.id.btnSaveGlobal);

        String[] units = {"%", "Rp"};
        spinnerGlobalUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units));
        spinnerGlobalUnit.setText(units[0], false);

        switchDiscountMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardGlobalForm.setVisibility(View.VISIBLE);
                layoutItemWise.setVisibility(View.GONE);
            } else {
                cardGlobalForm.setVisibility(View.GONE);
                layoutItemWise.setVisibility(View.VISIBLE);
            }
        });

        edtSearchMenu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterMenu(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveGlobal.setOnClickListener(v -> saveGlobalDiscount());
    }

    private void filterMenu(String query) {
        filteredMenuList.clear();
        if (query.isEmpty()) {
            filteredMenuList.addAll(menuList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Menu m : menuList) {
                if (m.name.toLowerCase().contains(lowerQuery)) {
                    filteredMenuList.add(m);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuDiscountAdapter();
        rvMenu.setAdapter(adapter);
    }

    private void loadSessionAndData() {
        executor.execute(() -> {
            AppSession session = db.sessionDao().getSession();
            if (session != null) {
                branchId = session.branchId;
                restaurantId = session.restaurantId;
                ownerId = session.ownerId;
                refreshData();
            }
        });
    }

    private void refreshData() {
        executor.execute(() -> {
            menuList = db.menuDao().getByBranch(branchId);
            List<Diskon> allDiscounts = db.diskonDao().getByBranch(branchId);
            
            itemDiscounts.clear();
            activeGlobalDiscount = null;

            for (Diskon d : allDiscounts) {
                if ("GLOBAL".equals(d.type) && d.isActive) {
                    activeGlobalDiscount = d;
                } else if ("ITEM".equals(d.type)) {
                    itemDiscounts.put(d.menuId, d);
                }
            }

            runOnUiThread(() -> {
                if (activeGlobalDiscount != null) {
                    switchDiscountMode.setChecked(true);
                    edtGlobalName.setText(activeGlobalDiscount.name);
                    edtGlobalValue.setText(String.valueOf(activeGlobalDiscount.value));
                    spinnerGlobalUnit.setText(activeGlobalDiscount.isPercentage ? "%" : "Rp", false);
                    btnSaveGlobal.setText("Nonaktifkan Diskon Global");
                    btnSaveGlobal.setBackgroundTintList(getColorStateList(R.color.error));
                    
                    // Lock mode to Global if active
                    switchDiscountMode.setEnabled(false);
                    cardGlobalForm.setVisibility(View.VISIBLE);
                    layoutItemWise.setVisibility(View.GONE);
                } else {
                    switchDiscountMode.setEnabled(true);
                    // Reset fields if nothing active
                    if (!switchDiscountMode.isChecked()) {
                        edtGlobalName.setText("");
                        edtGlobalValue.setText("");
                    }
                    
                    btnSaveGlobal.setText("Aktifkan Diskon Global");
                    btnSaveGlobal.setBackgroundTintList(getColorStateList(R.color.primary));
                    
                    if (switchDiscountMode.isChecked()) {
                        cardGlobalForm.setVisibility(View.VISIBLE);
                        layoutItemWise.setVisibility(View.GONE);
                    } else {
                        cardGlobalForm.setVisibility(View.GONE);
                        layoutItemWise.setVisibility(View.VISIBLE);
                    }
                }
                
                // Update filtered list and adapter
                filterMenu(edtSearchMenu.getText().toString());
            });
        });
    }

    private void saveGlobalDiscount() {
        if (activeGlobalDiscount != null) {
            // Deactivate
            StatusHelper.showConfirm(this, "Nonaktifkan Global", "Matikan diskon menyeluruh?", () -> {
                activeGlobalDiscount.isActive = false;
                activeGlobalDiscount.syncStatus = 0;
                executor.execute(() -> {
                    db.diskonDao().update(activeGlobalDiscount);
                    new FirebaseRepository().saveDiskon(activeGlobalDiscount.firebaseId, branchId, activeGlobalDiscount.name, 
                            activeGlobalDiscount.type, activeGlobalDiscount.value, activeGlobalDiscount.isPercentage, 
                            false, 0, "", activeGlobalDiscount.syncStatus, restaurantId, ownerId, new FirebaseRepository.OnCompleteListener() {
                                @Override
                                public void success() {
                                    executor.execute(() -> {
                                        activeGlobalDiscount.syncStatus = 1;
                                        db.diskonDao().update(activeGlobalDiscount);
                                        refreshData();
                                    });
                                }
                                @Override
                                public void failed(String error) {
                                    executor.execute(() -> {
                                        activeGlobalDiscount.syncStatus = 2;
                                        db.diskonDao().update(activeGlobalDiscount);
                                        refreshData();
                                    });
                                }
                            });
                    refreshData();
                });
            });
            return;
        }

        String name = edtGlobalName.getText().toString().trim();
        String valStr = edtGlobalValue.getText().toString().trim();
        if (name.isEmpty() || valStr.isEmpty()) {
            Toast.makeText(this, "Lengkapi data global", Toast.LENGTH_SHORT).show();
            return;
        }

        Diskon diskon = new Diskon();
        diskon.branchId = branchId;
        diskon.restaurantId = restaurantId;
        diskon.ownerId = ownerId;
        diskon.firebaseId = UUID.randomUUID().toString();
        diskon.name = name;
        diskon.value = Double.parseDouble(valStr);
        diskon.isPercentage = "%".equals(spinnerGlobalUnit.getText().toString());
        
        // Validation
        if (diskon.isPercentage && diskon.value > 100) {
            edtGlobalValue.setError("Diskon % maksimal 100%");
            edtGlobalValue.requestFocus();
            return;
        }

        diskon.type = "GLOBAL";
        diskon.isActive = true;
        diskon.syncStatus = 0;

        StatusHelper.showLoading(this, "Mengaktifkan diskon global...");
        executor.execute(() -> {
            db.diskonDao().insert(diskon);
            new FirebaseRepository().saveDiskon(diskon.firebaseId, branchId, diskon.name, diskon.type, diskon.value, 
                    diskon.isPercentage, true, 0, "", diskon.syncStatus, restaurantId, ownerId, new FirebaseRepository.OnCompleteListener() {
                        @Override
                        public void success() {
                            executor.execute(() -> {
                                diskon.syncStatus = 1;
                                db.diskonDao().update(diskon);
                                refreshData();
                            });
                        }
                        @Override
                        public void failed(String error) {
                            executor.execute(() -> {
                                diskon.syncStatus = 2;
                                db.diskonDao().update(diskon);
                                refreshData();
                            });
                        }
                    });
            refreshData();
            runOnUiThread(StatusHelper::hideLoading);
        });
    }

    private void showItemDiscountDialog(Menu menu) {
        Diskon existing = itemDiscounts.get(menu.id);
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_discount, null);
        TextInputEditText edtName = dialogView.findViewById(R.id.edtDiscountName);
        TextInputEditText edtValue = dialogView.findViewById(R.id.edtDiscountValue);
        AutoCompleteTextView spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        
        String[] units = {"%", "Rp"};
        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units));
        
        if (existing != null) {
            edtName.setText(existing.name);
            edtValue.setText(String.valueOf(existing.value));
            spinnerUnit.setText(existing.isPercentage ? "%" : "Rp", false);
        } else {
            edtName.setText("Promo " + menu.name);
            spinnerUnit.setText("%", false);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Atur Diskon: " + menu.name)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String valStr = edtValue.getText().toString().trim();
                    if (name.isEmpty() || valStr.isEmpty()) return;

                    double val = Double.parseDouble(valStr);
                    boolean isPct = "%".equals(spinnerUnit.getText().toString());

                    // Validation
                    if (isPct && val > 100) {
                        Toast.makeText(this, "Diskon % maksimal 100%", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isPct && val > menu.price) {
                        Toast.makeText(this, "Diskon Rp tidak boleh melebihi harga menu (Rp " + menu.price + ")", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Diskon d = (existing != null) ? existing : new Diskon();
                    d.branchId = branchId;
                    d.restaurantId = restaurantId;
                    d.ownerId = ownerId;
                    if (d.firebaseId == null) d.firebaseId = UUID.randomUUID().toString();
                    d.name = name;
                    d.value = val;
                    d.isPercentage = isPct;
                    d.type = "ITEM";
                    d.menuId = menu.id;
                    d.menuName = menu.name;
                    d.isActive = true;
                    d.syncStatus = 0;

                    executor.execute(() -> {
                        if (existing == null) db.diskonDao().insert(d);
                        else db.diskonDao().update(d);
                        
                        new FirebaseRepository().saveDiskon(d.firebaseId, branchId, d.name, d.type, d.value, 
                                d.isPercentage, true, d.menuId, d.menuName, d.syncStatus, restaurantId, ownerId, new FirebaseRepository.OnCompleteListener() {
                                    @Override
                                    public void success() {
                                        executor.execute(() -> {
                                            d.syncStatus = 1;
                                            db.diskonDao().update(d);
                                            refreshData();
                                        });
                                    }
                                    @Override
                                    public void failed(String error) {
                                        executor.execute(() -> {
                                            d.syncStatus = 2;
                                            db.diskonDao().update(d);
                                            refreshData();
                                        });
                                    }
                                });
                        refreshData();
                    });
                })
                .setNeutralButton(existing != null ? "Hapus" : null, (dialog, which) -> {
                    if (existing != null) {
                        executor.execute(() -> {
                            db.diskonDao().delete(existing);
                            new FirebaseRepository().deleteDiskon(existing.firebaseId, null);
                            refreshData();
                        });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // --- Adapter ---
    private class MenuDiscountAdapter extends RecyclerView.Adapter<MenuDiscountAdapter.MenuVH> {
        @NonNull
        @Override
        public MenuVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MenuVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MenuVH holder, int position) {
            Menu menu = filteredMenuList.get(position);
            holder.tvName.setText(menu.name);
            holder.tvPrice.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", menu.price));
            holder.tvCategory.setText(menu.category);
            
            Diskon d = itemDiscounts.get(menu.id);
            if (d != null && d.isActive) {
                String val = d.isPercentage ? ((int)d.value + "%") : ("Rp " + String.format(Locale.getDefault(), "%,.0f", d.value));
                holder.tvStatus.setText("Diskon: " + val);
                holder.tvStatus.setBackgroundTintList(getColorStateList(R.color.primary_container));
                holder.tvStatus.setTextColor(getColor(R.color.primary));
                
                // Highlight item with discount
                ((MaterialCardView) holder.itemView).setStrokeColor(getColor(R.color.primary));
                ((MaterialCardView) holder.itemView).setStrokeWidth(4);
                holder.itemView.setBackgroundTintList(getColorStateList(R.color.primary_light));
            } else {
                holder.tvStatus.setText("Tanpa Diskon");
                holder.tvStatus.setBackgroundTintList(null);
                holder.tvStatus.setTextColor(getColor(R.color.secondary));
                holder.tvStatus.setAlpha(0.5f);
                
                // Reset highlight
                ((MaterialCardView) holder.itemView).setStrokeWidth(0);
                holder.itemView.setBackgroundTintList(null);
            }

            holder.itemView.setOnClickListener(v -> showItemDiscountDialog(menu));
            holder.btnEdit.setVisibility(View.GONE); // Use row click instead
        }

        @Override
        public int getItemCount() { return filteredMenuList.size(); }

        class MenuVH extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvCategory, tvStatus;
            ImageView btnEdit;
            public MenuVH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMenuName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnEdit = itemView.findViewById(R.id.btnEditMenu);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}