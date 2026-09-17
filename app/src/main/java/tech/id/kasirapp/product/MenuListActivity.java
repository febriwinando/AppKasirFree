package tech.id.kasirapp.product;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Menu;

public class MenuListActivity extends AppCompatActivity {

    private RecyclerView rvMenu;
    private MenuAdapter adapter;
    private List<Menu> allMenuList = new ArrayList<>();
    private List<Menu> filteredList = new ArrayList<>();

    private TextInputEditText edtSearch;
    private ChipGroup chipGroupFilters;
    private TextView tvEmptyState;
    private ExtendedFloatingActionButton btnAddMenu;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        db = DatabaseClient.getDatabase(this);
        branchId = getIntent().getLongExtra("branch_id", 0);

        initView();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initView() {
        rvMenu = findViewById(R.id.rvMenu);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnAddMenu = findViewById(R.id.btnAddMenu);

        btnAddMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMenuActivity.class);
            intent.putExtra("branch_id", branchId);
            startActivity(intent);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuAdapter(filteredList);
        rvMenu.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void loadData() {
        executor.execute(() -> {
            allMenuList = db.menuDao().getByBranch(branchId);
            runOnUiThread(() -> applyFilters());
        });
    }

    private void applyFilters() {
        String query = edtSearch.getText().toString().toLowerCase().trim();
        int checkedChipId = chipGroupFilters.getCheckedChipId();

        filteredList.clear();
        for (Menu menu : allMenuList) {
            boolean matchesName = menu.name.toLowerCase().contains(query);
            boolean matchesType = true;

            if (checkedChipId == R.id.chipFood) {
                matchesType = "Makanan".equalsIgnoreCase(menu.type);
            } else if (checkedChipId == R.id.chipDrink) {
                matchesType = "Minuman".equalsIgnoreCase(menu.type);
            }

            if (matchesName && matchesType) {
                filteredList.add(menu);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // --- Adapter Class ---
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        private List<Menu> list;

        public MenuAdapter(List<Menu> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            Menu menu = list.get(position);
            holder.tvName.setText(menu.name);
            holder.tvCategory.setText(menu.category);
            holder.tvType.setText(menu.type);
            holder.tvPrice.setText("Rp " + String.format("%,.0f", menu.price));
            
            // Status & Ketersediaan
            String statusText = menu.status != null ? menu.status : "Aktif";
            if (!menu.isAvailable) {
                statusText += " (Habis)";
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1AE74C3C")));
                holder.tvStatus.setTextColor(Color.parseColor("#E74C3C"));
            } else {
                holder.tvStatus.setBackgroundTintList(null); // Reset to default
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success));
            }
            holder.tvStatus.setText(statusText);

            if (menu.imagePath != null) {
                holder.imgMenu.setImageURI(Uri.parse(menu.imagePath));
                holder.imgMenu.setAlpha(1.0f);
            } else {
                holder.imgMenu.setImageResource(R.drawable.ic_product);
                holder.imgMenu.setAlpha(0.5f);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvType, tvPrice, tvStatus;
            ImageView imgMenu;

            public MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMenuName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvType = itemView.findViewById(R.id.tvType);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                imgMenu = itemView.findViewById(R.id.imgMenu);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}