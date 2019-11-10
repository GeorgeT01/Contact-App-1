package com.gea.contactapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditContactActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 7777;
    private Context context;
    private String _contact_id, contact_name, contact_email, contact_phone, contact_note, contact_gender, contact_image, contact_db;
    private CircleImageView contactImg;
    private EditText contactName, contactEmail, contactPhone, contactNote;
    private Button contactDb;
    private boolean imageSelected;
    private Uri selectedImage;
    private Spinner contactGender;
    private ProgressDialog progressDialog;
    private String jsonURL = "";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        context = this;
        setTitle("Edit Contact");
        // change back button to close icon
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
        //get contact id
        Intent intent = getIntent();
        _contact_id = intent.getStringExtra("contact_id");
        contact_name = intent.getStringExtra("contact_name");
        contact_email = intent.getStringExtra("contact_email");
        contact_phone = intent.getStringExtra("contact_phone");
        contact_image = intent.getStringExtra("contact_img");
        contact_gender = intent.getStringExtra("contact_gender");
        contact_db = intent.getStringExtra("contact_db");
        contact_note = intent.getStringExtra("contact_note");


        contactName = findViewById(R.id.editContactName);
        contactEmail = findViewById(R.id.editContactEmail);
        contactPhone = findViewById(R.id.editContactPhone);
        contactDb = findViewById(R.id.editContactDate);
        contactGender = findViewById(R.id.editContactGender);
        contactNote = findViewById(R.id.editContactNote);
        contactImg = findViewById(R.id.editContactImage);

        contactName.setText(contact_name);
        Picasso.get().load(contact_image).into(contactImg);
        contactEmail.setText(contact_email);
        contactPhone.setText(contact_phone);
        contactDb.setText(contact_db);
        populateSpinner();
        contactNote.setText(contact_note);
        contactDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        //browse image
        contactImg.setOnClickListener(new View.OnClickListener() {
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
        String[] arrayGender;
        if (contact_gender.equals("Male")){
            arrayGender = new String[] { "Male", "Female" };
        }else{
            arrayGender = new String[] { "Female", "Male" };
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, arrayGender);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactGender.setAdapter(adapter);
        contactGender.setPrompt("Gender");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.doneMenuBtn){
            if (TextUtils.isEmpty(contactName.getText().toString())){
                contactName.setError("Name is required");
                contactName.requestFocus();
            }else {
                EditContact();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            contactImg.setImageURI(selectedImage);
            imageSelected = true;
        }else{
            if(imageSelected){
                contactImg.setImageURI(selectedImage);
            }else{
                Picasso.get().load(contact_image).into(contactImg);
            }
        }
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
                        contactDb.setText(_day + "." + _month + "." + year);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show(); // show dialog
    }


    public void EditContact(){
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, jsonURL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Toast.makeText(context, "Contact edited successfullty", Toast.LENGTH_LONG).show();
                onBackPressed();
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
                params.put("ContactId", _contact_id);
                params.put("ContactName", contactName.getText().toString());
                params.put("ContactEmail", contactEmail.getText().toString());
                params.put("ContactPhone", contactPhone.getText().toString());
                params.put("ContactDateBirth", contactDb.getText().toString());
                params.put("ContactGender", contactGender.getSelectedItem().toString());
                params.put("ContactNote", contactNote.getText().toString());
                params.put("ContactImage", ImageToString(((BitmapDrawable)contactImg.getDrawable()).getBitmap()));

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
