package com.loszorros.quienjuega.RegistroLoginActivitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.loszorros.quienjuega.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class REGISTROSignIn2 extends AppCompatActivity {

    Button botonPasarPagina;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText nombre;
    EditText apellidos;
    EditText username;
    Calendar selectedCalendar;
    CalendarView fechaNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in2);
        botonPasarPagina = findViewById(R.id.botonSiguiente);
        nombre= findViewById(R.id.TxtName);
        apellidos= findViewById(R.id.TxtApellido);
        username= findViewById(R.id.TxtUsername);
        fechaNacimiento= findViewById(R.id.TxtFechaNacimiento);
        //getSupportActionBar().hide();

        fechaNacimiento.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                selectedCalendar.set(Calendar.MILLISECOND, 0);
            }
        });


        InputFilter filtroNombre = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isLetter(character) || character == ' ') {
                        stringBuilder.append(character);
                    }
                }

                return stringBuilder.toString();
            }
        };


        nombre.setFilters(new InputFilter[] { filtroNombre });
        apellidos.setFilters(new InputFilter[] { filtroNombre });

        InputFilter filtroUsername = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isLetterOrDigit(character) || character == '_' || character == '.') {
                        stringBuilder.append(character);
                    }
                }

                return stringBuilder.toString();
            }
        };

        username.setFilters(new InputFilter[] { filtroUsername });

    }

    //Te lleva a la pagina 3 del registro
    public void Sign3(View v){
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefe.getString("email",null);
        HashMap<String, String> hasMap = new HashMap<>();

        // Obtener los valores de los campos de texto
        String nombreText = nombre.getText().toString().trim();
        String apellidosText = apellidos.getText().toString().trim();
        String usernameText = username.getText().toString().trim();

        // Validar que no haya campos vacíos y que se haya seleccionado una fecha
        if (nombreText.isEmpty() || apellidosText.isEmpty() || usernameText.isEmpty() || selectedCalendar == null) {
            // Mostrar una alerta de error
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setMessage("Por favor, completa todos los campos y selecciona una fecha.")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return; // Detener la ejecución del método si hay campos vacíos o falta fecha seleccionada
        }

        hasMap.put("name", nombreText);
        hasMap.put("surName", apellidosText);
        hasMap.put("username", usernameText);
        hasMap.put("birthdate", guardarFecha());

        Intent i = new Intent(this, REGISTROSignIn3.class);
        i.putExtra("hash",hasMap);
        startActivity(i);
        finish();

    }

    public void Sign1(View v){
        Intent i = new Intent(this, REGISTROSignIn1.class);
        startActivity(i);
        finish();
    }

    public String guardarFecha(){
        // Obtener la fecha seleccionada en milisegundos
        long selectedDateInMillis = selectedCalendar.getTimeInMillis();

        // Crear un objeto Date a partir de los milisegundos
        Date selectedDate = new Date(selectedDateInMillis);

        // Formatear la fecha como una cadena (string)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = sdf.format(selectedDate);
        return dateString;
    }



}