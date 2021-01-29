package com.example.digibarter.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.digibarter.R;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;
import com.example.digibarter.productDetail;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StorePageAdapter extends RecyclerView.Adapter<StorePageAdapter.storeHolder>{

    ArrayList<Product> products;
    Context context;

    public StorePageAdapter( Context context,ArrayList<Product> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public StorePageAdapter.storeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.store_page,parent,false);
        return new storeHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull StorePageAdapter.storeHolder holder, final int position) {
        String imageURL = "https://enhancedperformanceinc.com/wp-content/uploads/2017/10/product-dummy.png";
        if(products.get(position).images.length != 0)
        {
            imageURL = products.get(position).images[0];
        }
        Glide.with(context).load(imageURL).into(holder.image);
        holder.image.setTag(imageURL);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context,productDetail.class);
                i.putExtra("pid",products.get(position).id);
                context.startActivity(i);
            }
        });
        holder.productName.setText(products.get(position).title);
        holder.buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context,productDetail.class);
                i.putExtra("pid",products.get(position).id);
                context.startActivity(i);
            }
        });
    }


    @Override
    public int getItemCount() {
        return products.size();
    }

    public class storeHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView productName;
        Button buyBtn;
        public storeHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageSlide);
            productName = itemView.findViewById(R.id.productName);
            buyBtn = itemView.findViewById(R.id.buyBtn);
        }
    }

}
