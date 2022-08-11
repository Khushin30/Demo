package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//@TODO Build APK
public class MainActivity extends AppCompatActivity {

    static final String AUTHENTICATION = "authentication";
    static final String LOGO = "logo";
    String userType = "user";
    Button login, query, register, add;
    FirebaseAuth auth;
    EditText et_username, et_password;
    BootstrapButton logOut;
    ImageView iv_Logo;
    byte[] logoBytes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE }, 100);
        }
        auth = FirebaseAuth.getInstance();
        register = findViewById(R.id.BTN_main_register);
        add = findViewById(R.id.BTN_main_add);
        query = findViewById(R.id.BTN_main_query);
        et_username = findViewById(R.id.ET_username);
        et_password = findViewById(R.id.ET_password);
        login = findViewById(R.id.BTN_main_login);
        logOut = findViewById(R.id.BTN_logOut);
        iv_Logo = findViewById(R.id.IV_main_logo);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("staffITlogo.png")
                .getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        logoBytes = bytes;
                        iv_Logo.setImageBitmap(bytesToBitmap(logoBytes));
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
                Intent intent = new Intent(MainActivity.this,Register.class);
                intent.putExtra(AUTHENTICATION,userType);
                intent.putExtra(LOGO, logoBytes);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser();
            }
        });
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,GetTransaction.class);
                intent.putExtra(AUTHENTICATION,userType);
                intent.putExtra(LOGO, logoBytes);
                startActivity(intent);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddTransaction.class);
                intent.putExtra(AUTHENTICATION,userType);
                intent.putExtra(LOGO, logoBytes);
                startActivity(intent);
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOff();
            }
        });
    }

    private void authenticateUser() {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        if (username.isEmpty() || password.isEmpty()){
            System.out.println("improper username or password entered");
            return;
        }
        auth.signInWithEmailAndPassword(username,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            System.out.println("auth success");
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Users").document(username)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                User user = task.getResult().toObject(User.class);
                                                userType = user.getType();
                                                if (userType.toLowerCase().equals("admin")){
                                                    query.setVisibility(View.VISIBLE);
                                                    add.setVisibility(View.VISIBLE);
                                                    register.setVisibility(View.VISIBLE);
                                                }else if (userType.toLowerCase().equals("manager")) {
                                                    query.setVisibility(View.VISIBLE);
                                                    add.setVisibility(View.VISIBLE);
                                                    register.setVisibility(View.GONE);
                                                }else {
                                                    query.setVisibility(View.VISIBLE);
                                                    add.setVisibility(View.GONE);
                                                    register.setVisibility(View.GONE);
                                                }
                                                logOut.setText("Log off");
                                            }
                                        }
                                    });
                        }else {
                            System.out.println("auth failed");
                        }
                    }
                });
        et_password.setText("");
        et_username.setText("");

    }

    public void logOff(){
        auth.signOut();
        query.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        register.setVisibility(View.GONE);
        et_password.setText("");
        et_username.setText("");
        logOut.setText("");
    }

    private Bitmap bytesToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }
}