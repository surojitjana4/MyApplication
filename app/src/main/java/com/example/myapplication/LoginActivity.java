package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private ImageView profile_pic;
    private LoginButton login;
    CallbackManager callbackManager;
    public static final String ShPref = "ShPref";
    private static final String EMAIL = "email";
    private static final String public_profile = "public_profile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }else{
            this.login();
        }
    }

    private void login() {
        login = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        login.setReadPermissions(Arrays.asList(public_profile,EMAIL));
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(getApplicationContext(),"Login Successful",Toast.LENGTH_SHORT).show();

                        String imageUrl = "https://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large";
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(ShPref, 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("imageUrl", imageUrl);
                        editor.putString("userId",loginResult.getAccessToken().getUserId());
                        editor.apply();

                        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {

                                JSONObject json = response.getJSONObject();
                                try {
                                    if(json != null){
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences(ShPref, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("name", json.getString("name"));
                                        editor.putString("email",json.getString("email"));
                                        editor.apply();
                                        String msg = "Welcome "+json.getString("name");
                                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();

                                        SharedPreferences sharedPreferences = getSharedPreferences("ShPref", Context.MODE_PRIVATE);
                                        String imageUrl1 = sharedPreferences.getString("imageUrl", "");
                                        String name1 = sharedPreferences.getString("name", "");

                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}