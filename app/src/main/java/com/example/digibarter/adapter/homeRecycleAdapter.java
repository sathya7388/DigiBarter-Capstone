package com.example.digibarter.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.digibarter.R;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.model.Product;
import com.example.digibarter.productDetail;

import java.util.ArrayList;


public class homeRecycleAdapter extends RecyclerView.Adapter<homeRecycleAdapter.homeHolder> {

    ArrayList<Product> products;
    Context context;
    public homeRecycleAdapter(Context ct, ArrayList<Product> p){
        context = ct;
        this.products = p;
    }
    @NonNull
    @Override
    public homeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.home_product_cell,parent,false);
        return new homeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull homeHolder holder, final int position) {
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
                Intent i = new Intent(context, productDetail.class);
                i.putExtra("pid",products.get(position).id);
                i.putExtra("userId", SharedPreferenceHelper.getSharedPreferenceInt(context, "id", -1));
                i.putExtra("userType",products.get(position).usertype);
                context.startActivity(i);
            }
        });
    }



    @Override
    public int getItemCount() {
        return products.size();
    }


    public class homeHolder extends RecyclerView.ViewHolder {
        ImageView image;
        public homeHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
        }
    }
    public void clear() {
        int size = products.size();
        products.clear();
        notifyItemRangeRemoved(0, size);
    }
}
