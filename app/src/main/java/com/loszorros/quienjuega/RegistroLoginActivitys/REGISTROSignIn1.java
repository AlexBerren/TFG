package com.loszorros.quienjuega.RegistroLoginActivitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loszorros.quienjuega.R;

public class REGISTROSignIn1 extends AppCompatActivity {

     Button botonPasarPagina;
     Button botonVolverAtras;
     EditText cajaEmail;
     EditText pwd1;
     EditText pwd2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in1);

        botonPasarPagina = findViewById(R.id.botonSiguiente);
        botonVolverAtras = findViewById(R.id.botonVolver);
        cajaEmail = findViewById(R.id.TxtEmail);
        pwd1 = findViewById(R.id.TxtPwd1);
        pwd2 = findViewById(R.id.TxtPwd2);
        //getSupportActionBar().hide();

        InputFilter filtroEmail = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isLetterOrDigit(character) || character == '@'|| character == '.') {
                        stringBuilder.append(character);
                    }
                }

                return stringBuilder.toString();
            }
        };

// Aplicar el filtro al EditText
        cajaEmail.setFilters(new InputFilter[] { filtroEmail });

        setup();
    }

    //Te lleva a la pagina 2 del registro
   /** public void Sign2(View v){
        Intent i = new Intent(this, REGISTROSignIn2.class);
        startActivity(i);
        finish();
    }*/

    //Te lleva a la pagina 1 del registro
    public void backToLogin(View v){
        Intent i = new Intent(this, INICIOLoginActivity.class);
        startActivity(i);
        finish();
    }

    /**Cuando se pulsa el boton se valida si este usuario existe, si es así
     * entones se registra y pasa a la siguiente pantalla, si no se le dice que esa cuanta no existe que pruebe otra vez o que
     * se registre
     */
    public void setup(){
        botonPasarPagina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cajaEmail.getText().toString().isEmpty() || pwd1.getText().toString().isEmpty() || pwd2.getText().toString().isEmpty() || !pwd1.getText().toString().equals(pwd2.getText().toString())){
                    showAlert();
                }else{
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(
                                    cajaEmail.getText().toString(),
                                    pwd1.getText().toString()
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                   if (task.isSuccessful()){
                                       SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);

                                       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                       user.sendEmailVerification();
                                       SharedPreferences.Editor editor = prefe.edit();
                                       editor.putString("email", cajaEmail.getText().toString());
                                       editor.apply();
                                       Intent i = new Intent(getApplicationContext(), REGISTROSignIn2.class);
                                       startActivity(i);
                                   }else{
                                       showAlert();
                                   }
                                }
                            });
                }
            }
        });
    }
    /** Lanza un cuadro de alerta cuando ha habido un fallo*/
    private void showAlert(){
        //Crear un cuadro de error
        AlertDialog.Builder builder = new AlertDialog.Builder(REGISTROSignIn1.this);
        builder.setTitle("Error de autenticación");
        builder.setMessage("Se ha producido un error en la autenticación");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción a realizar cuando se hace clic en el botón "Aceptar"
                dialog.dismiss(); //Cerrar el cuadro
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}