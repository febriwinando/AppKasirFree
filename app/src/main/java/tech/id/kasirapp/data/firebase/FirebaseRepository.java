package tech.id.kasirapp.data.firebase;

import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for handling Firebase Firestore and Firebase Storage operations.
 * Provides methods for saving, updating, and deleting restaurant-related entities.
 */
public class FirebaseRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public FirebaseRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public interface OnCompleteListener {
        void success();
        void failed(String error);
    }

    public interface OnImageUploadListener {
        void success(String downloadUrl);
        void failed(String error);
    }

    // --- Image Upload Helper ---

    public void uploadImage(String folder, String fileName, String localPath, OnImageUploadListener listener) {
        if (localPath == null || localPath.isEmpty()) {
            if (listener != null) listener.success("");
            return;
        }

        try {
            Uri fileUri = Uri.parse(localPath);
            // Check if it's already a remote URL
            if (localPath.startsWith("http")) {
                if (listener != null) listener.success(localPath);
                return;
            }
            
            File file = new File(fileUri.getPath());
            if (!file.exists()) {
                if (listener != null) listener.success(localPath); 
                return;
            }

            StorageReference ref = storage.getReference().child(folder).child(fileName + ".jpg");
            ref.putFile(fileUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    if (listener != null) listener.success(uri.toString());
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.failed(e.getMessage());
                });
        } catch (Exception e) {
            if (listener != null) listener.failed(e.getMessage());
        }
    }

    public void deleteImage(String imageUrl, OnCompleteListener listener) {
        if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.startsWith("http")) {
            if (listener != null) listener.success();
            return;
        }

        try {
            storage.getReferenceFromUrl(imageUrl).delete()
                .addOnSuccessListener(unused -> {
                    if (listener != null) listener.success();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.failed(e.getMessage());
                });
        } catch (Exception e) {
            if (listener != null) listener.failed(e.getMessage());
        }
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
            long createdAt,
            int syncStatus,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("name", name);
        data.put("ownerName", owner);
        data.put("phone", phone);
        data.put("email", email);
        data.put("ownerId", ownerId);
        data.put("isActive", isActive);
        data.put("createdAt", createdAt);
        data.put("syncStatus", syncStatus);

        handleTask(firestore.collection("restaurants").document(firebaseId).set(data), listener);
    }

    public void deleteRestaurant(String firebaseId, OnCompleteListener listener) {
        handleTask(firestore.collection("restaurants").document(firebaseId).delete(), listener);
    }

    // --- Owner ---

    public void saveOwner(
            String firebaseId,
            String name,
            String username,
            String password,
            String email,
            String phone,
            String role,
            int syncStatus,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("email", email);
        data.put("phone", phone);
        data.put("role", role);
        data.put("syncStatus", syncStatus);

        handleTask(firestore.collection("owners").document(firebaseId).set(data), listener);
    }

    // --- Branch ---

    public void saveBranch(
            String firebaseId,
            long restaurantFirebaseId,
            String name,
            long restaurantId,
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
            int syncStatus,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("restaurantFirebaseId", restaurantFirebaseId);
        data.put("restaurantId", restaurantId);
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
        data.put("syncStatus", syncStatus);

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
            String role,
            int syncStatus,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("branchFirebaseId", branchFirebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("role", role);
        data.put("syncStatus", syncStatus);

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

    public void deleteMenu(String firebaseId, OnCompleteListener listener) {
        handleTask(firestore.collection("menus").document(firebaseId).delete(), listener);
    }

    public void deleteWaiter(String firebaseId, OnCompleteListener listener) {
        handleTask(firestore.collection("waiters").document(firebaseId).delete(), listener);
    }

    // --- Staff (Waiter, Cashier, Kitchen Staff) ---

    public void saveWaiter(
            String firebaseId,
            String branchFirebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            String nik,
            String address,
            String education,
            String photoPath,
            String ktpPhotoPath,
            String employeeNumber,
            String role,
            boolean isActive,
            int syncStatus,
            long restaurantId,
            long ownerId,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("branchFirebaseId", branchFirebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("nik", nik);
        data.put("address", address);
        data.put("education", education);
        data.put("photoPath", photoPath);
        data.put("ktpPhotoPath", ktpPhotoPath);
        data.put("employeeNumber", employeeNumber);
        data.put("role", role);
        data.put("isActive", isActive);
        data.put("syncStatus", syncStatus);
        data.put("restaurantId", restaurantId);
        data.put("ownerId", ownerId);

        handleTask(firestore.collection("waiters").document(firebaseId).set(data), listener);
    }

    public void saveMenu(
            String firebaseId,
            long branchId,
            String name,
            String type,
            String category,
            String unit,
            String sku,
            double costPrice,
            double price,
            String description,
            String imagePath,
            int stock,
            String status,
            boolean isAvailable,
            int syncStatus,
            long restaurantId,
            long ownerId,
            OnCompleteListener listener
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", firebaseId);
        data.put("firebaseId", firebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("type", type);
        data.put("category", category);
        data.put("unit", unit);
        data.put("sku", sku);
        data.put("costPrice", costPrice);
        data.put("price", price);
        data.put("description", description);
        data.put("imagePath", imagePath);
        data.put("stock", stock);
        data.put("status", status);
        data.put("isAvailable", isAvailable);
        data.put("syncStatus", syncStatus);
        data.put("restaurantId", restaurantId);
        data.put("ownerId", ownerId);

        handleTask(firestore.collection("menus").document(firebaseId).set(data), listener);
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