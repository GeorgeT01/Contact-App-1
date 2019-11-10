package com.gea.contactapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button loginBtn;
    TextView goToSignUp;
    private EditText emailTxt, passwordTxt;
    private String jsonURL = "";
    private static ProgressDialog mProgressDialog;
    private Context context;
    private static String _password;
    private static String _email;
    private Session session;
    private String user_id, user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Login");
        context = this;
        session = new Session(context);
        user_id = session.getUserId();
        user_email = session.getUserEmail();
        /*
        check if user logged in
        by checking session values
         */
        if (!user_email.equals("") && !user_id.equals("")){
            Intent _intent = new Intent(MainActivity.this, HomeActivity.class);
            MainActivity.this.startActivity(_intent);
        }

        emailTxt = findViewById(R.id.login_emailTxt);
        passwordTxt = findViewById(R.id.login_passwordTxt);

        // <<Sign up>> TextView click event handler
        goToSignUp = findViewById(R.id.signupPage);
        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        // <<Login Button>> click event handler
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (InternetStatus.isConnectedToInternet(context)){ // check internet connection
                if(emailTxt.getText().toString().isEmpty()){ // validate email and password input
                    emailTxt.setError("Email field is empty");
                }else if(passwordTxt.getText().toString().isEmpty()){
                    passwordTxt.setError("Password is empty");
                }else{
                    login(); // call api to login
                }
            }else{
                showAlertDialog("Oops!", "No internet connection");
            }

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void login(){

        showSimpleProgressDialog(context, "Loading...",false);
        _email  = emailTxt.getText().toString();
        _password  = passwordTxt.getText().toString();

        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                map.put("UserEmail",_email);
                map.put("UserPassword",_password);
                try {
                    HttpsRequest req = new HttpsRequest(jsonURL);
                    response = req.prepare(HttpsRequest.Method.POST).withData(map).sendAndReadString();

                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted(result);
            }
        }.execute();
    }

    public void onTaskCompleted(String response) {

        removeSimpleProgressDialog();

        if (isSuccess(response)) {
            getUserInfo(response); // get user info && set session;
            // redirect to HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            MainActivity.this.startActivity(intent);
        }else {
            showAlertDialog("Login error", "Wrong email or password");
        }

    }

    private void getUserInfo(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray dataArray = jsonObject.getJSONArray("user_info");
            JSONObject dataobj = dataArray.getJSONObject(0);
            /* set session
            * */
            session.setUserId(dataobj.getString("UserId"));
            session.setUserEmail(dataobj.getString("UserEmail"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isSuccess(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);

            return jsonObject.getString("error").equals("false");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getErrorCode(String response) {
            String msg ="";
        try {
            JSONObject jsonObject = new JSONObject(response);
            msg =  jsonObject.getString("message"); // json object called message

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static void removeSimpleProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();

        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showSimpleProgressDialog(Context context,
                                                String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, "",msg);
                mProgressDialog.setCancelable(isCancelable);
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog(String title, String msg){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() { moveTaskToBack(true); }
}
