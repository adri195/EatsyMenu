package com.example.appmenu;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passEditText;
    private EditText nameEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Al hacer click en el botón "Registrarse" del layout de inicio de sesión
    public void signInTosignUp(View view){
        setContentView(R.layout.activity_signup);
    }

    //Al hacer click en el botón "Registrarse" del layout de registro
    public void signUp(View view){
        View parent = getParent(view);
        Context cont = this;
        nameEditText = parent.findViewById(R.id.editTextName);
        emailEditText = parent.findViewById(R.id.editTextEmail);
        passEditText = parent.findViewById(R.id.editTextPassword);
        //Se comprueba que se han completado todos los campos
        if (emailEditText.getText() != null && passEditText.getText() != null) {
            if (!emailEditText.getText().toString().isEmpty() && !passEditText.getText().toString().isEmpty()) {
                //Se manda la orden de registro al servidor
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(), passEditText.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                //Cuando se completa se informa al usuario, se pasa a la activity principal y se guardan los datos de inicio de sesión
                                if(task.isSuccessful()){
                                    Toast.makeText(cont, "El usuario se ha registrado correctamente", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(cont, MainActivity.class);
                                    startActivity(intent);

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(nameEditText.getText().toString()).build();
                                    user.updateProfile(profileUpdates);

                                    finish();
                                }else{
                                    //Si devuelve algún error se informa al usuario para que revise los campos introducidos
                                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                                    alert.setTitle("Error").setMessage("Introduce un email correcto o una contraseña más segura").setPositiveButton("Aceptar", null).create().show();

                                }
                            }
                        });
            } else {
                //Si alguno de los campos no se ha completado se informa al usuario
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                alert.setTitle("Error").setMessage("Completa todos los campos para continuar").setPositiveButton("Aceptar", null).create().show();
            }
        }
    }

    //Al hacer click en el botón "Iniciar sesión" del layout de inicio de sesión
    public void signIn(View view){
        View parent = getParent(view);
        Context cont = this;
        emailEditText = parent.findViewById(R.id.editTextEmail);
        passEditText = parent.findViewById(R.id.editTextPassword);
        //Se comprueba que se han completado todos los campos
        if (emailEditText.getText() != null && passEditText.getText() != null) {
            if (!emailEditText.getText().toString().isEmpty() && !passEditText.getText().toString().isEmpty()) {
                //Se manda la orden de inicio de sesión al servidor
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(), passEditText.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                //Cuando se completa se informa al usuario, se pasa a la activity principal y se guardan los datos de inicio de sesión
                                if(task.isSuccessful()){
                                    Toast.makeText(cont, "Se ha iniciado sesión correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(cont, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    //Si devuelve algún error se informa al usuario para que revise los campos introducidos
                                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                                    alert.setTitle("Error").setMessage("Email o contraseña incorrectos").setPositiveButton("Aceptar", null).create().show();

                                }
                            }
                        });

            } else {
                //Si alguno de los campos no se ha completado se informa al usuario
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                alert.setTitle("Error").setMessage("Completa todos los campos para continuar").setPositiveButton("Aceptar", null).create().show();
            }
        }
    }

    public View getParent(View view){
        View firstParent = (View) view.getParent();
        View secondParent = (View) firstParent.getParent();
        return secondParent;
    }

    public void onBackPressed() {
        if(findViewById(R.id.signUpInputs) != null){
            setContentView(R.layout.activity_auth);
        }else{
            finish();
        }
    }

}