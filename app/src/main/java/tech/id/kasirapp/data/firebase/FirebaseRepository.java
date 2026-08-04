package tech.id.kasirapp.data.firebase;


import com.google.firebase.firestore.DocumentReference;
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
            String address,

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
                "address",
                address
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

}