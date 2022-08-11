package com.example.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter1 extends RecyclerView.Adapter<MyAdapter1.MyViewHolder> {

    Context context;
    ArrayList<Transaction> transactions;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        System.out.println("on bind reached");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        holder.id.setText(  "ID: " + String.format("%d", transactions.get(position).getId()));
        Date date = transactions.get(position).getDate();
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String strDate = dateFormat.format(date);
        holder.date.setText("Date: " +strDate);
        holder.type.setText("Type: " +transactions.get(position).getType());
        holder.amount.setText("Amount: " +String.format("%,.2f", transactions.get(position).getAmount()));
        holder.description.setText("Description: " +transactions.get(position).getDescription());
        try {
            storageReference.child(Integer.toString(transactions.get(position).getId()))
                    .getBytes(1024*1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            holder.imageView.setImageBitmap(bytesToBitmap(bytes));
                            System.out.println("image added");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("image wasnt added ");
                        }
                    });
        }catch (Exception e){
            System.out.println("image could not be added");
        }
    }

    private Bitmap bytesToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory
                .decodeByteArray(b, 0, b.length);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public MyAdapter1(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView id, date, type, amount, description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.CardImage);
            this.id = itemView.findViewById(R.id.TV_ID);
            this.amount = itemView.findViewById(R.id.TV_Amount);
            this.date = itemView.findViewById(R.id.TV_Date);
            this.description = itemView.findViewById(R.id.TV_Description);
            this.type = itemView.findViewById(R.id.TV_Type);
        }
    }
}
