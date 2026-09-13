package tech.id.kasirapp.data.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for handling Firebase Firestore operations.
 * Provides methods for saving, updating, and deleting restaurant-related entities.
 */
public class FirebaseRepository {
    private final FirebaseFirestore firestore;

    public FirebaseRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface OnCompleteListener {
        void success();
        void failed(String error);
    }

    // --- Helper Methods ---

    private void handleTask(Task<?> task, OnCompleteListener listener) {
        task.addOnSuccessListener(unused -> {
            if (listener != null) listener.success();
        }).addOnFailureListener(e -> {
            if (listener != null) listener.failed(e.getMessage());
        });
    }

    // --- Restaurant ---

    public void saveRestaurant(
            String firebaseId,
            String name,
            String owner,
            String phone,
            String email,
            long ownerId,
            boolean isActive,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("ownerName", owner);
        data.put("phone", phone);
        data.put("email", email);
        data.put("ownerId", ownerId);
        data.put("isActive", isActive);
        data.put("createdAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("restaurants").document(firebaseId).set(data), listener);
    }

    // --- Owner ---

    public void saveOwner(
            String firebaseId,
            String name,
            String username,
            String email,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("name", name);
        data.put("username", username);
        data.put("email", email);
        data.put("password", password);
        data.put("phone", phone);
        data.put("createdAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("owners").document(firebaseId).set(data), listener);
    }

    // --- Branch ---

    public void saveBranch(
            String firebaseId,
            String restaurantFirebaseId,
            String name,
            long restaurantId,
            String address,
            String phone,
            String openTime,
            String closeTime,
            boolean isMain,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("restaurantFirebaseId", restaurantFirebaseId);
        data.put("restaurantId", restaurantId);
        data.put("name", name);
        data.put("address", address);
        data.put("phone", phone);
        data.put("openTime", openTime);
        data.put("closeTime", closeTime);
        data.put("isMain", isMain);
        data.put("createdAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("branches").document(firebaseId).set(data), listener);
    }

    public void updateBranch(
            String firebaseId,
            String name,
            String address,
            String phone,
            String openTime,
            String closeTime,
            boolean isMain,
            double tax,
            double serviceCharge,
            int jumlahMeja,
            boolean dineIn,
            boolean takeAway,
            boolean delivery,
            boolean sendToKitchen,
            boolean automaticStock,
            boolean allowNegativeStock,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("address", address);
        data.put("phone", phone);
        data.put("openTime", openTime);
        data.put("closeTime", closeTime);
        data.put("isMain", isMain);
        data.put("tax", tax);
        data.put("serviceCharge", serviceCharge);
        data.put("jumlahMeja", jumlahMeja);
        data.put("dineIn", dineIn);
        data.put("takeAway", takeAway);
        data.put("delivery", delivery);
        data.put("sendToKitchen", sendToKitchen);
        data.put("automaticStock", automaticStock);
        data.put("allowNegativeStock", allowNegativeStock);
        data.put("updatedAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("branches").document(firebaseId).update(data), listener);
    }

    public void deleteBranch(String firebaseId, OnCompleteListener listener) {
        handleTask(firestore.collection("branches").document(firebaseId).delete(), listener);
    }

    // --- Manager ---

    public void saveManager(
            String firebaseId,
            String branchFirebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("branchFirebaseId", branchFirebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("createdAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("managers").document(firebaseId).set(data), listener);
    }

    public void updateManager(
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("updatedAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection("managers").document(firebaseId).update(data), listener);
    }

    public void deleteManager(String firebaseId, OnCompleteListener listener) {
        handleTask(firestore.collection("managers").document(firebaseId).delete(), listener);
    }

    // --- Staff (Waiter, Cashier, Kitchen Staff) ---

    public void saveWaiter(
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        saveStaff("waiters", firebaseId, branchId, name, username, password, phone, listener);
    }

    public void saveCashier(
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        saveStaff("cashiers", firebaseId, branchId, name, username, password, phone, listener);
    }

    public void saveKitchenStaff(
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        saveStaff("kitchen_staff", firebaseId, branchId, name, username, password, phone, listener);
    }

    private void saveStaff(
            String collection,
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("createdAt", FieldValue.serverTimestamp());

        handleTask(firestore.collection(collection).document(firebaseId).set(data), listener);
    }
}