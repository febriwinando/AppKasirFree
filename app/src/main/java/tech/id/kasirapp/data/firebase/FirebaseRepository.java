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

}