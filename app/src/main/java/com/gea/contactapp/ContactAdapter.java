package com.gea.contactapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerViewHolder> implements Filterable {

    private Context context;
    private LayoutInflater inflater;
    private List<ContactModel> data;
    private List<ContactModel> dataFilterd;

    // constructor to innitilize context and data sent from MainActivity
    public ContactAdapter(Context context, List<ContactModel> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data = data;
        this.dataFilterd = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.contact_item, parent,false);

        return new RecyclerViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        // Get current position of item in recycler view to bind data and assign values from list

        final ContactModel contactModel = data.get(position);
        holder.name.setText(contactModel.getContactName());
        holder.phone.setText(contactModel.getContactPhone());
        holder.email.setText(contactModel.getContactEmail());
        Picasso.get().load(contactModel.getContactImage()).into(holder.circleImageView);




        // convert byte[] to image
        final String[] Options = {"Delete", "Edit"};
        holder.setOnClickListener(new ItemClickListener() {
            @Override
            public void onClick(final View view, final int position, boolean isLongClcik) {
                if (isLongClcik){
                    AlertDialog.Builder optionDialog;
                    optionDialog = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
                    optionDialog.setItems(Options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0){
                                new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                                        .setTitle("Delete Contact")
                                        .setMessage("Are you sure you want to delete this contact?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Continue with delete operation
                                                deleteContact(contactModel.getContactId());
                                                data.remove(position);
                                                notifyDataSetChanged();

                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton("No", null)
                                        .show();

                            }else if(which == 1){
                                Intent intent = new Intent(view.getContext(), ContactDetailsActivity.class);
                                intent.putExtra("contact_id", contactModel.getContactId()); //Optional parameters
                                intent.putExtra("contact_img", contactModel.getContactImage());
                                intent.putExtra("contact_name", contactModel.getContactName());
                                intent.putExtra("contact_email", contactModel.getContactEmail());
                                intent.putExtra("contact_phone", contactModel.getContactPhone());
                                intent.putExtra("contact_db", contactModel.getContactDateBirth());
                                intent.putExtra("contact_gender", contactModel.getContactGender());
                                intent.putExtra("contact_note", contactModel.getContactNote());
                                view.getContext().startActivity(intent);
                            }
                        }
                    });

                    optionDialog.show();

                }else{

                    Intent intent = new Intent(view.getContext(), ContactDetailsActivity.class);
                    intent.putExtra("contact_id", contactModel.getContactId()); //Optional parameters
                    intent.putExtra("contact_img", contactModel.getContactImage());
                    intent.putExtra("contact_name", contactModel.getContactName());
                    intent.putExtra("contact_email", contactModel.getContactEmail());
                    intent.putExtra("contact_phone", contactModel.getContactPhone());
                    intent.putExtra("contact_db", contactModel.getContactDateBirth());
                    intent.putExtra("contact_gender", contactModel.getContactGender());
                    intent.putExtra("contact_note", contactModel.getContactNote());
                    view.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public Filter getFilter() {

        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ContactModel> filterdList = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0){
                filterdList.addAll(dataFilterd);
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (ContactModel contactModel: dataFilterd){
                    if (contactModel.getContactName().toLowerCase().contains(filterPattern)){
                        filterdList.add(contactModel);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterdList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            data.clear();
            data.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };


    /// DELETE CONTACT
    @SuppressLint("StaticFieldLeak")
    private void deleteContact(final String _contact_ic){
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                map.put("ContactId",_contact_ic);
                try {
                    HttpsRequest req = new HttpsRequest("https://test.baity.com.br/contact/delete_contact.php");
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
        if (isSuccess(response)) {
            Toast.makeText(context, "Contact deleted", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(context, "Error connecting to server!", Toast.LENGTH_SHORT).show();
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

}

class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

    private ItemClickListener itemClickListener;
    public TextView name, phone, email;
    public CircleImageView circleImageView;

    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.nameTxt);
        phone = itemView.findViewById(R.id.phoneTxt);
        email = itemView.findViewById(R.id.emailTxt);
        circleImageView =  itemView.findViewById(R.id.contactImgView);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }
    public void setOnClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;

    }
    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(),false);
    }

    @Override
    public boolean onLongClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), true);
        return true;
    }
}