package com.example.takeit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    EditText editTextEmail, editTextPassword1, editTextPassword2, editTextName;

    Button buttonSignUp;
    Spinner spin1, spin2;

    String academic_year, branches, userID;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword1 = findViewById(R.id.editTextPassword1);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        spin1 = findViewById(R.id.spin1);
        spin2 = findViewById(R.id.spin2);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        spin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                academic_year =parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                branches = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextEmail.getText().toString();
                String password1 = editTextPassword1.getText().toString();
                String password2 = editTextPassword2.getText().toString();

                final String name = editTextName.getText().toString();



                if(!(password1.equals(password2))){
                    editTextPassword2.setError("incorrect password");
                    editTextPassword2.requestFocus();
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Registration.this,"User Created",Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("UserDetails").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("Name",name);
                            user.put("AcademicYear",academic_year);
                            user.put("Branch",branches);
                            user.put("EmailID",email);
                            user.put("Books Sold",0);
                            user.put("Books Received",0);
                            user.put("Books for sale",0);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","On success: User Profile is created for "+userID);
                                }
                            });

                            startActivity(new Intent(getApplicationContext(),Home.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Registration.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    public void goToLogin(View view) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();

    }
}