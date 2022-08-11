package com.example.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GetTransaction extends AppCompatActivity {

//@TODO holder on second thread

    FirebaseFirestore db;
    ArrayList<Transaction> transactions = new ArrayList<>();
    Button add;
    RadioGroup rg_OrderBy;
    RadioButton rb_ByID, rb_ByDate, rb_ByAmount;
    EditText et_specID, et_before,et_after,et_lessThan,et_greaterThan;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_transaction);
        db = FirebaseFirestore.getInstance();
        rg_OrderBy = findViewById(R.id.sortByRadioGroup);
        rb_ByAmount = findViewById(R.id.RB_byAmount);
        rb_ByDate = findViewById(R.id.RB_byDate);
        rb_ByID = findViewById(R.id.RB_byID);
        et_after = findViewById(R.id.ET_After);
        et_before = findViewById(R.id.ET_Before);
        et_lessThan = findViewById(R.id.ET_lessThan);
        et_greaterThan = findViewById(R.id.ET_moreThan);
        et_specID = findViewById( R.id.ET_Get_specificID);
        add = findViewById(R.id.BUTTON);
        logo = findViewById(R.id.IV_query_logo);
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

        rb_ByID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_specID.setVisibility(View.VISIBLE);
                et_after.setVisibility(View.GONE);
                et_before.setVisibility(View.GONE);
                et_greaterThan.setVisibility(View.GONE);
                et_lessThan.setVisibility(View.GONE);

            }
        });
        rb_ByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_specID.setVisibility(View.GONE);
                et_after.setVisibility(View.VISIBLE);
                et_before.setVisibility(View.VISIBLE);
                et_greaterThan.setVisibility(View.GONE);
                et_lessThan.setVisibility(View.GONE);
            }
        });
        rb_ByAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_specID.setVisibility(View.GONE);
                et_after.setVisibility(View.GONE);
                et_before.setVisibility(View.GONE);
                et_greaterThan.setVisibility(View.VISIBLE);
                et_lessThan.setVisibility(View.VISIBLE);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("pressed");
                if (selectedRadioButton() == R.id.RB_byAmount){
                    setTransactionsByAmount();
                }else if (selectedRadioButton() == R.id.RB_byDate){
                    setTransactionsByDate();
                }else if (selectedRadioButton() == R.id.RB_byID){
                    setTransactions();
                }else {
                    System.out.println("its wrong");
                }
//                System.out.println("holder reached");
//                MyAdapter1 adapter = new MyAdapter1(GetTransaction.this,transactions);
//                RecyclerView rv = findViewById(R.id.RV_get_show);
//                rv.setLayoutManager(new LinearLayoutManager(GetTransaction.this));
//                rv.setAdapter(adapter);
            }
        });
    }

    private void setTransactionsByAmount() {
        System.out.println("by amount reached");
        transactions.clear();
        double max;
        double min;
        if (!et_lessThan.getText().toString().equals("")){
            max = Double.parseDouble(et_lessThan.getText().toString());
        }else {
            max = 10000000.0;
        }

        if (!et_greaterThan.getText().toString().equals("")){
            min = Double.parseDouble(et_greaterThan.getText().toString());
        }else{
            min = -1.0;
        }
        db.collection("Transaction")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document: task.getResult()){
                                Transaction transaction = (Transaction) document.toObject(Transaction.class);
                                if (transaction.getAmount()<=max && transaction.getAmount()>=min){
                                    transactions.add(transaction);
                                    System.out.println(transaction);
                                }
                            }
                        }else {
                            System.out.println("unable to get by date");
                        }
                        showTransactions();
                    }
                });

    }

    private void setTransactionsByDate() {
        System.out.println("by date reached");
        transactions.clear();
        System.out.println(transactions);
        Date before;
        Date after;
        if (!et_before.getText().toString().equals("")){
            before = stringToDate(et_before.getText().toString());
        }else {
            before = stringToDate("01/01/2975");
        }

        if (!et_after.getText().toString().equals("")){
            after = stringToDate(et_after.getText().toString());
        }else {
            after = stringToDate("01/01/1975");
        }

        db.collection("Transaction")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document: task.getResult()){

                                Transaction transaction = (Transaction) document.toObject(Transaction.class);
                                if (transaction.getDate().after(after) && transaction.getDate().before(before)){
                                    transactions.add(transaction);
                                    System.out.println(transaction.getDate().before(before));
                                    System.out.println(transaction.getDate().after(after));

                                    System.out.println(transaction);
                                }
                            }
                        }else {
                            System.out.println("unable to get by date");
                        }
                        showTransactions();
                    }
                });
    }

    private void setTransactions() {
        System.out.println("by reached");
        transactions.clear();
        if (et_specID.getText().toString().equals("")){
            db.collection("Transaction")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot document: task.getResult()){
                                    Transaction transaction = (Transaction) document.toObject(Transaction.class);
                                    transactions.add(transaction);
                                    System.out.println(transaction);
                                }
                                showTransactions();
                            }
                        }
                    });
        }else {
            String id = et_specID.getText().toString();
            db.collection("Transaction").document(id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                Transaction transaction = (Transaction) task.getResult().toObject(Transaction.class);
                                System.out.println(transaction);
                                transactions.add(transaction);
                                showTransactions();
                            }

                        }
                    });
        }
    }

    public int selectedRadioButton(){
        return rg_OrderBy.getCheckedRadioButtonId();
    }

    public Date stringToDate(String d){
        String[] date_tokens = d.split("/");
        int month = Integer.parseInt(date_tokens[0]) - 1;
        int day = Integer.parseInt(date_tokens[1]);
        int year = Integer.parseInt(date_tokens[2]);

        Calendar cal = Calendar.getInstance();
        try {
            cal.set(year,month,day);
            System.out.println("date set");
        }catch (Exception e){
            System.out.println("you've entered an invalid date please try again");
        }
        Date transaction_date = cal.getTime();
        return transaction_date;
    }

    public void showTransactions(){
        System.out.println("holder reached");
        MyAdapter1 adapter = new MyAdapter1(GetTransaction.this,transactions);
        RecyclerView rv = findViewById(R.id.RV_get_show);
        rv.setLayoutManager(new LinearLayoutManager(GetTransaction.this));
        rv.setAdapter(adapter);
    }

    private Bitmap bytesToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }

}