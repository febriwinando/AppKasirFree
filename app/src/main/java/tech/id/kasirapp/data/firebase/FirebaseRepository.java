package tech.id.kasirapp.data.firebase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRepository {
    FirebaseFirestore firestore;

    public FirebaseRepository(){
        firestore = FirebaseFirestore.getInstance();
    }

    public void saveRestaurant(

            String firebaseId,
            String name,
            String owner,
            String phone,
            String email,
            long ownerId,
            boolean isActive,
            OnCompleteListener listener
    ){


        Map<String,Object> data =
                new HashMap<>();

        data.put(
                "name",
                name
        );

        data.put(
                "ownerName",
                owner
        );

        data.put(
                "phone",
                phone
        );

        data.put(
                "email",
                email
        );


        data.put(
                "createdAt",
                System.currentTimeMillis()
        );



        firestore
                .collection("restaurants")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    listener.success();
                })

                .addOnFailureListener(e -> {
                    listener.failed(
                            e.getMessage()
                    );

                });
    }

    public interface OnCompleteListener{
        void success();

        void failed(String error);
    }

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
    ){

        Map<String,Object> data = new HashMap<>();

        data.put("id", firebaseId);
        data.put("restaurantId", restaurantFirebaseId);
        data.put("name", name);
        data.put("address", address);
        data.put("phone", phone);
        data.put("openTime", openTime);
        data.put("closeTime", closeTime);
        data.put("isMain", isMain);
        data.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("branches")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(unused -> listener.success())
                .addOnFailureListener(e -> listener.failed(e.getMessage()));

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

        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "name",
                name
        );

        data.put(
                "address",
                address
        );

        data.put(
                "phone",
                phone
        );

        data.put(
                "openTime",
                openTime
        );

        data.put("jumlahMeja", jumlahMeja);

        data.put(
                "closeTime",
                closeTime
        );

        data.put(
                "isMain",
                isMain
        );


        // Pajak & service

        data.put(
                "tax",
                tax
        );

        data.put(
                "serviceCharge",
                serviceCharge
        );


        // Metode penjualan

        data.put(
                "dineIn",
                dineIn
        );

        data.put(
                "takeAway",
                takeAway
        );

        data.put(
                "delivery",
                delivery
        );


        // Operasional

        data.put(
                "sendToKitchen",
                sendToKitchen
        );

        data.put(
                "automaticStock",
                automaticStock
        );

        data.put(
                "allowNegativeStock",
                allowNegativeStock
        );


        firestore
                .collection("branches")
                .document(firebaseId)
                .update(data)
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
    }

    public void deleteBranch(
            String firebaseId,
            OnCompleteListener listener
    ) {

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("branches")
                .document(firebaseId)
                .delete()
                .addOnSuccessListener(unused -> {

                    listener.success();

                })
                .addOnFailureListener(e -> {

                    listener.failed(
                            e.getMessage()
                    );

                });
    }
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

        Map<String, Object> data =
                new HashMap<>();

        data.put("id", firebaseId);
        data.put("branch_id", branchFirebaseId);
        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);

        data.put(
                "created_at",
                FieldValue.serverTimestamp()
        );

        firestore.collection("managers")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(unused -> {

                    listener.success();

                })
                .addOnFailureListener(e -> {

                    listener.failed(
                            e.getMessage()
                    );

                });
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

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "branchId",
                branchId
        );

        data.put(
                "name",
                name
        );

        data.put(
                "username",
                username
        );

        data.put(
                "password",
                password
        );

        data.put(
                "phone",
                phone
        );

        db.collection("managers")
                .document(firebaseId)
                .update(data)
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
    }

    public void deleteManager(
            String firebaseId,
            OnCompleteListener listener
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

    public void saveWaiter(
            String firebaseId,
            long branchId,
            String name,
            String username,
            String password,
            String phone,
            OnCompleteListener listener
    ) {

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> data =
                new HashMap<>();

        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);

        db.collection("waiters")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
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

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> data =
                new HashMap<>();

        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);

        db.collection("cashiers")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
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

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> data =
                new HashMap<>();

        data.put("branchId", branchId);
        data.put("name", name);
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);

        db.collection("kitchen_staff")
                .document(firebaseId)
                .set(data)
                .addOnSuccessListener(
                        unused -> listener.success()
                )
                .addOnFailureListener(
                        e -> listener.failed(
                                e.getMessage()
                        )
                );
    }



}