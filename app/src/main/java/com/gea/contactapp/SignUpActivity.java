package com.gea.contactapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupEmailTxt, signupPasswordTxt, confirmPasswordTxt;
    private TextView passwordError;
    private Button signUpButton;
    private boolean validSignUp;
    private Context context;
    private String jsonURL = "";
    private static ProgressDialog mProgressDialog;
    private static String _password;
    private static String _email;
    private Session session;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign up");

        signupEmailTxt =findViewById(R.id.signup_emailTxt);
        signupPasswordTxt =findViewById(R.id.signup_passwordTxt);
        confirmPasswordTxt =findViewById(R.id.confirm_passwordTxt);
        passwordError = findViewById(R.id.passwordErrorMsg);
        signUpButton = findViewById(R.id.signupBtn);
        context = this;
        session = new Session(context);

        // validate password
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (confirmPasswordTxt.getText().toString().equals(signupPasswordTxt.getText().toString())){
                    passwordError.setVisibility(View.INVISIBLE);
                    validSignUp = true;
                }else{
                    passwordError.setVisibility(View.VISIBLE);
                    validSignUp = false;
                }

                // check if empty to remove error msg
                if (confirmPasswordTxt.getText().toString().isEmpty() && signupPasswordTxt.getText().toString().isEmpty()){
                    passwordError.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        confirmPasswordTxt.addTextChangedListener(textWatcher);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InternetStatus.isConnectedToInternet(context)){ // check internet connection
                    if(signupEmailTxt.getText().toString().isEmpty()){ // validate email and password input
                        signupEmailTxt.setError("Email field is empty");
                    }else if(signupPasswordTxt.getText().toString().isEmpty()){
                        signupPasswordTxt.setError("Password is empty");
                    }else{
                        if(validSignUp){
                            SignUp();
                        }else{
                            passwordError.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    showAlertDialog("Oops!", "No internet connection");
                }
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private void SignUp(){

        showSimpleProgressDialog(context, "Loading...",false);
        _email  = signupEmailTxt.getText().toString();
        _password  = signupPasswordTxt.getText().toString();

        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map = new HashMap<>();
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
                Log.d("newwwss",result);
                onTaskCompleted(result);
            }
        }.execute();
    }

    public void onTaskCompleted(String response) {

        removeSimpleProgressDialog();

        if (isSuccess(response)) {
            getUserInfo(response); // get user info && set session;
            // redirect to HomeActivity
            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
            SignUpActivity.this.startActivity(intent);
        }else {
            showAlertDialog("Sign up error", getErrorCode(response));
        }
    }

    private void getUserInfo(String response) {

        try {
            JSONObject jsonObject = new JSONObject();
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
        String errorMsg;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("message").equals("User already exist")){
                errorMsg = "User already exist";
            }else{
                errorMsg = "Error Signning up";
            }

        } catch (JSONException e) {
            e.printStackTrace();
            errorMsg ="Error";
        }
        return errorMsg;
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
}
