package com.gea.contactapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {
    private Session session;
    private Context context;
    private String jsonURL = "https://test.baity.com.br/contact/read_user_contacts.php";
    private String _user_id;
    private ArrayList<ContactModel> contactModelArrayList;
    private FloatingActionButton addContactFloatingBtn;
    RecyclerView contactRV;
    ProgressBar progressBar;
    private ContactAdapter contactAdapter;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        session = new Session(context);

        _user_id = session.getUserId();
        contactRV =findViewById(R.id.contactsRecyclerView);
        progressBar = findViewById(R.id.loadContactProgressBar);
        addContactFloatingBtn = findViewById(R.id.addContactFloatingBtn);
        getUserContacts();
        //recyclerview divider
        contactRV.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));


        final SwipeRefreshLayout refreshContacts = findViewById(R.id.refreshContacts);
        refreshContacts.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserContacts();
                refreshContacts.setRefreshing(false);
            }
        });
        addContactFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddContactActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    @SuppressLint("StaticFieldLeak")
    private void getUserContacts(){
        progressBar.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                map.put("UserId",_user_id);
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

        progressBar.setVisibility(View.GONE);
        if (isSuccess(response)) {
            contactModelArrayList = getInfo(response);
            contactAdapter = new ContactAdapter(context, contactModelArrayList);
            contactRV.setLayoutManager(new LinearLayoutManager(context));
            contactRV.setAdapter(contactAdapter);
           // Toast.makeText(context, "contact fetched successfully", Toast.LENGTH_LONG).show();
             }
        //else{
//            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
//
//        }
    }

    private ArrayList<ContactModel> getInfo(String response) {
        ArrayList<ContactModel> contactModelArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray dataArray = jsonObject.getJSONArray("contacts");
            for (int i = 0; i < dataArray.length(); i++) {

                JSONObject dataobj = dataArray.getJSONObject(i);
                ContactModel contactModel = new ContactModel();
                contactModel.setContactId(dataobj.getString("ContactId"));
                contactModel.setContactImage(dataobj.getString("ContactImage"));
                contactModel.setContactName(dataobj.getString("ContactName"));
                contactModel.setContactEmail(dataobj.getString("ContactEmail"));
                contactModel.setContactPhone(dataobj.getString("ContactPhone"));
                contactModel.setContactDateBirth(dataobj.getString("ContactDateBirth"));
                contactModel.setContactGender(dataobj.getString("ContactGender"));
                contactModel.setContactNote(dataobj.getString("ContactNote"));

                contactModelArrayList.add(contactModel);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contactModelArrayList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.searchItem);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                contactAdapter.getFilter().filter(s);
                return false;
            }
        });



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutItem) {
            session.logOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            HomeActivity.this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
