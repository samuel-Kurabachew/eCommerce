package com.sam.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;
import com.sam.ecommerce.model.Users;
import com.sam.ecommerce.prevalent.Prevalent;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText inputPhone, inputPassword;
    private ProgressDialog loadingBar;
    private TextView adminLink, notAdminLink;


    private String parentDBName = "Users";
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_button);
        inputPhone = findViewById(R.id.login_phone_number_input);
        inputPassword = findViewById(R.id.login_password_input);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        adminLink = findViewById(R.id.admin_panel_link);
        notAdminLink = findViewById(R.id.not_admin_panel_link);

        Paper.init(this);

        loadingBar = new ProgressDialog(this);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText(getString(R.string.login_admin_label));
                adminLink.setVisibility(View.INVISIBLE);
                notAdminLink.setVisibility(View.VISIBLE);
                parentDBName = "Admin";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText(getString(R.string.login_button_label));
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);
                parentDBName = "Users";
            }
        });
    }

    private void loginUser() {
        String phoneNumber = inputPhone.getText().toString();
        String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(phoneNumber)) {
            inputPhone.requestFocus();
            Toast.makeText(this, "Please input your phone...", Toast.LENGTH_SHORT).show();
        } if (TextUtils.isEmpty(password)) {
            inputPassword.requestFocus();
            Toast.makeText(this, "Please input your password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            allowAccessToAccount(phoneNumber, password);
        }
    }

    private void allowAccessToAccount(final String phoneNumber, final String password) {
        if (rememberMeCheckBox.isChecked()) {
            Paper.book().write(Prevalent.userPhoneKey, phoneNumber);
            Paper.book().write(Prevalent.userPasswordKey, password);
        }
        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDBName).child(phoneNumber).exists()) {
                    Users usersData = dataSnapshot.child(parentDBName).child(phoneNumber).getValue(Users.class);

                    if (usersData.getPhone().equals(phoneNumber)) {
                        if (usersData.getPassword().equals(password)) {
                            if (parentDBName.equals("Admin")) {
                                Toast.makeText(LoginActivity.this, "Welcome admin. you are logged in successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AdminAddNewProductActivity.class);
                                startActivity(intent);
                            } else if (parentDBName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Account with this " + phoneNumber + " doesn't exist. Please register.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
