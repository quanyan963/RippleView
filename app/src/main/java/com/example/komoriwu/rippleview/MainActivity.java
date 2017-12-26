package com.example.komoriwu.rippleview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.example.komoriwu.rippleview.bean.Directive;
import com.example.komoriwu.rippleview.bean.Endpoint;
import com.example.komoriwu.rippleview.bean.Header;
import com.example.komoriwu.rippleview.bean.Run;
import com.example.komoriwu.rippleview.bean.TurnOn;
import com.example.komoriwu.rippleview.util.Constent;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_conn, btn_un_conn, btn_login,btn_light,btn_switch;
    private TextView message;
    private final String HOST = "182.61.53.117";
    private final int PORT = 22001;
    private Socket client;
    private OutputStream outStr = null;
    private InputStream inStr = null;
    public Thread tConn;
    private boolean connected;
    private Disposable mSendDisposable = null;
    private Disposable mReadDisposable = null;
    private int r;
    private RequestContext requestContext;
    private boolean mIsLoggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_conn = (Button) findViewById(R.id.btn_conn);
        btn_un_conn = (Button) findViewById(R.id.btn_un_conn);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_light = (Button) findViewById(R.id.btn_light);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        message = (TextView) findViewById(R.id.message);
        //et_text = (EditText) findViewById(R.id.et_text);

        btn_conn.setOnClickListener(this);
        btn_un_conn.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_switch.setOnClickListener(this);
        btn_light.setOnClickListener(this);

        requestContext = RequestContext.create(this);

        requestContext.registerListener(new AuthorizeListener() {
            /* Authorization was completed successfully. */
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                runOnUiThread(() -> {
                    // At this point we know the authorization completed, so remove the ability to return to the app to sign-in again
                    setLoggingInState(true);
                });
                fetchUserProfile();
            }

            /* There was an error during the attempt to authorize the application */
            @Override
            public void onError(AuthError authError) {
                Log.e("onError", "AuthError during authorization", authError);
                runOnUiThread(() -> {
                    showAuthToast("Error during authorization.  Please try again.");
                    resetProfileView();
                    setLoggingInState(false);
                });
            }

            /* Authorization was cancelled before it could be completed. */
            @Override
            public void onCancel(AuthCancellation authCancellation) {
                Log.e("onCancel", "User cancelled authorization");
                runOnUiThread(() -> {
                    showAuthToast("Authorization cancelled");
                    resetProfileView();
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = {ProfileScope.profile(), ProfileScope.postalCode()};
        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(final AuthorizeResult result) {
                if (result.getAccessToken() != null) {
                    /* The user is signed in */
                    runOnUiThread(() -> {
                        message.setText("");
                        message.setText("accessToken:"+result.getAccessToken().toString());
                    });

                    fetchUserProfile();
                } else {
                    runOnUiThread(() -> message.setText("The user is not signed in"));

                    /* The user is not signed in */
                }
            }

            @Override
            public void onError(AuthError ae) {
                /* The user is not signed in */
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    /**
     * Sets the text in the mProfileText {@link TextView} to the prompt it originally displayed.
     */
    private void resetProfileView() {
        setLoggingInState(false);
        message.setText("unLogin");
    }

    /**
     * Turns on/off display elements which indicate that the user is currently in the process of logging in
     *
     * @param loggingIn whether or not the user is currently in the process of logging in
     */
    private void setLoggingInState(final boolean loggingIn) {
        if (loggingIn) {
            btn_login.setText("logout");
            btn_login.setTextColor(Color.WHITE);
            btn_login.setBackgroundResource(R.color.green_color);
        } else {
            btn_login.setText("login");
            btn_login.setTextColor(Color.BLACK);
            btn_login.setBackgroundResource(R.color.gray);
        }
    }

    private void fetchUserProfile() {
        User.fetch(this, new Listener<User, AuthError>() {

            /* fetch completed successfully. */
            @Override
            public void onSuccess(User user) {
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipCode = user.getUserPostalCode();

                runOnUiThread(() -> updateProfileData(name, email, account, zipCode));
            }

            /* There was an error during the attempt to get the profile. */
            @Override
            public void onError(AuthError ae) {
                runOnUiThread(() -> {
                    setLoggedOutState();
                    String errorMessage = "Error retrieving profile information.\nPlease log in again";
                    Toast errorToast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG);
                    errorToast.setGravity(Gravity.CENTER, 0, 0);
                    errorToast.show();
                });
            }
        });
    }

    private void updateProfileData(String name, String email, String account, String zipCode) {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append(String.format("\nWelcome, %s!\n", name));
        profileBuilder.append(String.format("Your email is %s\n", email));
        profileBuilder.append(String.format("Your UserId is %s\n", account));
        final String profile = profileBuilder.toString();
        Log.d("updateProfileData", "Profile Response: " + profile);
        runOnUiThread(() -> {
            updateProfileView(profile);
            setLoggedInState();
        });
    }

    /**
     * Sets the text in the mProfileText {@link TextView} to the value of the provided String.
     *
     * @param profileInfo the String with which to update the {@link TextView}.
     */
    private void updateProfileView(String profileInfo) {
        Log.d("updateProfileView", "Updating profile view");
        message.setText(message.getText().toString()+profileInfo);
    }

    /**
     * Sets the state of the application to reflect that the user is currently authorized.
     */
    private void setLoggedInState() {
        mIsLoggedIn = true;
        setLoggingInState(true);
    }

    /**
     * Sets the state of the application to reflect that the user is not currently authorized.
     */
    private void setLoggedOutState() {
        mIsLoggedIn = false;
        resetProfileView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_conn:
                if (tConn == null) {
                    tConn = new Thread(new ConnThread());
                    tConn.start();
                }
                break;
            case R.id.btn_un_conn:
                connected = false;
                disconnect();
                break;
            case R.id.btn_login:
                if (!mIsLoggedIn){
                    AuthorizationManager.authorize(
                            new AuthorizeRequest.Builder(requestContext)
                                    .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                                    .build()
                    );
                }else {
                    AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
                        @Override
                        public void onSuccess(Void response) {
                            runOnUiThread(() -> setLoggedOutState());
                        }
                        @Override
                        public void onError(AuthError authError) {
                            Log.e("onError", "Error clearing authorization state.", authError);
                        }
                    });
                }

                break;
            case R.id.btn_switch:
                TurnOn turnOff = new TurnOn(new Directive(new Header(3,
                        "APP.PowerController",
                        (btn_switch.getText().toString().equals("off") ? "TurnOn" : "TurnOff"),
                        "456"),new Endpoint("endpoint-002")));
                Gson gsonColor = new Gson();
                OkGo.<String>post(Constent.HEAD+Constent.CONTROL)
                        .tag(this)
                        .upJson(gsonColor.toJson(turnOff))
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {

                            }
                        });
                break;
            case R.id.btn_light:
                TurnOn turnOn = new TurnOn(new Directive(new Header(3,
                        "APP.PowerController",
                        (btn_light.getText().toString().equals("off") ? "TurnOn" : "TurnOff"),
                        "456"),new Endpoint("endpoint-001")));
                Gson gsonLight = new Gson();
                OkGo.<String>post(Constent.HEAD+Constent.CONTROL)
                        .tag(this)
                        .upJson(gsonLight.toJson(turnOn))
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {

                                Log.d("ddd", "onSuccess: "+response.toString());
                            }
                        });
                break;
        }
    }

    private void showAuthToast(String authToastMessage) {
        Toast authToast = Toast.makeText(getApplicationContext(), authToastMessage, Toast.LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER, 0, 0);
        authToast.show();
    }

    private class ConnThread implements Runnable {

        @Override
        public void run() {
            client = new Socket();

            try {
                client.connect(new InetSocketAddress(HOST, PORT), 5000);
                outStr = client.getOutputStream();
                inStr = client.getInputStream();
                connected = true;
                outStr.write("account:456:endpointId:endpoint-001,endpoint-002\r\n".getBytes());
                mReadDisposable = Observable.just(connected)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(aBoolean -> {
                            try {
                                while (connected) {
                                    final byte[] b = new byte[1024];
                                    r = inStr.read(b);
                                    if (r > 0) {
                                        final String str = new String(b).trim();
                                        if (str.contains("account")){
                                            final Run result = new Gson().fromJson(str,Run.class);
                                            if (result.getNamespace().contains("PowerController")){
                                                if (result.getName().contains("On") || result.getName().contains("Off")) {
                                                    switch (result.getEndpointId()){
                                                        case "endpoint-001":
                                                            runOnUiThread(() -> {
                                                                btn_light.setText(result.getName().contains("On") ? "on" : "off");
                                                                btn_light.setTextColor(result.getName().contains("On") ? Color.WHITE : Color.BLACK);
                                                                btn_light.setBackgroundResource(result.getName().contains("On") ?
                                                                        R.color.green_color : R.color.gray);
                                                            });
                                                            break;
                                                        case "endpoint-002":
                                                            runOnUiThread(() -> {
                                                                btn_switch.setText(result.getName().contains("On") ? "on" : "off");
                                                                btn_switch.setTextColor(result.getName().contains("On") ? Color.WHITE : Color.BLACK);
                                                                btn_switch.setBackgroundResource(result.getName().contains("On") ?
                                                                        R.color.green_color : R.color.gray);
                                                            });
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                        Log.d("read", "run: " + str);

                                    } else {
                                        if (connected) {
                                            disconnect();
                                            connect();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (connected) {
                                    disconnect();
                                    connect();
                                }
                            }

                        });

                mSendDisposable = Observable.interval(30, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(aLong -> {
                            if (connected) {
                                outStr.write("heart\r\n".getBytes());
                                Log.d("send", "accept: heart");
                            }
                        });

            } catch (IOException e) {
                try {
                    Thread.sleep(3000);
                    if (!connected) {
                        disconnect();
                        connect();
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

        }
    }

    public void connect() {
        tConn = new Thread(new ConnThread());
        tConn.start();
    }

    public void disconnect() {
        try {
            if (client != null) {
                client.shutdownOutput();
                client.shutdownInput();
                client.close();
                client = null;
            }
            tConn = null;
            connected = false;
            if (outStr != null) {
                outStr.close();
                outStr = null;
            }
            if (inStr != null) {
                inStr.close();
                inStr = null;
            }
            if (mReadDisposable != null) {
                mReadDisposable.dispose();
            }
            if (mSendDisposable != null) {
                mSendDisposable.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
