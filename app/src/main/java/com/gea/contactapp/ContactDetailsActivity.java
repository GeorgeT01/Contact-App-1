package com.gea.contactapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactDetailsActivity extends AppCompatActivity {
    String _contactId, _contactImg, _contactName, _contactEmail, _contactPhone, _contactGender, _contactDb, _conatcNote;
    private Context context;
    private CircleImageView circleImageView;
    private TextView contactName, contactEmail, contactPhone, contactBd, contactGender, contactNote;
    private ListView listView;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        context = this;
        listView = findViewById(R.id.detailsListView);
        getContact();
        populateListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editMenuBtn) {
            Intent intent = new Intent(context, EditContactActivity.class);
            intent.putExtra("contact_id", _contactId);
            intent.putExtra("contact_img", _contactImg);
            intent.putExtra("contact_name", _contactName);
            intent.putExtra("contact_email", _contactEmail);
            intent.putExtra("contact_phone", _contactPhone);
            intent.putExtra("contact_gender", _contactGender);
            intent.putExtra("contact_db", _contactDb);
            intent.putExtra("contact_note", _conatcNote);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getContact(){
        //get contact id
        Intent intent = getIntent();
        _contactId = intent.getStringExtra("contact_id");
        _contactImg = intent.getStringExtra("contact_img");
        _contactName = intent.getStringExtra("contact_name");
        _contactEmail = intent.getStringExtra("contact_email");
        _contactPhone = intent.getStringExtra("contact_phone");
        _contactGender = intent.getStringExtra("contact_gender");
        _contactDb = intent.getStringExtra("contact_db");
        _conatcNote = intent.getStringExtra("contact_note");


        contactName =findViewById(R.id.contactName);
        circleImageView =findViewById(R.id.contactImage);

        setTitle(_contactName);
        contactName.setText(_contactName);
        Picasso.get().load(_contactImg).into(circleImageView);

    }
    /// populate list view in contact fragment
    private void populateListView(){

        String[] itemText = new String[]{
                _contactEmail,
                _contactPhone,
                _contactGender,
                _contactDb,
                _conatcNote
        };
        int[] itemImage = new int[]{
                R.drawable.ic_email,
                R.drawable.ic_phone,
                R.drawable.ic_gender,
                R.drawable.ic_calendar,
                R.drawable.ic_note
        };


        List<HashMap<String, String>> aList = new ArrayList<>();
        String[] from = {"ItemImage", "itemText"};

        int[] to = {R.id.itemImage, R.id.itemText};

        for (int i = 0; i < itemText.length; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("itemText", itemText[i]);
            hm.put("ItemImage", Integer.toString(itemImage[i]));
            aList.add(hm);
        }
        // set adapter to listview
        final SimpleAdapter simpleAdapter = new SimpleAdapter(context, aList, R.layout.contact_details_item, from, to);
        listView.setAdapter(simpleAdapter);
    }
}
