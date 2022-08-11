package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Register extends AppCompatActivity {
    Button register;
    EditText et_first,et_last,et_email,et_password,et_type;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        register = findViewById(R.id.BTN_register_register);
        et_first = findViewById(R.id.ET_register_first);
        et_last = findViewById(R.id.ET_register_last);
        et_email = findViewById(R.id.ET_register_email);
        et_password = findViewById(R.id.ET_register_password);
        et_type = findViewById(R.id.ET_register_type);
        db = FirebaseFirestore.getInstance();
        logo = findViewById(R.id.IV_register_logo);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("staffITlogo.png")
                .getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        logo.setImageBitmap(bytesToBitmap(bytes));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("logo wasn't added ");
                    }
                });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String fullName = et_first.getText().toString() + " " + et_last.getText().toString();
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();
        String type = et_type.getText().toString();
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || type.isEmpty()){
            System.out.println("please fill out all fields");
            return;
        }else if (!type.toLowerCase().equals("user") && !type.toLowerCase().equals("manager") && type.toLowerCase().equals("admin")){
            System.out.println("in valid user type entered");
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            System.out.println("user added successfully on firebase");
                            db.collection("Users").document(email)
                                    .set(new User(email,fullName,type))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            System.out.println("new user added");
                                            et_email.setText("");
                                            et_first.setText("");
                                            et_type.setText("");
                                            et_last.setText("");
                                            et_password.setText("");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("new user couldn't be added");
                                        }
                                    });
                        }else {
                            System.out.println("user couldn't add successfully");
                            return;
                        }
                    }
                });
    }

    private Bitmap bytesToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }
}