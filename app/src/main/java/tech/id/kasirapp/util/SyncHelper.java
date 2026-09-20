package tech.id.kasirapp.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.DatabaseClient;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Combo;
import tech.id.kasirapp.data.local.entity.ComboItem;
import tech.id.kasirapp.data.local.entity.Diskon;
import tech.id.kasirapp.data.local.entity.Manager;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.data.local.entity.Restaurant;
import tech.id.kasirapp.data.local.entity.Ruangan;
import tech.id.kasirapp.data.local.entity.Waiter;

public class SyncHelper {

    private final AppDatabase db;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SyncHelper(Context context) {
        this.db = DatabaseClient.getDatabase(context);
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface OnSyncCompleteListener {
        void onSyncComplete();
    }

    public void syncOwnerData(String ownerFirebaseId, long ownerId, OnSyncCompleteListener listener) {
        executor.execute(() -> {
            // 1. Sync Restaurants
            firestore.collection("restaurants")
                    .whereEqualTo("ownerId", ownerId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        saveRestaurants(queryDocumentSnapshots);
                        
                        // 2. Sync Branches
                        firestore.collection("branches")
                                .whereEqualTo("ownerId", ownerId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    saveBranches(queryDocumentSnapshots1);
                                    
                                    // 3. Sync Managers, Menus, Waiters, Rooms, Discounts, Combos
                                    syncRemainingData("ownerId", ownerId, listener);
                                });
                    });
        });
    }

    public void syncBranchData(long branchId, OnSyncCompleteListener listener) {
        syncRemainingData("branchId", branchId, listener);
    }

    private void syncRemainingData(String field, Object value, OnSyncCompleteListener listener) {
        // This is a simplified sequential sync for brevity. 
        // In a real app, you might want to use Tasks.whenAll() or similar.
        
        firestore.collection("managers").whereEqualTo(field, value).get().addOnSuccessListener(this::saveManagers);
        firestore.collection("menus").whereEqualTo(field, value).get().addOnSuccessListener(this::saveMenus);
        firestore.collection("waiters").whereEqualTo(field, value).get().addOnSuccessListener(this::saveWaiters);
        firestore.collection("rooms").whereEqualTo(field, value).get().addOnSuccessListener(this::saveRooms);
        firestore.collection("discounts").whereEqualTo(field, value).get().addOnSuccessListener(this::saveDiscounts);
        firestore.collection("combos").whereEqualTo(field, value).get().addOnSuccessListener(queryDocumentSnapshots -> {
            saveCombos(queryDocumentSnapshots, listener);
        });
    }

    private void saveRestaurants(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Restaurant r = new Restaurant();
                r.firebaseId = doc.getId();
                r.ownerId = getLongSafe(doc, "ownerId");
                r.name = doc.getString("name");
                r.phone = doc.getString("phone");
                r.email = doc.getString("email");
                r.syncStatus = 1;
                db.restaurantDao().insert(r);
            }
        });
    }

    private void saveBranches(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Branch b = new Branch();
                b.firebaseId = doc.getId();
                b.restaurantId = getLongSafe(doc, "restaurantId");
                b.name = doc.getString("name");
                b.address = doc.getString("address");
                b.phone = doc.getString("phone");
                b.openTime = doc.getString("openTime");
                b.closeTime = doc.getString("closeTime");
                b.jumlahMeja = (int) getLongSafe(doc, "tableCount");
                b.tax = getDoubleSafe(doc, "tax");
                b.serviceCharge = getDoubleSafe(doc, "serviceCharge");
                b.sendToKitchen = doc.getBoolean("isKitchenSync") != null && doc.getBoolean("isKitchenSync");
                b.automaticStock = doc.getBoolean("isStockSync") != null && doc.getBoolean("isStockSync");
                b.syncStatus = 1;
                db.branchDao().insert(b);
            }
        });
    }

    private void saveManagers(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Manager m = new Manager();
                m.firebaseId = doc.getId();
                m.branchId = getLongSafe(doc, "branchId");
                m.name = doc.getString("name");
                m.phone = doc.getString("phone");
                m.username = doc.getString("username");
                m.role = "manager";
                m.syncStatus = 1;
                db.managerDao().insert(m);
            }
        });
    }

    private void saveMenus(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Menu m = new Menu();
                m.firebaseId = doc.getId();
                m.branchId = getLongSafe(doc, "branchId");
                m.name = doc.getString("name");
                m.sku = doc.getString("sku");
                m.type = doc.getString("type");
                m.category = doc.getString("category");
                m.price = getDoubleSafe(doc, "price");
                m.unit = doc.getString("unit");
                m.status = doc.getString("status");
                m.isAvailable = doc.getBoolean("availability") != null && doc.getBoolean("availability");
                m.description = doc.getString("description");
                m.imagePath = doc.getString("imageUrl");
                m.syncStatus = 1;
                db.menuDao().insert(m);
            }
        });
    }

    private void saveWaiters(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Waiter w = new Waiter();
                w.firebaseId = doc.getId();
                w.branchId = getLongSafe(doc, "branchId");
                w.name = doc.getString("name");
                w.phone = doc.getString("phone");
                w.nik = doc.getString("nik");
                w.address = doc.getString("address");
                w.employeeNumber = doc.getString("employeeNumber");
                w.education = doc.getString("education");
                w.username = doc.getString("username");
                w.isActive = doc.getBoolean("status") != null && doc.getBoolean("status");
                w.photoPath = doc.getString("imageUrl");
                w.ktpPhotoPath = doc.getString("ktpUrl");
                w.role = "waiter";
                w.syncStatus = 1;
                db.waiterDao().insert(w);
            }
        });
    }

    private void saveRooms(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Ruangan r = new Ruangan();
                r.firebaseId = doc.getId();
                r.branchId = getLongSafe(doc, "branchId");
                r.name = doc.getString("name");
                r.tableCount = (int) getLongSafe(doc, "tableCount");
                r.syncStatus = 1;
                db.ruanganDao().insert(r);
            }
        });
    }

    private void saveDiscounts(QuerySnapshot snapshots) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Diskon d = new Diskon();
                d.firebaseId = doc.getId();
                d.branchId = getLongSafe(doc, "branchId");
                d.restaurantId = getLongSafe(doc, "restaurantId");
                d.ownerId = getLongSafe(doc, "ownerId");
                d.name = doc.getString("name");
                d.type = doc.getString("type");
                d.value = getDoubleSafe(doc, "value");
                d.isPercentage = doc.getBoolean("isPercentage") != null && doc.getBoolean("isPercentage");
                d.isActive = doc.getBoolean("isActive") != null && doc.getBoolean("isActive");
                d.menuId = getLongSafe(doc, "menuId");
                d.menuName = doc.getString("menuName");
                d.syncStatus = 1;
                db.diskonDao().insert(d);
            }
        });
    }

    private void saveCombos(QuerySnapshot snapshots, OnSyncCompleteListener listener) {
        executor.execute(() -> {
            for (DocumentSnapshot doc : snapshots) {
                Combo c = new Combo();
                c.firebaseId = doc.getId();
                c.branchId = getLongSafe(doc, "branchId");
                c.name = doc.getString("name");
                c.price = getDoubleSafe(doc, "price");
                c.description = doc.getString("description");
                c.status = doc.getString("status");
                c.syncStatus = 1;
                long id = db.comboDao().insert(c);

                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                if (items != null) {
                    List<ComboItem> comboItems = new ArrayList<>();
                    for (Map<String, Object> itemMap : items) {
                        ComboItem ci = new ComboItem();
                        ci.comboId = id;
                        ci.menuId = ((Number) itemMap.get("menuId")).longValue();
                        ci.menuName = (String) itemMap.get("menuName");
                        ci.quantity = ((Number) itemMap.get("quantity")).intValue();
                        comboItems.add(ci);
                    }
                    db.comboItemDao().insertAll(comboItems);
                }
            }
            if (listener != null) listener.onSyncComplete();
        });
    }

    private long getLongSafe(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) return ((Number) val).longValue();
        return 0;
    }

    private double getDoubleSafe(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}