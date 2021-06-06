package com.example.appmenu;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CstmProductListAdapter extends ArrayAdapter<String> {
    Context context;
    int resource;
    List<String> productsList;

    public CstmProductListAdapter(Context context, int resource, List<String> productsList) {
        super(context, resource, productsList);
        this.context = context;
        this.resource = resource;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.product_item, null);
        CheckBox checkBoxProduct = view.findViewById(R.id.checkBox);

        String rawProduct = productsList.get(position);
        String[] splitProduct = rawProduct.split(":#:");

        //Se comprueba si el producto está marcado o no
        if (splitProduct.length > 1 && splitProduct[1].equalsIgnoreCase("uncheck")) {
            checkBoxProduct.setText(splitProduct[0]);
        }else{
            checkBoxProduct.setChecked(true);
            checkBoxProduct.setText(splitProduct[0]);
            checkBoxProduct.setPaintFlags(checkBoxProduct.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        return view;
    }
}