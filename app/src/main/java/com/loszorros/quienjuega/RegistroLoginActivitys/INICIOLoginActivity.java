package com.loszorros.quienjuega.RegistroLoginActivitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.loszorros.quienjuega.MainActivity;
import com.loszorros.quienjuega.R;

public class INICIOLoginActivity extends AppCompatActivity {

    private int GOOGLE_SIGN_IN = 100;

    private TextView registrate;
    Button iniciarSesion;
    EditText cajaEmail;
    EditText pwd;
    Button botonGoogle;

    FirebaseUser user;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Setup
        iniciarSesion = findViewById(R.id.btnInicioSesion);
        cajaEmail = findViewById(R.id.TxtEmailLogIn);
        pwd = findViewById(R.id.TxtPwdLogIn);
        botonGoogle = findViewById(R.id.GoogleAuth);
        user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email",null);
        setup();
        //getSupportActionBar().hide();
        if (email!=null){
            cajaEmail.setText(email);
            session();
        }
    }

    //te lleva a la actividad de registro
    public void SignIn(View v){
        Intent i = new Intent(this, REGISTROSignIn1.class);
        startActivity(i);
        finish();
    }

    //Mantener sesion iniciada, comprueba si la preferencia esta vacia, si no lo esta inicia sesion
    public void session(){
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email",null);



        if (email != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                // El usuario ha verificado su cuenta
                cajaEmail.setText(email);
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            } else {
                // El usuario no ha verificado su cuenta
                // Aquí puedes mostrar un mensaje o realizar alguna acción adicional
                // Por ejemplo, mostrar un AlertDialog indicando que el usuario debe verificar su cuenta antes de acceder a MainActivity
                showVerificationAlert();
            }
        }

    }



    /**Cuando se pulsa el boton se valida si este usuario existe, si es así
     * entones se loggea, si no se le dice que esa cuanta no existe que pruebe otra vez o que
     * se registre
     */
    public void setup(){
        iniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cajaEmail.getText().toString().isEmpty() || pwd.getText().toString().isEmpty()) {
                    showAlert();
                } else {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            cajaEmail.getText().toString(),
                            pwd.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    // The user has verified their email
                                    // Saving data and starting MainActivity
                                    SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefe.edit();
                                    editor.putString("email", cajaEmail.getText().toString());
                                    editor.apply();

                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);
                                } else {
                                    // The user has not verified their email
                                    // Show alert or perform additional action
                                    showVerificationAlert();
                                }
                            } else {
                                showAlert();
                            }
                        }
                    });
                }
            }
        });
        //Configuracion de autenticacion con google (PASO 1 para el registro con Google)
        botonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions googleConf =
                        //Configurar login con google
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail().build();

                //Creamos cliente de autenticacion de google
                GoogleSignInClient googleClient = GoogleSignIn.getClient(getApplicationContext(), googleConf);
                googleClient.signOut(); //Para poder elegir una cuenta distinta (por si tenemos varias con google)
                startActivityForResult(googleClient.getSignInIntent(), GOOGLE_SIGN_IN); //Mostrar pantalla de autenticacion de google
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Verificar si la cuenta ya está registrada en Firebase Authentication
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(account.getEmail())
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();
                                    if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                                        // La cuenta ya está registrada, redirigir al Mai
                                        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefe.edit();
                                        editor.putString("email", account.getEmail());
                                        editor.apply();

                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);
                                    } else {
                                        // La cuenta no está registrada, realizar el registro con Google
                                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Guardado de datos, crea la preferencia e introduce el email en ella
                                                    SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = prefe.edit();
                                                    editor.putString("email", account.getEmail());
                                                    editor.apply();

                                                    Intent i = new Intent(getApplicationContext(), REGISTROSignIn2.class);
                                                    startActivity(i);
                                                } else {
                                                    showAlert();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    showAlert();
                                }
                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    }


    private void showAlert(){
        //Crear un cuadro de error
        AlertDialog.Builder builder = new AlertDialog.Builder(INICIOLoginActivity.this);
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

    private void showVerificationAlert(){
        //Crear un cuadro de error
        AlertDialog.Builder builder = new AlertDialog.Builder(INICIOLoginActivity.this);
        builder.setTitle("Error de Verificación");
        builder.setMessage("Usted no ha verificado su cuenta, porfavor revise el email");
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