package com.example.appmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CstmShopListAdapter extends ArrayAdapter<String> {
    Context context;
    int resource;
    List<String> shopList;

    public CstmShopListAdapter(Context context, int resource, List<String> shopList) {
        super(context, resource, shopList);
        this.context = context;
        this.resource = resource;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.shop_item, null);
        TextView textViewShop = view.findViewById(R.id.item_title);
        textViewShop.setText(shopList.get(position));

        return view;
    }
}
