package com.example.appmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class CstmListAdapter extends ArrayAdapter<Menu> {
    Context context;
    int resource;
    List<Menu> listaMenu;

    public CstmListAdapter(Context context, int resource, List<Menu> listaMenu) {
        super(context, resource, listaMenu);
        this.context = context;
        this.resource = resource;
        this.listaMenu = listaMenu;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dia, null);
        TextView textViewNum = view.findViewById(R.id.num);
        TextView textViewDia = view.findViewById(R.id.dia);
        TextView textViewComida = view.findViewById(R.id.txtcom);
        TextView textViewCena = view.findViewById(R.id.txtcen);

        Menu menu = listaMenu.get(position);
        textViewNum.setText(menu.getNum());
        textViewDia.setText(menu.getDia());
        textViewComida.setText(menu.getComida());
        textViewCena.setText(menu.getCena());

        return view;
    }
}
