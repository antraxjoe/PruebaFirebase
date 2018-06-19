package com.example.jose6.pruebafirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 123;

    private static final String KEY_IDSTORAGE = "idStorage";
    private static final String KEY_PRICE = "price";

    private EditText editTextFood;
    private TextView textView;
    private ImageView imageView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference foodsRef = db.collection("foods");

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    private FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFood = findViewById(R.id.editTextFood);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        if (auth.getCurrentUser() != null) {
            Toast.makeText(this, "Ya estas logeado", Toast.LENGTH_SHORT).show();
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "Logeado", Toast.LENGTH_SHORT).show();
            } else {
                // Sign in failed, check response for error code
                Toast.makeText(this, "Error logeo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void signOut(View v) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        foodsRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Toast.makeText(MainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, e.toString());
//                    return;
//                }
//
//                if (documentSnapshot.exists()) {
//                    String title = documentSnapshot.getString(KEY_TITLE);
//                    String description = documentSnapshot.getString(KEY_DESCRIPTION);
//
//                    textView.setText("Title: " + title + "\n" + "Description: " + description);
//                }
//            }
//        });
//    }

//    public void saveNote(View v) {
//        String title = editTextTitle.getText().toString();
//        String description = editTextDescription.getText().toString();
//
//        Map<String, Object> note = new HashMap<>();
//        note.put(KEY_TITLE, title);
//        note.put(KEY_DESCRIPTION, description);
//
//        noteRef.set(note)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(MainActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, e.toString());
//                    }
//                });
//    }

    public void loadImage(View v) {
        String foodName = editTextFood.getText().toString();

        foodsRef.document(foodName).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String idStorage = documentSnapshot.getString(KEY_IDSTORAGE);
                            long price = documentSnapshot.getLong(KEY_PRICE);

                            textView.setText("ID: " + idStorage + "\n" + "Precio: " + price + " $");

                            StorageReference foodsStoRef = storageRef.child(idStorage);

                            GlideApp.with(getApplicationContext())
                                    .load(foodsStoRef)
                                    .into(imageView);
                        } else {
                            Toast.makeText(MainActivity.this, "El documento no existe.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }


}
