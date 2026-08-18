package tech.id.kasirapp.owner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.BranchActivity;
import tech.id.kasirapp.R;
import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Manager;
import tech.id.kasirapp.data.local.entity.Restaurant;
import tech.id.kasirapp.register.RegisterBranchActivity;
import tech.id.kasirapp.register.RegisterManagerActivity;
import tech.id.kasirapp.register.RegisterRestaurantActivity;

public class RestaurantActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private MaterialButton btnAddRestaurant;

    private LinearLayout restaurantContainer;

    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_restaurant
        );


        db = DatabaseClient
                .getDatabase(this);


        initView();

        setupToolbar();

        setupClick();

    }


    @Override
    protected void onStart() {

        super.onStart();

        loadRestaurants();

    }


    private void initView() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        btnAddRestaurant =
                findViewById(
                        R.id.btnAddRestaurant
                );


        restaurantContainer =
                findViewById(
                        R.id.restaurantContainer
                );

    }


    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );

    }


    private void setupClick() {

        btnAddRestaurant.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    RestaurantActivity.this,
                                    RegisterRestaurantActivity.class
                            );

                    startActivity(intent);

                }
        );

    }


    // =========================================================
    // LOAD RESTORAN
    // =========================================================

    private void loadRestaurants() {

        executor.execute(() -> {

            List<Restaurant> restaurants =
                    db.restaurantDao()
                            .getAll();


            runOnUiThread(() -> {

                displayRestaurants(
                        restaurants
                );

            });

        });

    }


    // =========================================================
    // DISPLAY RESTORAN
    // =========================================================

    private void displayRestaurants(
            List<Restaurant> restaurants
    ) {

        restaurantContainer.removeAllViews();


        if (restaurants == null ||
                restaurants.isEmpty()) {

            TextView empty =
                    new TextView(this);


            empty.setText(
                    "Belum ada restoran.\n" +
                            "Silakan tambahkan restoran."
            );


            empty.setTextSize(14);

            empty.setTextColor(
                    Color.rgb(
                            117,
                            117,
                            117
                    )
            );


            empty.setGravity(
                    Gravity.CENTER
            );


            empty.setPadding(
                    16,
                    40,
                    16,
                    40
            );


            restaurantContainer.addView(
                    empty
            );


            return;
        }


        for (Restaurant restaurant :
                restaurants) {

            createRestaurantCard(
                    restaurant
            );

        }

    }


    // =========================================================
    // CARD RESTORAN
    // =========================================================

    private void createRestaurantCard(
            Restaurant restaurant
    ) {

        MaterialCardView card =
                new MaterialCardView(this);


        card.setRadius(
                dp(16)
        );


        card.setCardElevation(
                dp(1)
        );


        card.setCardBackgroundColor(
                Color.WHITE
        );


        LinearLayout root =
                new LinearLayout(this);


        root.setOrientation(
                LinearLayout.VERTICAL
        );


        // =====================================================
        // RESTAURANT HEADER
        // =====================================================

        LinearLayout header =
                new LinearLayout(this);


        header.setOrientation(
                LinearLayout.HORIZONTAL
        );


        header.setGravity(
                Gravity.CENTER_VERTICAL
        );


        header.setPadding(
                dp(16),
                dp(16),
                dp(12),
                dp(16)
        );


        // Icon restoran

        ImageView icon =
                new ImageView(this);


        icon.setImageResource(
                R.drawable.ic_store
        );


        icon.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );


        header.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(40),
                        dp(40)
                )
        );


        // Informasi restoran

        LinearLayout info =
                new LinearLayout(this);


        info.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );


        infoParams.setMargins(
                dp(12),
                0,
                0,
                0
        );


        TextView tvName =
                new TextView(this);


        tvName.setText(
                restaurant.name
        );


        tvName.setTextColor(
                Color.rgb(
                        33,
                        33,
                        33
                )
        );


        tvName.setTextSize(
                17
        );


        tvName.setTypeface(
                null,
                Typeface.BOLD
        );








        info.addView(
                tvName
        );




        header.addView(
                info,
                infoParams
        );


        // Menu restoran

        ImageView menu =
                new ImageView(this);


        menu.setImageResource(
                R.drawable.ic_more_vert
        );


        menu.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );


        header.addView(
                menu,
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(32)
                )
        );


        root.addView(
                header
        );


        // =====================================================
        // DIVIDER
        // =====================================================

        View divider =
                new View(this);


        divider.setBackgroundColor(
                Color.rgb(
                        238,
                        238,
                        238
                )
        );


        root.addView(
                divider,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(1)
                )
        );


        // =====================================================
        // HEADER CABANG
        // =====================================================

        LinearLayout branchHeader =
                new LinearLayout(this);


        branchHeader.setOrientation(
                LinearLayout.HORIZONTAL
        );


        branchHeader.setGravity(
                Gravity.CENTER_VERTICAL
        );


        branchHeader.setPadding(
                dp(16),
                dp(12),
                dp(12),
                dp(4)
        );


        TextView branchTitle =
                new TextView(this);


        branchTitle.setText(
                "Cabang"
        );


        branchTitle.setTextSize(
                13
        );


        branchTitle.setTextColor(
                Color.rgb(
                        117,
                        117,
                        117
                )
        );


        branchTitle.setTypeface(
                null,
                Typeface.BOLD
        );


        branchHeader.addView(
                branchTitle,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );


        // Tambah cabang

        TextView addBranch =
                new TextView(this);


        addBranch.setText(
                "+ Tambah Cabang"
        );


        addBranch.setTextSize(
                13
        );


        addBranch.setTextColor(
                Color.rgb(
                        249,
                        168,
                        37
                )
        );


        addBranch.setTypeface(
                null,
                Typeface.BOLD
        );


        addBranch.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );


        branchHeader.addView(
                addBranch
        );


        root.addView(
                branchHeader
        );


        // =====================================================
        // CONTAINER CABANG
        // =====================================================

        LinearLayout branchContainer =
                new LinearLayout(this);


        branchContainer.setOrientation(
                LinearLayout.VERTICAL
        );


        root.addView(
                branchContainer
        );


        card.addView(
                root
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );


        cardParams.setMargins(
                0,
                0,
                0,
                dp(16)
        );


        restaurantContainer.addView(
                card,
                cardParams
        );


        // =====================================================
        // EVENT MENU RESTORAN
        // =====================================================

        menu.setOnClickListener(
                v -> showRestaurantMenu(
                        menu,
                        restaurant
                )
        );


        // =====================================================
        // TAMBAH CABANG
        // =====================================================

        addBranch.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    RestaurantActivity.this,
                                    RegisterBranchActivity.class
                            );


                    intent.putExtra(
                            "restaurant_id",
                            restaurant.id
                    );


                    startActivity(intent);

                }
        );


        // =====================================================
        // LOAD CABANG
        // =====================================================

        loadBranches(
                restaurant.id,
                branchContainer
        );

    }


    // =========================================================
    // LOAD CABANG
    // =========================================================

    private void loadBranches(
            long restaurantId,
            LinearLayout container
    ) {

        executor.execute(() -> {

            List<Branch> branches =
                    db.branchDao()
                            .getByRestaurant(
                                    restaurantId
                            );


            runOnUiThread(() -> {

                container.removeAllViews();


                if (branches == null ||
                        branches.isEmpty()) {

                    TextView empty =
                            new TextView(this);


                    empty.setText(
                            "Belum ada cabang."
                    );


                    empty.setTextSize(
                            13
                    );


                    empty.setTextColor(
                            Color.rgb(
                                    158,
                                    158,
                                    158
                            )
                    );


                    empty.setPadding(
                            dp(16),
                            dp(10),
                            dp(16),
                            dp(16)
                    );


                    container.addView(
                            empty
                    );


                    return;
                }


                for (Branch branch :
                        branches) {

                    createBranchView(
                            container,
                            branch
                    );

                }

            });

        });

    }


    // =========================================================
    // CABANG
    // =========================================================

    private void createBranchView(
            LinearLayout container,
            Branch branch
    ) {

        LinearLayout row =
                new LinearLayout(this);


        row.setOrientation(
                LinearLayout.HORIZONTAL
        );


        row.setGravity(
                Gravity.CENTER_VERTICAL
        );


        row.setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
        );


        // =====================================================
        // DOT
        // =====================================================

        View dot =
                new View(this);


        GradientDrawable dotBackground =
                new GradientDrawable();


        dotBackground.setShape(
                GradientDrawable.OVAL
        );


        dotBackground.setColor(
                Color.rgb(
                        249,
                        168,
                        37
                )
        );


        dot.setBackground(
                dotBackground
        );


        row.addView(
                dot,
                new LinearLayout.LayoutParams(
                        dp(8),
                        dp(8)
                )
        );


        // =====================================================
        // INFO CABANG
        // =====================================================

        LinearLayout info =
                new LinearLayout(this);


        info.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );


        infoParams.setMargins(
                dp(12),
                0,
                0,
                0
        );


        TextView name =
                new TextView(this);


        name.setText(
                branch.name
        );


        name.setTextSize(
                15
        );


        name.setTextColor(
                Color.rgb(
                        33,
                        33,
                        33
                )
        );


        if (branch.isMain) {

            name.setTypeface(
                    null,
                    Typeface.BOLD
            );

        }


        TextView address =
                new TextView(this);


        address.setText(
                branch.address == null
                        ? ""
                        : branch.address
        );


        address.setTextSize(
                12
        );


        address.setTextColor(
                Color.rgb(
                        117,
                        117,
                        117
                )
        );


        address.setPadding(
                0,
                dp(3),
                0,
                0
        );


        info.addView(
                name
        );


        info.addView(
                address
        );


        row.addView(
                info,
                infoParams
        );


        // =====================================================
        // LABEL CABANG UTAMA
        // =====================================================

        if (branch.isMain) {

            TextView label =
                    new TextView(this);


            label.setText(
                    "Utama"
            );


            label.setTextSize(
                    11
            );


            label.setTextColor(
                    Color.WHITE
            );


            label.setTypeface(
                    null,
                    Typeface.BOLD
            );


            label.setPadding(
                    dp(10),
                    dp(5),
                    dp(10),
                    dp(5)
            );


            GradientDrawable background =
                    new GradientDrawable();


            background.setColor(
                    Color.rgb(
                            67,
                            160,
                            71
                    )
            );


            background.setCornerRadius(
                    dp(20)
            );


            label.setBackground(
                    background
            );


            row.addView(
                    label
            );

        }


        // =====================================================
        // KLIK CABANG
        // =====================================================

        row.setClickable(
                true
        );


        row.setFocusable(
                true
        );


        row.setBackgroundResource(
                android.R.drawable.list_selector_background
        );


        row.setOnClickListener(
                v -> selectBranch(
                        branch.restaurantId,
                        branch.id
                )
        );

        setupBranchLongClick(
                row,
                branch
        );

        container.addView(
                row
        );

    }


    // =========================================================
    // MENU RESTORAN
    // =========================================================

    private void showRestaurantMenu(
            View anchor,
            Restaurant restaurant
    ) {

        PopupMenu popup =
                new PopupMenu(
                        RestaurantActivity.this,
                        anchor
                );


        popup.getMenu().add(
                "Edit Restoran"
        );


        popup.getMenu().add(
                "Kelola Cabang"
        );


        popup.getMenu().add(
                "Hapus Restoran"
        );


        popup.setOnMenuItemClickListener(
                item -> {

                    String title =
                            item.getTitle()
                                    .toString();


                    // =========================================
                    // EDIT
                    // =========================================

                    if (title.equals(
                            "Edit Restoran"
                    )) {

                        Intent intent =
                                new Intent(
                                        RestaurantActivity.this,
                                        EditRestaurantActivity.class
                                );


                        intent.putExtra(
                                "restaurant_id",
                                restaurant.id
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    // =========================================
                    // KELOLA CABANG
                    // =========================================

                    if (title.equals(
                            "Kelola Cabang"
                    )) {

                        Intent intent =
                                new Intent(
                                        RestaurantActivity.this,
                                        BranchActivity.class
                                );


                        intent.putExtra(
                                "restaurant_id",
                                restaurant.id
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    // =========================================
                    // HAPUS RESTORAN
                    // =========================================

                    if (title.equals(
                            "Hapus Restoran"
                    )) {

                        confirmDeleteRestaurant(
                                restaurant
                        );


                        return true;
                    }


                    return false;

                }
        );


        popup.show();

    }


    // =========================================================
    // KONFIRMASI HAPUS
    // =========================================================

    private void confirmDeleteRestaurant(
            Restaurant restaurant
    ) {

        new AlertDialog.Builder(
                RestaurantActivity.this
        )
                .setTitle(
                        "Hapus Restoran?"
                )
                .setMessage(
                        "Restoran \"" +
                                restaurant.name +
                                "\" dan seluruh cabangnya akan dihapus."
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .setPositiveButton(
                        "Hapus",
                        (dialog, which) -> {

                            deleteRestaurant(
                                    restaurant
                            );

                        }
                )
                .show();

    }


    // =========================================================
    // DELETE
    // =========================================================

    private void deleteRestaurant(
            Restaurant restaurant
    ) {

        executor.execute(() -> {

            // Hapus cabang terlebih dahulu

            db.branchDao()
                    .deleteByRestaurant(
                            restaurant.id
                    );


            // Kemudian restoran

            db.restaurantDao()
                    .delete(
                            restaurant
                    );


            runOnUiThread(() -> {

                Toast.makeText(
                        RestaurantActivity.this,
                        "Restoran berhasil dihapus",
                        Toast.LENGTH_SHORT
                ).show();


                loadRestaurants();

            });

        });

    }


    // =========================================================
    // PILIH CABANG
    // =========================================================

    private void selectBranch(
            long restaurantId,
            long branchId
    ) {

        Intent intent = new Intent(
                RestaurantActivity.this,
                EditBranchActivity.class
        );

        intent.putExtra(
                "restaurant_id",
                restaurantId
        );

        intent.putExtra(
                "branch_id",
                branchId
        );

        startActivity(intent);
    }

    private void setupBranchLongClick(
            View branchView,
            Branch branch
    ) {

        branchView.setOnLongClickListener(v -> {

            showBranchMenu(
                    branchView,
                    branch
            );

            return true;
        });
    }

    private void showBranchMenu(
            View anchor,
            Branch branch
    ) {

        PopupMenu popup = new PopupMenu(
                RestaurantActivity.this,
                anchor
        );

        popup.getMenu().add("Edit Cabang");

        if (!branch.isMain) {
            popup.getMenu().add("Jadikan Cabang Utama");
        }

        popup.getMenu().add("Hapus Cabang");

        // =====================================================
        // CEK MANAGER CABANG
        // =====================================================

        executor.execute(() -> {

            Manager manager =
                    db.managerDao()
                            .getByBranchId(branch.id);

            runOnUiThread(() -> {

                if (manager == null) {

                    // Belum memiliki manager
                    popup.getMenu().add(
                            "Tambah Manager Cabang"
                    );

                } else {

                    // Sudah memiliki manager
                    popup.getMenu().add(
                            "Edit Manager Cabang"
                    );
                }

                // =================================================
                // MENU CLICK
                // =================================================

                popup.setOnMenuItemClickListener(item -> {

                    String title =
                            item.getTitle().toString();


                    // =================================================
                    // EDIT CABANG
                    // =================================================

                    if (title.equals("Edit Cabang")) {

                        Intent intent = new Intent(
                                RestaurantActivity.this,
                                EditBranchActivity.class
                        );

                        intent.putExtra(
                                "branch_id",
                                branch.id
                        );

                        intent.putExtra(
                                "restaurant_id",
                                branch.restaurantId
                        );

                        startActivity(intent);

                        return true;
                    }


                    // =================================================
                    // JADIKAN CABANG UTAMA
                    // =================================================

                    if (title.equals(
                            "Jadikan Cabang Utama"
                    )) {

                        confirmChangeMainBranch(
                                branch
                        );

                        return true;
                    }


                    // =================================================
                    // HAPUS CABANG
                    // =================================================

                    if (title.equals(
                            "Hapus Cabang"
                    )) {

                        confirmDeleteBranch(
                                branch
                        );

                        return true;
                    }


                    // =================================================
                    // TAMBAH MANAGER
                    // =================================================

                    if (title.equals(
                            "Tambah Manager Cabang"
                    )) {

                        Intent intent = new Intent(
                                RestaurantActivity.this,
                                RegisterManagerActivity.class
                        );

                        intent.putExtra(
                                "restaurant_id",
                                branch.restaurantId
                        );

                        intent.putExtra(
                                "branch_id",
                                branch.id
                        );

                        startActivity(intent);

                        return true;
                    }


                    // =================================================
                    // EDIT MANAGER
                    // =================================================

                    if (title.equals(
                            "Edit Manager Cabang"
                    )) {

                        Intent intent = new Intent(
                                RestaurantActivity.this,
                                EditManagerActivity.class
                        );

                        intent.putExtra(
                                "restaurant_id",
                                branch.restaurantId
                        );

                        intent.putExtra(
                                "branch_id",
                                branch.id
                        );

                        intent.putExtra(
                                "manager_id",
                                manager.id
                        );

                        startActivity(intent);

                        return true;
                    }


                    return false;
                });

                popup.show();

            });

        });
    }

    private void confirmChangeMainBranch(Branch branch) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Jadikan Cabang Utama?")
                .setMessage(
                        "Cabang \"" +
                                branch.name +
                                "\" akan menjadi cabang utama.\n\n" +
                                "Cabang utama saat ini akan otomatis menjadi cabang biasa."
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .setPositiveButton(
                        "Jadikan Utama",
                        (dialog, which) -> {

                            changeMainBranch(branch);

                        }
                )
                .show();
    }

    private void changeMainBranch(Branch branch) {

        executor.execute(() -> {

            try {

                // Pastikan cabang masih ada
                Branch currentBranch =
                        db.branchDao()
                                .getById(branch.id);

                if (currentBranch == null) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                RestaurantActivity.this,
                                "Cabang tidak ditemukan",
                                Toast.LENGTH_SHORT
                        ).show();

                    });

                    return;
                }

                // Ganti cabang utama di Room
                db.branchDao()
                        .changeMainBranch(
                                branch.restaurantId,
                                branch.id
                        );


                runOnUiThread(() -> {

                    Toast.makeText(
                            RestaurantActivity.this,
                            "\"" + branch.name +
                                    "\" sekarang menjadi cabang utama",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Refresh tampilan
                    loadRestaurants();

                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            RestaurantActivity.this,
                            "Gagal mengganti cabang utama: " +
                                    e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });
    }
    private void confirmDeleteBranch(Branch branch) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Cabang?")
                .setMessage(
                        "Yakin ingin menghapus cabang \"" +
                                branch.name +
                                "\"?"
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .setPositiveButton(
                        "Hapus",
                        (dialog, which) -> {

                            deleteBranch(branch);

                        }
                )
                .show();
    }
//    private void deleteBranch(Branch branch) {
//
//        // =========================================
//        // CABANG UTAMA
//        // =========================================
//
//        if (branch.isMain) {
//
//            Toast.makeText(
//                    this,
//                    "Cabang utama tidak dapat dihapus.",
//                    Toast.LENGTH_LONG
//            ).show();
//
//            return;
//        }
//
//        FirebaseRepository firebase =
//                new FirebaseRepository();
//
//        firebase.deleteBranch(
//                branch.firebaseId,
//
//                new FirebaseRepository.OnCompleteListener() {
//
//                    @Override
//                    public void success() {
//
//                        executor.execute(() -> {
//
//                            db.branchDao()
//                                    .deleteById(branch.id);
//
//                            runOnUiThread(() -> {
//
//                                Toast.makeText(
//                                        RestaurantActivity.this,
//                                        "Cabang berhasil dihapus",
//                                        Toast.LENGTH_SHORT
//                                ).show();
//
//                                loadRestaurants();
//
//                            });
//
//                        });
//
//                    }
//
//                    @Override
//                    public void failed(String error) {
//
//                        runOnUiThread(() -> {
//
//                            Toast.makeText(
//                                    RestaurantActivity.this,
//                                    "Gagal menghapus cabang: " + error,
//                                    Toast.LENGTH_LONG
//                            ).show();
//
//                        });
//
//                    }
//                }
//        );
//    }

    private void deleteBranch(Branch branch) {

        if (branch.isMain) {

            Toast.makeText(
                    this,
                    "Cabang utama tidak dapat dihapus.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        AppDatabase db =
                DatabaseClient.getDatabase(this);

        FirebaseRepository firebase =
                new FirebaseRepository();

        executor.execute(() -> {

            // =====================================================
            // CARI MANAGER CABANG
            // =====================================================

            Manager manager =
                    db.managerDao()
                            .getByBranchId(
                                    branch.id
                            );

            runOnUiThread(() -> {

                // =================================================
                // JIKA ADA MANAGER
                // =================================================

                if (manager != null) {

                    firebase.deleteManager(
                            manager.firebaseId,
                            new FirebaseRepository.OnCompleteListener() {

                                @Override
                                public void success() {

                                    deleteBranchLocal(
                                            db,
                                            branch
                                    );
                                }

                                @Override
                                public void failed(
                                        String error
                                ) {

                                    Toast.makeText(
                                            RestaurantActivity.this,
                                            "Gagal menghapus Manager: " + error,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );

                } else {

                    // Tidak ada manager,
                    // langsung hapus cabang

                    deleteBranchLocal(
                            db,
                            branch
                    );
                }

            });

        });
    }

    public void deleteManager(
            String firebaseId,
            FirebaseRepository.OnCompleteListener listener
    ) {

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("managers")
                .document(firebaseId)
                .delete()
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
    }

    private void deleteBranchLocal(
            AppDatabase db,
            Branch branch
    ) {

        executor.execute(() -> {

            // =====================================================
            // HAPUS MANAGER DARI ROOM
            // =====================================================

            db.managerDao()
                    .deleteByBranchId(
                            branch.id
                    );

            // =====================================================
            // HAPUS CABANG DARI ROOM
            // =====================================================

            db.branchDao()
                    .deleteById(
                            branch.id
                    );

            runOnUiThread(() -> {

                Toast.makeText(
                        RestaurantActivity.this,
                        "Cabang dan Manager berhasil dihapus",
                        Toast.LENGTH_SHORT
                ).show();

                loadRestaurants();

            });

        });
    }
    // =========================================================
    // DP
    // =========================================================

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();

    }

}