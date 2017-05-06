package jid.quitedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by JiYeon on 2016-06-14.
 */
public class SettingsActivity extends AppCompatActivity {
    private static final int PICK_CONTACT = 1;
    public static final String MyPREFERENCES = "ExceptionList" ;
    private SharedPreferences sharedpreferences;
    private List<String> exceptionList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Linking with the settings xml file in res/layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
//        Intent mainIntent = getIntent();

        //Shared preferences that store the exception list in an xml file
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        //Get ListView and set adapter
        final ListView listView = (ListView)findViewById(R.id.exceptionListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, exceptionList);
        listView.setAdapter(adapter);

        //When view button is pressed, display the exception list
        //TODO link with the listview in settings.xml
        Button viewButton = (Button) findViewById(R.id.viewButton);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Retrieve exception list
                Map<String,?> keys = sharedpreferences.getAll();
                //Clear exception array list
                exceptionList.clear();

                for(Map.Entry<String,?> entry : keys.entrySet()){
                    //Debug output
                    Log.d("map values", entry.getKey() + ": " +
                            entry.getValue().toString());

                    //Add to contact names array list
                    exceptionList.add(entry.getKey());
                }

                //Notify list view adapter that changes were made, update list view
                adapter.notifyDataSetChanged();
            }
        });

        //When the add button is pressed, display contacts list for the user to add contacts to exception list
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Launch Contacts list for selecting contacts to be included into the exception list
                Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI); // //Contacts.CONTENT_URI
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        //Clear content of exception list when clear button has been pressed
        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear exception list
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
            }
        });

    }

    //Receiving data through onActivityResult
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT) :
                //Check that the user chose a valid contact from the list
                if (resultCode == Activity.RESULT_OK) {
                    getContactData(data);
                }
                break;
        }
    }

    //From the contact pick, analyze the data and place into exception list
    public void getContactData(Intent data){
        // Get the URI that points to the selected contact
        Uri contactData = data.getData();
        Cursor cursor =  getContentResolver().query(contactData, null, null, null, null);

        //Retrieve contacts name and store
        if (cursor.moveToFirst()) {
            //Get name of contact
            String exceptionContactsName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            //Get ID, to be used for the phone number query
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            //Phone numbers are stored in their own table, and need to be queried separately!
            //Check that the contact that the user picked actually has a phone number
            if (Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                //Query for the phone number
                Cursor phoneNumberCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{contactId}, null);

                if (phoneNumberCursor.moveToFirst()) {
                    //Get the contacts phone number
                    String exceptionContactsNumber = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    //Replace +64 with 0 before storing because incoming numbers don't say +64 (To talk with 다운이)
                    exceptionContactsNumber = exceptionContactsNumber.replaceAll("\\+64", "0");

                    //Remove all other symbols (The way it is stored is different for each phone)
                    exceptionContactsNumber = exceptionContactsNumber.replaceAll("[^a-zA-Z0-9\\s]", "");
                    exceptionContactsNumber = exceptionContactsNumber.replaceAll(" ","");

                    //Store data into the SharedPreferences
                    //Retrieve Shared Preferences
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(exceptionContactsName, exceptionContactsNumber);
                    editor.commit();
                }
                //Close phone number cursor
                phoneNumberCursor.close();
            }
            //Close main cursor
            cursor.close();
        }
    }
}
