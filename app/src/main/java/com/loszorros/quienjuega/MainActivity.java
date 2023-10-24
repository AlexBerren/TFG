package com.loszorros.quienjuega;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUFirstFragment;
import com.loszorros.quienjuega.MenuFragments.MENUFourthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUSecondFragment;
import com.loszorros.quienjuega.MenuFragments.MENUThirdFragment;import android.Manifest;

public class MainActivity extends AppCompatActivity {

    //Se crean los cinco fragmentos
    com.loszorros.quienjuega.MenuFragments.MENUFirstFragment MENUFirstFragment = new MENUFirstFragment();
    com.loszorros.quienjuega.MenuFragments.MENUSecondFragment MENUSecondFragment = new MENUSecondFragment();
    com.loszorros.quienjuega.MenuFragments.MENUThirdFragment MENUThirdFragment = new MENUThirdFragment();
    com.loszorros.quienjuega.MenuFragments.MENUFourthFragment MENUFourthFragment = new MENUFourthFragment();
    com.loszorros.quienjuega.MenuFragments.MENUFifthFragment MENUFifthFragment = new MENUFifthFragment();
    private static final int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLocationPermissions();
            setContentView(R.layout.activity_main);
            //para ocultar la barra superior de "quien juega"
             //getSupportActionBar().hide();
            loadFragment(MENUFirstFragment);
            //Se asocia "navigation" con el menu y se le añade el listener
            BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        }
    private void requestLocationPermissions() {
        // Verificar si los permisos de ubicación ya están otorgados
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos de ubicación
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            // Los permisos de ubicación ya están otorgados, realizar acciones necesarias
            // ...
        }
    }





    //Metodo para que al pulsar cada boton del menu se abra el fragmento seleccionado
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId()) {
                case R.id.firstFragment:
                    loadFragment(MENUFirstFragment);
                    return true;
                case R.id.secondFragment:
                    loadFragment(MENUSecondFragment);
                    return true;
                case R.id.thirdFragment:
                    loadFragment(MENUThirdFragment);
                    return true;
                case R.id.fourthFragment:
                    loadFragment(MENUFourthFragment);
                    return true;
                case R.id.fifthFragment:
                    loadFragment(MENUFifthFragment);
                    return true;

            }
            return false;
        }
    };

    //Metodo para cargar un fragmento
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
