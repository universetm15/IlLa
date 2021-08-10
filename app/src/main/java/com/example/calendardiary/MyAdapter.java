package com.example.calendardiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    ArrayList<ItemData> itemData=new ArrayList<ItemData>();

    public void addItem(ItemData data){
        itemData.add(data);
    }
    public void setItemData(ArrayList<ItemData> itemData){
        this.itemData=itemData;
    }
    public ItemData getItem(int position){
        return itemData.get(position);
    }
    public void setItems(int position, ItemData data){
        itemData.set(position,data);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View itemView=inflater.inflate(R.layout.listitem,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemData data=itemData.get(position);
        holder.setItem(data);
    }

    @Override
    public int getItemCount() {
        return itemData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTxtDate, itemTxtList;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTxtDate=itemView.findViewById(R.id.itemTxtDate);
            itemTxtList=itemView.findViewById(R.id.itemTxtList);
        }
        public void setItem(ItemData data){
            itemTxtDate.setText(data.getItemTxtDate());
            itemTxtList.setText(data.getItemTxtList());
        }
    }
}


