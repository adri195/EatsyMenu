package com.example.appmenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<GestorDias> listaDias = new ArrayList<>();
    private List<GestorDias> listaOldDias = new ArrayList<>();
    private List<String> listaListas = new ArrayList<>();
    private List<Menu> listaMenus = new ArrayList<>();
    private final GestorDias gestordias = new GestorDias();
    private ListView listView;
    private CardView cardView;
    private BottomNavigationView botNav;
    private FloatingActionButton addButton;
    private String uid;
    private Context cont;
    private boolean checkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        getSupportActionBar().hide();

        cont = this;
        checkView = false;
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Se comprueba el tema de la app
        boolean isNight = myPreferences.getBoolean("NIGHT", false);
        if (isNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        listaDias = gestordias.array();

        botNav = findViewById(R.id.botNavBar);
        botNavBar();
        botNav.getMenu().getItem(1).setChecked(true);

        addButton = findViewById(R.id.addButton);
        cardView = findViewById(R.id.CardView);

        updateMainLayout();
    }

    public void botNavBar(){
        botNav.setOnNavigationItemSelectedListener(navListener);
    }

    //Acciones de la barra de navegación
    public BottomNavigationView.OnNavigationItemSelectedListener navListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.History:
                        setView(R.layout.activity_main, 0);
                        checkView = false;
                        getSupportActionBar().hide();
                        listView = findViewById(R.id.lstvw);
                        cardView = findViewById(R.id.CardView);
                        listView.setVisibility(View.VISIBLE);
                        cardView.setVisibility(View.VISIBLE);
                        listaOldDias = gestordias.arrayOld();
                        updateOldList();
                        if (addButton != null){
                            addButton.setVisibility(View.INVISIBLE);
                        }
                        break;

                    case R.id.Home:
                        checkView = false;
                        setView(R.layout.main_layout, 1);
                        cardView = findViewById(R.id.CardView);
                        cardView.setVisibility(View.VISIBLE);
                        updateMainLayout();
                        break;

                    case R.id.List:
                        checkView = false;
                        cardView.setVisibility(View.INVISIBLE);
                        setView(R.layout.activity_main, 2);
                        listView = findViewById(R.id.lstvw);
                        updateLists();
                        shopListControler();
                        break;

                    case R.id.User:
                        checkView = false;
                        Intent intent = new Intent(cont, ProfileSettings.class);
                        startActivity(intent);
                        finish();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                return true;
            }
        };

    //Se piden al servidor los datos de los menús próximos y se devuelven en una lista de objetos menu
    private void readData(MainActivity.FirebaseReadCB cb){
        List<Menu> menus = new ArrayList<>();
        for(int i = 0; i < listaDias.size(); i++){
            DocumentReference docRef = db.collection("Users").document(uid).collection("Menus").document(listaDias.get(i).getFecha());
            int finalI = i;
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String comida = (String) document.getData().get("comida");
                            String cena = (String) document.getData().get("cena");
                            Menu menu = new Menu(listaDias.get(finalI).getFecha(), listaDias.get(finalI).getNumero(),listaDias.get(finalI).getDia(),comida,cena);
                            menus.add(menu);
                        } else {
                            //Si alguno de los días no está creado, se crea vacío
                            Map<String, Object> campos = new HashMap<>();
                            campos.put("numero", listaDias.get(finalI).getNumero());
                            campos.put("nombre", listaDias.get(finalI).getDia());
                            campos.put("comida", "");
                            campos.put("cena", "");
                            db.collection("Users").document(uid).collection("Menus").document(listaDias.get(finalI).getFecha()).set(campos);

                            Menu menu = new Menu(listaDias.get(finalI).getFecha(), listaDias.get(finalI).getNumero(), listaDias.get(finalI).getDia(), "", "");
                            menus.add(menu);
                        }
                        if (menus.size() == 7){
                            cb.onCallback(menus);
                        }
                    }
                }
            });
        }
    }

    private interface FirebaseReadCB{
        void onCallback(List<Menu> menus);
    }

    public void updateUI(){
        //Se actualiza la UI de los menús proximos con los datos actualizados del servidor al recibir la
        //respuesta de readData()
        readData(new FirebaseReadCB() {
            @Override
            public void onCallback(List<Menu> menus) {
                listaMenus = menus;
                Collections.sort(listaMenus);

                listView = findViewById(R.id.lstvw);
                CstmListAdapter adapter = new CstmListAdapter(MainActivity.this, R.layout.dia, listaMenus);
                cardView = findViewById(R.id.CardView);
                cardView.setVisibility(View.INVISIBLE);
                listView.setAdapter(adapter);
            }
        });
    }

    //Se piden al servidor los datos de los menús pasados y se devuelven en una lista de objetos menu
    private void readOldData(MainActivity.FirebaseReadOldCB cb){
        List<Menu> menus = new ArrayList<>();
        for(int i = 0; i < listaOldDias.size(); i++){
            DocumentReference docRef = db.collection("Users").document(uid).collection("Menus").document(listaOldDias.get(i).getFecha());
            int finalI = i;
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String comida = (String) document.getData().get("comida");
                            String cena = (String) document.getData().get("cena");
                            Menu menu = new Menu(listaOldDias.get(finalI).getFecha(), listaOldDias.get(finalI).getNumero(),listaOldDias.get(finalI).getDia(),comida,cena);
                            menus.add(menu);
                            if (menus.size() == 6){
                                cb.onCallback(menus);
                            }
                        } else {
                            //Si alguno de los días no está creado, se crea vacío
                            Map<String, Object> campos = new HashMap<>();
                            campos.put("numero", listaOldDias.get(finalI).getNumero());
                            campos.put("nombre", listaOldDias.get(finalI).getDia());
                            campos.put("comida", "");
                            campos.put("cena", "");
                            db.collection("Users").document(uid).collection("Menus").document(listaOldDias.get(finalI).getFecha()).set(campos);

                            Menu menu = new Menu(listaOldDias.get(finalI).getFecha(), listaOldDias.get(finalI).getNumero(), listaOldDias.get(finalI).getDia(),"","");
                            menus.add(menu);
                            if (menus.size() == 7){
                                cb.onCallback(menus);
                            }
                        }
                    }
                }
            });
        }
    }

    private interface FirebaseReadOldCB{
        void onCallback(List<Menu> menus);
    }


    public void updateOldList(){
        //Se actualiza la UI de los menús antiguos con los datos actualizados del servidor al recibir la
        //respuesta de readOldData()
        readOldData(new FirebaseReadOldCB() {
            @Override
            public void onCallback(List<Menu> menus) {
                listaMenus = menus;
                Collections.sort(listaMenus);
                Collections.reverse(listaMenus);

                listView = findViewById(R.id.lstvw);
                CstmListAdapter adapter = new CstmListAdapter(MainActivity.this, R.layout.dia, listaMenus);
                cardView = findViewById(R.id.CardView);
                cardView.setVisibility(View.INVISIBLE);
                listView.setAdapter(adapter);
            }
        });
    }

    //Al hacer click en algún item menú
    public void editDay(View view){
        LayoutInflater inflater = getLayoutInflater();
        TextView txtcom = view.findViewById(R.id.txtcom);
        TextView txtcen = view.findViewById(R.id.txtcen);
        TextView num = view.findViewById(R.id.num);
        View alertLayout = inflater.inflate(R.layout.editar_menu, null);
        TextInputEditText comida = alertLayout.findViewById(R.id.tiet_comida);
        TextInputEditText cena = alertLayout.findViewById(R.id.tiet_cena);

        comida.setText(txtcom.getText().toString());
        cena.setText(txtcen.getText().toString());
        String oldtxtcom = comida.getText().toString();
        String oldtxtcen = cena.getText().toString();

        //Se abre un cuadro de diálogo con el contenido de los campos comida y cena
        //con el valor inicial
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        alert.setPositiveButton("Editar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Al hacer click en Añadir se comprueba si se ha realizado algún cambio
                if (cena.getText().toString().equalsIgnoreCase(oldtxtcen) && comida.getText().toString().equalsIgnoreCase(oldtxtcom)){
                    Toast.makeText(getBaseContext(), "No se ha introducido ningún cambio", Toast.LENGTH_SHORT).show();
                }else{
                    //Si se ha realizado un cambio se manda la actualización al servidor y se actualiza la UI correspondiente
                    int editedDay = 0;
                    for(int i = 0; i < listaMenus.size(); i++){
                        if(listaMenus.get(i).getNum().equalsIgnoreCase(num.getText().toString())){
                            String fecha = listaMenus.get(i).getFecha();
                            String num =  listaMenus.get(i).getNum();
                            String nom = listaMenus.get(i).getDia();

                            editedDay = Integer.parseInt(num);

                            Map<String, Object> campos = new HashMap<>();
                            campos.put("nombre", nom);
                            campos.put("numero", num);
                            campos.put("comida", comida.getText().toString());
                            campos.put("cena", cena.getText().toString());

                            db.collection("Users").document(uid).collection("Menus").document(fecha).set(campos);
                        }
                    }

                    Calendar cal = Calendar.getInstance();
                    int today = cal.get(Calendar.DAY_OF_MONTH);
                    if (today < editedDay) {
                        if (today + 7 < editedDay){
                            updateOldList();
                        }else{
                            updateUI();
                        }
                    }
                    if (today > editedDay) {
                        if (today > editedDay+7){
                            updateUI();
                        }else{
                            updateOldList();
                        }
                    }
                    if (today == editedDay){
                        updateUI();
                    }
                    Toast.makeText(getBaseContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    //Al hacer click en el botón flotante de añadir
    public void addList(View view){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_list, null);
        TextInputEditText newList = alertLayout.findViewById(R.id.tiet_lista);

        //Se abre un cuadro de diálogo para pedir el nombre de la lista
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Introduce el nombre de la lista");
        alert.setView(alertLayout);
        alert.setCancelable(true);
        alert.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Al hacer click en el botón de Añadir se comprueba si se ha introducido un valor
                String list = newList.getText().toString();
                if (!list.equalsIgnoreCase("")){
                    //Se recoge el valor introducido por el usuario y se manda al servidor
                    Map<String, Object> rawList = new HashMap<>();
                    db.collection("Users").document(uid).collection("Listas").document(list).set(rawList);
                    updateLists();
                }else{
                    Toast.makeText(getBaseContext(), "Introduce el nombre de la lista", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    //Se recogen del servidor los datos de las listas existentes
    private void readLists(FirebaseReadListCB cb){
        listaListas = new ArrayList<>();
        db.collection("Users").document(uid).collection("Listas").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        listaListas.add(document.getId());
                    }
                    cb.onCallback(listaListas);
                }
            }
        });
    }

    private interface FirebaseReadListCB {
        void onCallback(List<String> listas);
    }

    //Se actualiza la UI de las listas con los datos actualizados del servidor al recibir la
    //respuesta de readLists()
    public void updateLists(){
        readLists(new FirebaseReadListCB(){
            @Override
            public void onCallback(List<String> listas) {
                listView = findViewById(R.id.lstvw);
                CstmShopListAdapter adapter = new CstmShopListAdapter(MainActivity.this, R.layout.shop_item, listas);
                cardView = findViewById(R.id.CardView);
                cardView.setVisibility(View.INVISIBLE);
                listView.setAdapter(adapter);
                addButton = findViewById(R.id.addButton);
                addButton.setVisibility(View.VISIBLE);
            }
        });
    }


    public void shopListControler() {
        //Al hacer click en algún item de la lista se pasa a la ListActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
                TextView shopView = view.findViewById(R.id.item_title);
                String shop = shopView.getText().toString();
                Intent intent = new Intent(cont, ListActivity.class);
                intent.putExtra("shopName", shop);
                startActivity(intent);
            }
        });

        //Al hacer click largo en algún item de la lista se muestra un cuadro de dialogo para
        //eliminarla
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                TextView shopView = view.findViewById(R.id.item_title);
                String shop = shopView.getText().toString();
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                alert.setTitle("¿Quieres eliminar la lista?");
                alert.setCancelable(true);
                alert.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("Users").document(uid).collection("Listas").document(shop).delete();
                        updateLists();
                    }
                });
                alert.setNegativeButton("No", null);
                android.app.AlertDialog dialog = alert.create();
                dialog.show();
                return true;
            }
        });
    }

    //Se cambia el layout existente por el que se pasa y se cambia el foco del NavBar
    public void setView(Integer layout, Integer index){
        setContentView(layout);
        botNav = findViewById(R.id.botNavBar);
        botNavBar();
        botNav.getMenu().getItem(index).setChecked(true);
    }

    //Se actualiza el layout principal con los datos actualizados del servidor al responder readData()
    public void updateMainLayout(){
        readData(new FirebaseReadCB() {
            @Override
            public void onCallback(List<Menu> menus) {
                listaMenus = menus;
                Collections.sort(listaMenus);

                configView(menus, 0);
                configView(menus, 1);

                cardView.setVisibility(View.INVISIBLE);
            }
        });
    }

    //Método de apoyo para la configuración del layout principal
    public void configView(List<Menu> menus, Integer pos){
        TextView textViewNum;
        TextView textViewDia;
        TextView textViewComida;
        TextView textViewCena;
        if(pos == 0){
            textViewNum = findViewById(R.id.num);
            textViewDia = findViewById(R.id.dia);
            textViewComida = findViewById(R.id.txtcom);
            textViewCena = findViewById(R.id.txtcen);
        }else{
            textViewNum = findViewById(R.id.num2);
            textViewDia = findViewById(R.id.dia2);
            textViewComida = findViewById(R.id.txtcom2);
            textViewCena = findViewById(R.id.txtcen2);
        }

        Menu menu = menus.get(pos);
        textViewNum.setText(menu.getNum());
        textViewDia.setText(menu.getDia());
        textViewComida.setText(menu.getComida());
        textViewCena.setText(menu.getCena());
    }

    //Al hacer click en el botón Menú semanal se modifica el layout y se activa el checkView para
    //utilizarlo en onBackPressed
    public void fullMenu(View view){
        setView(R.layout.activity_main, 1);
        getSupportActionBar().hide();
        listView = findViewById(R.id.lstvw);
        cardView = findViewById(R.id.CardView);
        listView.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.VISIBLE);
        listaDias = gestordias.array();
        updateUI();
        if (addButton != null){
            addButton.setVisibility(View.INVISIBLE);
        }
        checkView = true;
    }

    @Override
    public void onBackPressed() {
        //Se checkea el estado del checkView y se finaliza la app o se cambia de layout
        if(checkView){
            checkView = false;
            setView(R.layout.main_layout, 1);
            cardView = findViewById(R.id.CardView);
            cardView.setVisibility(View.VISIBLE);
            updateMainLayout();
        }else{
            finish();
        }
    }

    public Context getContext(){
        return this;
    }
}