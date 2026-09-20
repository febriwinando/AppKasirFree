package tech.id.kasirapp.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Combo;
import tech.id.kasirapp.util.StatusHelper;

public class ComboListActivity extends AppCompatActivity {

    private RecyclerView rvCombo;
    private TextView tvEmpty;
    private ExtendedFloatingActionButton btnAddCombo;
    private ComboAdapter adapter;
    private List<Combo> comboList = new ArrayList<>();

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_list);

        db = DatabaseClient.getDatabase(this);
        initView();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessionAndData();
    }

    private void initView() {
        rvCombo = findViewById(R.id.rvCombo);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddCombo = findViewById(R.id.btnAddCombo);

        btnAddCombo.setOnClickListener(v -> {
            startActivity(new Intent(this, AddComboActivity.class));
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvCombo.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComboAdapter();
        rvCombo.setAdapter(adapter);
    }

    private void loadSessionAndData() {
        executor.execute(() -> {
            AppSession session = db.sessionDao().getSession();
            if (session != null) {
                branchId = session.branchId;
                refreshData();
            }
        });
    }

    private void refreshData() {
        executor.execute(() -> {
            comboList = db.comboDao().getByBranch(branchId);
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(comboList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void deleteCombo(Combo combo) {
        StatusHelper.showConfirm(this, "Hapus Paket", "Hapus paket " + combo.name + "?", () -> {
            StatusHelper.showLoading(this, "Menghapus paket...");
            executor.execute(() -> {
                db.comboItemDao().deleteByCombo(combo.id);
                db.comboDao().delete(combo);
                new FirebaseRepository().deleteCombo(combo.firebaseId, null);
                refreshData();
                runOnUiThread(StatusHelper::hideLoading);
            });
        });
    }

    private class ComboAdapter extends RecyclerView.Adapter<ComboAdapter.ComboVH> {
        @NonNull
        @Override
        public ComboVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ComboVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_combo, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ComboVH holder, int position) {
            Combo combo = comboList.get(position);
            holder.tvName.setText(combo.name);
            holder.tvPrice.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", combo.price));

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ComboListActivity.this, AddComboActivity.class);
                intent.putExtra("combo_id", combo.id);
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> deleteCombo(combo));
        }

        @Override
        public int getItemCount() { return comboList.size(); }

        class ComboVH extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            ImageButton btnEdit, btnDelete;
            public ComboVH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvComboName);
                tvPrice = itemView.findViewById(R.id.tvComboPrice);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}