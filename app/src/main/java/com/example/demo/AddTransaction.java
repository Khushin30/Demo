package com.example.demo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.primitives.Bytes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

// @TODO check for errors

public class AddTransaction extends AppCompatActivity {

    FirebaseFirestore db;
    StorageReference storageRef;
    EditText et_date, et_type, et_amount, et_description, et_URL;
    Button btn_uploadIMG, btn_addTransaction, btn_camera, btn_load;
    ImageView iv_showImage;
    Bitmap transaction_image = null;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    boolean cameraPictureAdded;
    int transactionID;
    FirebaseAuth auth;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        et_URL = findViewById(R.id.ET_transactionURL);
        btn_addTransaction = findViewById(R.id.BTN_add);
        btn_uploadIMG = findViewById(R.id.BTN_uploadIMG);
        et_date = findViewById(R.id.ET_transactionDate);
        et_type = findViewById(R.id.ET_transactionType);
        et_amount = findViewById(R.id.ET_transactionAmount);
        et_description = findViewById(R.id.ET_transactionDescription);
        iv_showImage = findViewById(R.id.IV_showPicture);
        btn_camera = findViewById(R.id.BTN_camera);
        btn_load = findViewById(R.id.BTN_load);
        transaction_image = null;
        transactionID = -1;
        logo = findViewById(R.id.IV_transaction_logo);
        storageRef.child("staffITlogo.png")
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
        fetchID fetchID = new fetchID();
        fetchID.run();



        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        transaction_image = (Bitmap) data.getExtras().get("data");
                        iv_showImage.setImageBitmap(transaction_image);
                        cameraPictureAdded = true;
                    }
                });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AddTransaction.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(AddTransaction.this, new String[] { Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE }, 100);
                }else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    someActivityResultLauncher.launch(intent);
                }
            }
        });

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (transaction_image == null){
                    System.out.println("your image is still loading please wait");

                }else {
                    iv_showImage.setImageBitmap(transaction_image);
                    System.out.println("map added");
                }
            }
        });

        btn_uploadIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = et_URL.getText().toString();
                if (!url.isEmpty() && !cameraPictureAdded){
                    FetchImage ft = new FetchImage(url);
                    ft.start();
                }
            }
        });

        btn_addTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = et_date.getText().toString();
                String[] date_tokens = date.split("/");
                int month = Integer.parseInt(date_tokens[0]) - 1;
                int day = Integer.parseInt(date_tokens[1]);
                int year = Integer.parseInt(date_tokens[2]);
                if (!validDate(month,day,year)){
                    System.out.println("invalid date, please put new date");
                    et_date.setText("");
                    return;
                }
                Calendar cal = Calendar.getInstance();
                try {
                    cal.set(year,month,day);
                    System.out.println("date set");
                }catch (Exception e){
                    System.out.println("you've entered an invalid date please try again");
                    et_date.setText("");
                    return;
                }
                Date transaction_date = cal.getTime();
                String typeInput = et_type.getText().toString();
                String type;
                if (typeInput.equalsIgnoreCase("credit")){
                    type = "C";
                }else if (typeInput.equalsIgnoreCase("debit")){
                    type = "D";
                }else {
                    System.out.println("invalid trans type");
                    et_type.setText("");
                    return;
                }
                double amount = Double.parseDouble(et_amount.getText().toString());
                amount = Math.round(amount*100.0)/100.0;
                String description = et_description.getText().toString();

                if (transaction_image == null){
                    addTransaction(new Transaction(transactionID,transaction_date,type,amount,description),null);
                }else {
                    addTransaction(new Transaction(transactionID,transaction_date,type,amount,description), bitmapToBytes(transaction_image));
                }
            }
        });
    }

    private boolean validDate(int month, int day, int year) {
        if (month<=11 && month>=0 && day<=31 && day>=1 && year>=1000 && year<=9999){
            return true;
        }else {
            return false;
        }
    }

    private void addTransaction(Transaction transaction, byte[] image) {

        db.collection("Transaction").document(Integer.toString(transaction.getId()))
                .set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("trans added");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("failed to add");
                    }
                });
        if (image != null){
            storageRef.child(Integer.toString(transaction.getId()))
                    .putBytes(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("image added");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("image not added \n" + e.toString());
                        }
                    });
        }else {
            db.collection("Transaction").document(Integer.toString(transaction.getId()))
                    .update("hasImage",false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            System.out.println("hasImage added");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("hasImage not added");
                        }
                    });
        }
        incrementID incrementID = new incrementID();
        incrementID.run();
        transactionID++;
        et_URL.setText("");
        et_description.setText("");
        et_amount.setText("");
        et_type.setText("");
        et_date.setText("");
    }

    class FetchImage extends Thread{

        private String URL;

        FetchImage(String url) {
            this.URL = url;
        }
        @Override
        public void run() {
            transaction_image = null;
            InputStream inputStream = null;
            try {
                inputStream = new java.net.URL(URL).openStream();
                transaction_image = BitmapFactory.decodeStream(inputStream);
                System.out.println("map created");
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class fetchID extends Thread{
        @Override
        public void run(){
            DocumentReference idReference = db.collection("ID").document("ID");
            idReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.exists()){
                            long id = (long) snapshot.get("ID");
                            transactionID = (int) id;
                            System.out.println(transactionID);
                            if (auth.getCurrentUser() == null){
                                System.out.println("signed in");
                            }else {
                                System.out.println("not signed in");
                            }
                        }
                    }
                }
            });
        }
    }

    class incrementID extends Thread{
        @Override
        public void run() {
            long newID = (long) (transactionID+1);
            db.collection("ID").document("ID")
                    .update("ID", newID);
        }
    }

    public byte[] bitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private Bitmap bytesToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }
}