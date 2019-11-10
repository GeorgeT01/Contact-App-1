package com.gea.contactapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddContactActivity extends AppCompatActivity {

    private Context context;
    private Spinner genderSpinner;
    private Button datePickerBtn;
    private TextView newContactName, newContactEmail, newContactPhone, newContactNote;
    private CircleImageView newContactImage;
    public static final int PICK_IMAGE = 7777;
    private boolean imageSelected;
    private Uri selectedImage;
    private Session session;
    private String jsonURL = "https://test.baity.com.br/contact/add_new_contact.php";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        setTitle("Add Conatact");
        context = this;
        session = new Session(context);


        genderSpinner = findViewById(R.id.newContactGender);
        populateSpinner();

        newContactName = findViewById(R.id.newContactName);
        newContactEmail = findViewById(R.id.newContactEmail);
        newContactPhone = findViewById(R.id.newContactPhone);
        newContactNote = findViewById(R.id.newContactNote);
        newContactImage =findViewById(R.id.newContactImage);
        imageSelected = false;
        newContactPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //date picker
        datePickerBtn = findViewById(R.id.newContactDate);
        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });


        //browse image
        newContactImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
    }

    public void populateSpinner(){
        // add items to spinner
        String[] arrayGender = new String[] { "Male", "Female" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, arrayGender);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setPrompt("Gender");
    }

    public void showDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);//y
        int month = calendar.get(Calendar.MONTH);//m
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); //d

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month += 1;
                        String _month, _day;
                        if(month< 10){ _month = "0"+(month); }
                        else{ _month = Integer.toString(month); }
                        if(day < 10){ _day = "0"+day; }
                        else{ _day = Integer.toString(day); }
                        datePickerBtn.setText(_day + "." + _month + "." + year);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show(); // show dialog
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveMenuBtn){
            if (TextUtils.isEmpty(newContactName.getText().toString())){
                newContactName.setError("Name is required");
                newContactName.requestFocus();
            }else {
                AddNewContact();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();


            newContactImage.setImageURI(selectedImage);
            imageSelected = true;
        }else{
            if(imageSelected){
                newContactImage.setImageURI(selectedImage);
            }else{
                selectedImage =Uri.parse("android.resource://com.gea.contactapp/"+R.drawable.user);
                newContactImage.setImageURI(selectedImage);
            }
        }
    }

    public void AddNewContact(){
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, jsonURL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Toast.makeText(context, "Contact added successfullty", Toast.LENGTH_LONG).show();
                Intent _intent = new Intent(AddContactActivity.this, HomeActivity.class);
                AddContactActivity.this.startActivity(_intent);
                Log.d("Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show();
                // error
                Log.d("Error.Response", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("UserId", session.getUserId());
                params.put("ContactName", newContactName.getText().toString());
                params.put("ContactEmail", newContactEmail.getText().toString());
                params.put("ContactPhone", newContactPhone.getText().toString());
                params.put("ContactDateBirth", datePickerBtn.getText().toString());
                params.put("ContactGender", genderSpinner.getSelectedItem().toString());
                params.put("ContactNote", newContactNote.getText().toString());
                String contactImg;
                if (imageSelected){
                    contactImg = ImageToString(((BitmapDrawable)newContactImage.getDrawable()).getBitmap());
                }else{
                    contactImg ="";
                }
                params.put("ContactImage", contactImg);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    private String ImageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodingImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodingImage;
    }

}
