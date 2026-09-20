package tech.id.kasirapp.manager;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.AppSession;
import tech.id.kasirapp.data.local.entity.Ruangan;
import tech.id.kasirapp.util.StatusHelper;

public class RoomManagementActivity extends AppCompatActivity {

    private RecyclerView rvRuangan;
    private ExtendedFloatingActionButton btnAddRoom;
    private TextView tvEmpty;
    
    private RoomAdapter adapter;
    private List<Ruangan> roomList = new ArrayList<>();
    
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long branchId;
    private long restaurantId;
    private long ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        db = DatabaseClient.getDatabase(this);
        
        initView();
        setupToolbar();
        setupRecyclerView();
        loadSessionAndData();
    }

    private void initView() {
        rvRuangan = findViewById(R.id.rvRuangan);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnAddRoom.setOnClickListener(v -> showRoomDialog(null));
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvRuangan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(roomList);
        rvRuangan.setAdapter(adapter);
    }

    private void loadSessionAndData() {
        executor.execute(() -> {
            AppSession session = db.sessionDao().getSession();
            if (session != null) {
                branchId = session.branchId;
                restaurantId = session.restaurantId;
                ownerId = session.ownerId;
                loadRooms();
            }
        });
    }

    private void loadRooms() {
        executor.execute(() -> {
            roomList = db.ruanganDao().getByBranch(branchId);
            runOnUiThread(() -> {
                adapter.updateData(roomList);
                tvEmpty.setVisibility(roomList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showRoomDialog(Ruangan existingRoom) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        TextInputEditText edtName = dialogView.findViewById(R.id.edtRoomName);
        TextInputEditText edtCount = dialogView.findViewById(R.id.edtTableCount);

        if (existingRoom != null) {
            edtName.setText(existingRoom.name);
            edtCount.setText(String.valueOf(existingRoom.tableCount));
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String countStr = edtCount.getText().toString().trim();
                    
                    if (name.isEmpty() || countStr.isEmpty()) {
                        Toast.makeText(this, "Mohon lengkapi data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int count = Integer.parseInt(countStr);
                    saveRoom(existingRoom, name, count);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveRoom(Ruangan existing, String name, int count) {
        StatusHelper.showLoading(this, "Menyimpan data ruangan...");
        executor.execute(() -> {
            Ruangan room = (existing != null) ? existing : new Ruangan();
            room.branchId = branchId;
            room.name = name;
            room.tableCount = count;
            room.syncStatus = 0;
            
            if (room.firebaseId == null) {
                room.firebaseId = UUID.randomUUID().toString();
            }

            if (existing == null) {
                room.id = db.ruanganDao().insert(room);
            } else {
                db.ruanganDao().update(room);
            }

            // Sync to Firestore
            FirebaseRepository repo = new FirebaseRepository();
            repo.saveRuangan(room.firebaseId, branchId, room.name, room.tableCount, room.syncStatus, restaurantId, ownerId, new FirebaseRepository.OnCompleteListener() {
                @Override
                public void success() {
                    executor.execute(() -> {
                        room.syncStatus = 1;
                        db.ruanganDao().update(room);
                        loadRooms();
                    });
                }

                @Override
                public void failed(String error) {
                    executor.execute(() -> {
                        room.syncStatus = 2;
                        db.ruanganDao().update(room);
                        loadRooms();
                    });
                }
            });

            runOnUiThread(() -> {
                StatusHelper.hideLoading();
                loadRooms();
            });
        });
    }

    private void deleteRoom(Ruangan room) {
        StatusHelper.showConfirm(this, "Hapus Ruangan", "Apakah Anda yakin ingin menghapus ruangan ini?", () -> {
            StatusHelper.showLoading(this, "Menghapus data...");
            executor.execute(() -> {
                FirebaseRepository repo = new FirebaseRepository();
                repo.deleteRuangan(room.firebaseId, new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void success() {
                        executor.execute(() -> {
                            db.ruanganDao().delete(room);
                            loadRooms();
                        });
                    }

                    @Override
                    public void failed(String error) {
                        executor.execute(() -> {
                            db.ruanganDao().delete(room); // Tetap hapus local jika gagal cloud? Atau tandai?
                            loadRooms();
                        });
                    }
                });
                
                runOnUiThread(() -> {
                    StatusHelper.hideLoading();
                });
            });
        });
    }

    // --- Adapter ---
    private class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
        private List<Ruangan> list;

        public RoomAdapter(List<Ruangan> list) {
            this.list = list;
        }

        public void updateData(List<Ruangan> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruangan, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
            Ruangan room = list.get(position);
            holder.tvName.setText(room.name);
            holder.tvCount.setText(room.tableCount + " Meja Terdaftar");
            
            holder.btnEdit.setOnClickListener(v -> showRoomDialog(room));
            holder.btnDelete.setOnClickListener(v -> deleteRoom(room));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class RoomViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCount;
            ImageButton btnEdit, btnDelete;
            public RoomViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRoomName);
                tvCount = itemView.findViewById(R.id.tvTableCount);
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