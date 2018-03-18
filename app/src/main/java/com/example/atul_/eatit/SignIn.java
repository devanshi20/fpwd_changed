package com.example.atul_.eatit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atul_.eatit.Common.Common;
import com.example.atul_.eatit.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    TextView btnSignUp;
    CheckBox ckbRemember;
    TextView txtForgotPwd;
    ProgressDialog progressDialog;
    DatabaseReference table_user;
    FirebaseDatabase database;



    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public final boolean validate() {
        boolean valid = true;

        String phone = edtPhone.getText().toString();
        String password = edtPassword.getText().toString();


        if (phone.isEmpty()) {
            edtPhone.setError(" Invalid Id");
            valid = false;
        } else {
            edtPhone.setError(null);
        }

        if(password.isEmpty()) {
            edtPassword.setError("Invalid Password");
            valid = false;
        } else {
            edtPassword.setError(null);
        }



        return valid;
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        edtPassword = (EditText) findViewById(R.id.edtpassword);
        edtPhone = (EditText) findViewById(R.id.edtphone);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        ckbRemember = (CheckBox) findViewById(R.id.ckbRemember);
        txtForgotPwd = (TextView) findViewById(R.id.txtForgotPwd);
        btnSignUp = (TextView) findViewById(R.id.btnSignUp);

        Paper.init(this);


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotpwdDialog();
            }


        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View view) {
                String phone = edtPhone.getText().toString();
                String password = edtPassword.getText().toString();

                if (phone.isEmpty() && password.isEmpty()) {
                    edtPhone.setError("Required");
                    edtPassword.setError("Required");
                }
                else {

                    if (Common.isConnectedToInternet(getBaseContext())) {


                        if (ckbRemember.isChecked()) {
                            Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                            Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                        }

                        validate();
                        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                        mDialog.setMessage("Please wait");
                        mDialog.show();


                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                    mDialog.dismiss();
                                    User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                    user.setPhone(edtPhone.getText().toString());


                                    if (user.getPassword().equals(edtPassword.getText().toString())) {
                                        Intent homeIntent = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(homeIntent);
                                        finish();

                                        table_user.removeEventListener(this);
                                    } else {
                                        Toast.makeText(SignIn.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    Toast.makeText(SignIn.this, "User does not exist.", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(SignIn.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick (View v){
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
            }
        });

        String user=Paper.book().read(Common.USER_KEY);
        String pwd=Paper.book().read(Common.PWD_KEY);

        if (user !=null && pwd!=null){

            if (!user.isEmpty() && !pwd.isEmpty()) {
                Intent home = new Intent(SignIn.this, Home.class);

                startActivity(home);


            }


        }}



    private void showForgotpwdDialog() {


            AlertDialog.Builder alertDialog=new AlertDialog.Builder(SignIn.this);
            alertDialog.setTitle("CHANGE PASSWORD");
            alertDialog.setMessage("Please fill all information");
            LayoutInflater inflater=LayoutInflater.from(this);
            View layout_pwd=inflater.inflate(R.layout.change_password_layout,null );
            final MaterialEditText edtPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
            final MaterialEditText edtNewPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
            final MaterialEditText edtRepeatPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);
            alertDialog.setView(layout_pwd);

            alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    final android.app.AlertDialog waitingDialog=new SpotsDialog(SignIn.this);
                    waitingDialog.show();

                    if (edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                    {
                        if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                        {
                            Map<String,Object> passwordUpdate=new HashMap<>();
                            passwordUpdate.put("Password",edtNewPassword.getText().toString());

                            DatabaseReference user=FirebaseDatabase.getInstance().getReference("User");
                            user.child(Common.currentUser.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitingDialog.dismiss();
                                            Toast.makeText(SignIn.this, "Password was updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignIn.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                        else
                        {
                            waitingDialog.dismiss();
                            Toast.makeText(SignIn.this, "New password doesnt match", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {

                        waitingDialog.dismiss();
                        Toast.makeText(SignIn.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });

            alertDialog.show();






    }
}


