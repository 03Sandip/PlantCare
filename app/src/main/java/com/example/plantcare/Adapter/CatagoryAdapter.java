package com.example.plantcare.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantcare.Activity.Croprecomandation;
import com.example.plantcare.Activity.Soilfertilizer;
import com.example.plantcare.Domain.CategoryDomain;
import com.example.plantcare.R;

import java.util.ArrayList;

public class CatagoryAdapter extends RecyclerView.Adapter<CatagoryAdapter.ViewHolder> {
    private ArrayList<CategoryDomain> items;
    private Context context;

    public CatagoryAdapter(ArrayList<CategoryDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_catagory, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDomain category = items.get(position);
        holder.titleTxt.setText(category.getTitle());

        // Load image
        int drawableResourceId = holder.itemView.getResources()
                .getIdentifier(category.getImgPath(), "drawable", holder.itemView.getContext().getPackageName());
        Glide.with(context).load(drawableResourceId).into(holder.pic);

        // Set click listener to redirect based on the category title
        holder.itemView.setOnClickListener(v -> {
            if ("Fertilizer".equals(category.getTitle())) {
                Intent intent = new Intent(context, Croprecomandation.class);
                context.startActivity(intent);

            } else if ("Newspaper".equals(category.getTitle())) {
                Intent intent = new Intent(context, Soilfertilizer.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.title);
            pic = itemView.findViewById(R.id.img);
        }
    }
}
