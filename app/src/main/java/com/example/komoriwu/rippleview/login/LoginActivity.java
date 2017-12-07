package com.example.komoriwu.rippleview.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.komoriwu.rippleview.MainActivity;
import com.example.komoriwu.rippleview.R;
import com.example.komoriwu.rippleview.util.Constent;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;


/**
 * Created by KomoriWu on 2017/11/9.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //@BindView(R.id.et_name)
    EditText etName;
    //@BindView(R.id.et_pass)
    EditText etPass;
    //@BindView(R.id.btn_login)
    Button btnLogin;
    //@BindView(R.id.tv_register)
    TextView tvRegister;
    //@BindView(R.id.tv_forgot)
    TextView tvForgot;

    private String userName;
    private String pass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        btnLogin = (Button) findViewById(R.id.btn_login);
        etName = (EditText) findViewById(R.id.et_name);
        etPass = (EditText) findViewById(R.id.et_pass);
        tvRegister = (TextView) findViewById(R.id.tv_register);
        tvForgot = (TextView) findViewById(R.id.tv_forgot);
        btnLogin.setOnClickListener(this);
        tvForgot.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

    }

    private boolean check() {
        userName = etName.getText().toString();
        pass = etPass.getText().toString();
        if (userName.length() == 0) {
            Toast.makeText(this, "Please enter the UserName", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.length() == 0){
            Toast.makeText(this, "Please enter the Password", Toast.LENGTH_SHORT).show();
            return false;
        }
            return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (check()) {
                    OkGo.<String>get(Constent.HEAD+Constent.LOGIN+"?username="+userName+"&password="+pass)
                            .tag(this)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            });
                }
                break;
            case R.id.tv_register:
                OkGo.<String>post(Constent.HEAD+Constent.REGISTER)
                        .tag(this)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {

                            }
                        });
                break;
            case R.id.tv_forgot:
                OkGo.<String>post(Constent.HEAD+Constent.FORGOT)
                        .tag(this)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {

                            }
                        });
                break;
        }
    }
}
