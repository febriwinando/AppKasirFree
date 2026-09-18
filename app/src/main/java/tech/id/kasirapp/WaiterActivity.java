package tech.id.kasirapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Waiter;
import tech.id.kasirapp.util.StatusHelper;
import tech.id.kasirapp.waiter.AddWaiterActivity;

public class WaiterActivity extends AppCompatActivity {

    private RecyclerView rvWaiter;
    private WaiterAdapter adapter;
    private List<Waiter> allWaiterList = new ArrayList<>();
    private List<Waiter> filteredList = new ArrayList<>();

    private TextInputEditText edtSearch;
    private TextView tvEmptyState;
    private ExtendedFloatingActionButton btnAddWaiter;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiter);

        db = DatabaseClient.getDatabase(this);
        branchId = getIntent().getLongExtra("branch_id", 0);

        initView();
        setupToolbar();
        setupRecyclerView();

        View appBarLayout = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        View scrollView = findViewById(R.id.scrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
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
        rvWaiter = findViewById(R.id.rvWaiter);
        edtSearch = findViewById(R.id.edtSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnAddWaiter = findViewById(R.id.btnAddWaiter);

        btnAddWaiter.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWaiterActivity.class);
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
        rvWaiter.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaiterAdapter(filteredList);
        rvWaiter.setAdapter(adapter);
    }

    private void loadData() {
        executor.execute(() -> {
            allWaiterList = db.waiterDao().getByBranchId(branchId);
            runOnUiThread(() -> applyFilters());
        });
    }

    private void applyFilters() {
        if (edtSearch == null || allWaiterList == null) return;
        
        String query = edtSearch.getText() != null ? edtSearch.getText().toString().toLowerCase().trim() : "";

        filteredList.clear();
        for (Waiter waiter : allWaiterList) {
            if (waiter == null) continue;
            
            String name = waiter.name != null ? waiter.name.toLowerCase() : "";
            String username = waiter.username != null ? waiter.username.toLowerCase() : "";
            
            if (name.contains(query) || username.contains(query)) {
                filteredList.add(waiter);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // --- Adapter Class ---
    private class WaiterAdapter extends RecyclerView.Adapter<WaiterAdapter.WaiterViewHolder> {
        private List<Waiter> list;

        public WaiterAdapter(List<Waiter> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public WaiterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waiter, parent, false);
            return new WaiterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WaiterViewHolder holder, int position) {
            Waiter waiter = list.get(position);
            holder.tvName.setText(waiter.name != null ? capitalizeWords(waiter.name) : "-");
            holder.tvUsername.setText(waiter.username != null ? "@" + waiter.username : "@-");
            
            if (waiter.photoPath != null && !waiter.photoPath.isEmpty()) {
                try {
                    holder.imgWaiter.setImageURI(Uri.parse(waiter.photoPath));
                    holder.imgWaiter.setVisibility(View.VISIBLE);
                    holder.tvInitial.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.imgWaiter.setVisibility(View.GONE);
                    holder.tvInitial.setVisibility(View.VISIBLE);
                }
            } else {
                holder.imgWaiter.setVisibility(View.GONE);
                holder.tvInitial.setVisibility(View.VISIBLE);
            }

            String initial = "W";
            if (waiter.name != null && !waiter.name.isEmpty()) {
                initial = waiter.name.substring(0, 1).toUpperCase();
            }
            holder.tvInitial.setText(initial);

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(WaiterActivity.this, AddWaiterActivity.class);
                intent.putExtra("branch_id", branchId);
                intent.putExtra("waiter_id", waiter.id);
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> {
                deleteWaiter(waiter);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class WaiterViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUsername, tvInitial;
            ImageView imgWaiter;
            View btnEdit, btnDelete;

            public WaiterViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvWaiterName);
                tvUsername = itemView.findViewById(R.id.tvWaiterUsername);
                tvInitial = itemView.findViewById(R.id.tvInitial);
                imgWaiter = itemView.findViewById(R.id.imgWaiter);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void deleteWaiter(Waiter waiter) {
        if (waiter == null) return;
        String waiterName = waiter.name != null ? waiter.name : "staf";
        StatusHelper.showConfirm(this, "Hapus Waiter", "Apakah Anda yakin ingin menghapus waiter " + waiterName + "?", () -> {
            executor.execute(() -> {
                db.waiterDao().deleteById(waiter.id);
                runOnUiThread(() -> {
                    StatusHelper.showSuccess(this, "Berhasil", "Data waiter berhasil dihapus", this::loadData);
                });
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
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
}
