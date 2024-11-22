package com.example.music.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.music.MainActivity;
import com.example.music.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private Button btnLogin,btnRegisterInLogin;
    private EditText edtLoginEmail, edtLoginPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();
        initListener();
    }

    private void initUi() {
        btnRegisterInLogin = findViewById(R.id.btnRegisterInLogin);
        btnLogin = findViewById(R.id.btnLogin);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        progressDialog = new ProgressDialog(this);
    }

    private void initListener() {
        //Xu ly su kien an nut dang nhap
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtLoginEmail.getText().toString().trim();
                String password = edtLoginPassword.getText().toString().trim();

                //Check dien day du thong tin
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();

                //Hien thi progress dialog
                progressDialog.show();

                //Xac thuc nguoi dung voi Firebase
                auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()){
                                    //Dang nhap thanh cong
                                    FirebaseUser user = auth.getCurrentUser();
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                }else{
                                    //Dang nhap that bai
                                    Toast.makeText(Login.this, "Đăng nhập thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //Xu li su kien khi an nut dang ky
        btnRegisterInLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,Register.class);
                startActivity(intent);
            }
        });
    }
}