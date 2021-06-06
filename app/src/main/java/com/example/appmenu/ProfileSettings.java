package com.example.appmenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileSettings extends AppCompatActivity {

    private SharedPreferences.Editor myEditor;
    private SharedPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        getSupportActionBar().hide();
        myPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        TextInputEditText nameInput = findViewById(R.id.nameText);
        TextInputEditText emailInput = findViewById(R.id.emailText);

        nameInput.setText(user.getDisplayName());
        emailInput.setText(user.getEmail());

        checkSwitch();

    }

    public void NigthMode(View view){
        Switch switch1 = findViewById(R.id.switch1);
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        //Se comprueba qué tema está activo y se cambia
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                switch1.setChecked(false);
                myEditor = myPreferences.edit();
                myEditor.putBoolean("NIGHT", false);
                myEditor.commit();
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                switch1.setChecked(true);
                myEditor = myPreferences.edit();
                myEditor.putBoolean("NIGHT", true);
                myEditor.commit();
                break;
        }
    }

    public void checkSwitch(){
        Switch switch1 = findViewById(R.id.switch1);
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        //Se comprueba el estado del switch y se cambia
        switch (nightModeFlags) {
            case android.content.res.Configuration.UI_MODE_NIGHT_YES:
                switch1.setChecked(true);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                switch1.setChecked(false);
                break;
        }
    }

    //Al hacer click en el botón "Cerrar sesión" se inicia la AuthActivity
    public void signOut(View view){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    //Al hacer click en el botón "Cambiar la contraseña"
    public void resetPass(View view){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //Se manda la orden al servidor para que mande el correo y se informa al usuario para que lo revise
        mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail());
        Toast.makeText(this, "Se ha enviado un email a la dirección de correo " + mAuth.getCurrentUser().getEmail() + " desde el que podrás cambiar la contraseña", Toast.LENGTH_SHORT).show();

        //Se cierra la sesión y se inicia la AuthActivity
        mAuth.signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}