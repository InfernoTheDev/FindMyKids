package com.findmykid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    EditText search_box;
    ImageButton btn_search;
    Spinner dropDown;
    Bundle bundle;
    String typeOfValue = "";


    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search_box = (EditText) findViewById(R.id.search_box);
        btn_search = (ImageButton) findViewById(R.id.btn_search);
        dropDown = (Spinner) findViewById(R.id.dropdown);
        btn_search.setOnClickListener(this);

        ArrayList<String> dropDownChoices = new ArrayList<>();
        dropDownChoices.add("IP");
        dropDownChoices.add("Lat/Lng");

        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, dropDownChoices);

        dropDown.setAdapter(dropDownAdapter);

        dropDown.setOnItemSelectedListener(this);

        bundle = new Bundle();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getItemAtPosition(i).toString().equalsIgnoreCase("ip")) {
            typeOfValue = "ip";
        } else {
            typeOfValue = "latlng";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        if (view == btn_search) {
            Log.d(TAG, "onClick: btn_search");

            if (search_box.getText().toString().equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("");
                alertDialog.setMessage("กรุณากรอกข้อมูล");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                return;
            }

            String value = search_box.getText().toString();

            if (typeOfValue.equalsIgnoreCase("ip")) {
                bundle.putString("search_type", "ip");
            } else {
                bundle.putString("search_type", "latlng");

                String seperateValue[];
                seperateValue = value.split(",");

                value = "{ lat: " + seperateValue[0] + ", lng: " + seperateValue[1] + " }";

                try {
                    JSONObject jsonObject = new JSONObject(value);
                    value = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            bundle.putString("value", value);

            FragmentMap fragmentMap = new FragmentMap();

            fragmentMap.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragmentMap)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
