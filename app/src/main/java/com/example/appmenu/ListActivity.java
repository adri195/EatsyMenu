package com.example.appmenu;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> listaCheckProductos = new ArrayList<>();
    private List<String> listaUncheckProductos = new ArrayList<>();
    private Object[] array;
    private ListView productsList = null;
    private String shopName;
    private FirebaseUser user;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_shoplist);
        getSupportActionBar().hide();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        //Se capta el nombre de la lista y se guarde en una variable
        shopName = getIntent().getExtras().getString("shopName");
        showProducts(shopName);
    }

    //Se llama a update() pasándole el nombre de la lista
    public void showProducts(String shop){
        readProducts(new ListActivity.FirebaseReadProductsCB() {
            @Override
            public void onCallback(List<String> checkProducts, List<String> uncheckProducts) {
                updateUI(checkProducts, uncheckProducts);
            }
        }, shop);
    }

    //Se actualiza la interfaz con los datos actualizados del servidor
    public void updateUI(List<String> checkProducts, List<String> uncheckProducts){
        TextView name = findViewById(R.id.ShopName);
        name.setText(shopName);
        productsList = findViewById(R.id.ShopList);

        //Se pasan los datos al adapter y se actuaiza la vista de la lista
        CstmProductListAdapter uncheckAdapter = new CstmProductListAdapter(ListActivity.this, R.layout.product_item, uncheckProducts);
        productsList.setAdapter(uncheckAdapter);
        Utility.setListViewHeightBasedOnChildren(productsList);

        //Si la lista inferior no tiene elementos, se oculta el separador
        View divider = findViewById(R.id.divider2);
        if (checkProducts.size() == 0){
            divider.setVisibility(View.INVISIBLE);
        }else{
            divider.setVisibility(View.VISIBLE);
        }
        productsList = findViewById(R.id.ShopCheckedList);

        //Se pasan los datos al adapter y se actuaiza la vista de la lista
        CstmProductListAdapter checkAdapter = new CstmProductListAdapter(ListActivity.this, R.layout.product_item, checkProducts);
        productsList.setAdapter(checkAdapter);
        Utility.setListViewHeightBasedOnChildren(productsList);

    }

    //Al hacer click en algún item
    public void selectProduct(View view){
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        Map<String, Object> addProduct = new HashMap<>();

        //Se comprueba el estado del checkbox, se cambia, se informa al servidor y se actualiza la UI
        if (checkBox.isChecked()){
            checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            String mainText = checkBox.getText().toString();
            String[] splitText = mainText.split(":#:");
            addProduct.put(shopName + "-" + splitText[0], splitText[0] + ":#:check");
            db.collection("Users").document(uid).collection("Listas").document(shopName).update(addProduct);
            showProducts(shopName);
        }else{
            checkBox.setPaintFlags( checkBox.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            String mainText = checkBox.getText().toString();
            String[] splitText = mainText.split(":#:");
            addProduct.put(shopName + "-" + splitText[0], splitText[0] + ":#:uncheck");
            db.collection("Users").document(uid).collection("Listas").document(shopName).update(addProduct);
            showProducts(shopName);
        }
    }

    //Se piden los productos al servidor y se cargan ambas listas
    public void readProducts(ListActivity.FirebaseReadProductsCB cb, String list){
        db.collection("Users").document(uid).collection("Listas").document(list).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    listaCheckProductos = new ArrayList<>();
                    listaUncheckProductos = new ArrayList<>();
                    DocumentSnapshot document = task.getResult();
                    array = document.getData().values().toArray();
                    for (int i=0; i<array.length; i++){
                        if (array[i] != null){
                            String splitProduct[] = array[i].toString().split(":#:");
                            if(splitProduct[1].equalsIgnoreCase("check")){
                                listaCheckProductos.add(array[i].toString());
                            } else {
                                listaUncheckProductos.add(array[i].toString());
                            }
                        }
                    }
                    cb.onCallback(listaCheckProductos, listaUncheckProductos);

                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private interface FirebaseReadProductsCB {
        void onCallback(List<String> checkList, List<String> uncheckList);
    }

    //Al hacer click en el botón de añadir
    public void addProduct(View view){
        View parent = (View) view.getParent();
        View parent2 = (View) parent.getParent();
        View parent3 = (View) parent2.getParent();
        TextView shopNameView = parent3.findViewById(R.id.ShopName);
        String shopName = shopNameView.getText().toString();
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_product, null);

        //Se pide al usuario que introduzca el producto que quiere añadir
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
        alert.setTitle("Introduce el producto");
        alert.setView(alertLayout);
        alert.setCancelable(true);
        alert.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Al hacer click en añadir se pasa el producto al servidor y se le asigna
                // como estado por defecto "sin marcar"
                TextInputEditText productNameView = alertLayout.findViewById(R.id.tiet_subItem);
                String productName = productNameView.getText().toString();
                if (productName != null && !productName.equalsIgnoreCase("")){
                    Map<String, Object> addProduct = new HashMap<>();
                    addProduct.put(shopName + "-" + productName, productName + ":#:uncheck");
                    db.collection("Users").document(uid).collection("Listas").document(shopName).update(addProduct);
                    showProducts(shopName);
                }else{
                    Toast.makeText(getBaseContext(), "Introduce un producto", Toast.LENGTH_SHORT).show();
                }
            }
        });
        android.app.AlertDialog dialog = alert.create();
        dialog.show();
    }

    //Al hacer click en el botón eliminar de cada item
    public void deleteProduct(View view){
        View parent = (View) view.getParent();
        CheckBox productNameView = parent.findViewById(R.id.checkBox);
        HashMap<String, Object> deleteMap = new HashMap<>();
        String[] key = {null};

        //Se optiene el nombre del item, de pasa al hashMap con el nombre de la lista (Formato de la BBDD)
        //y se da la orden de eliminar al servidor
        key[0] = shopName + "-" + productNameView.getText().toString();
        deleteMap.put(key[0], FieldValue.delete());
        db.collection("Users").document(uid).collection("Listas").document(shopName).update(deleteMap);
        showProducts(shopName);
    }

}