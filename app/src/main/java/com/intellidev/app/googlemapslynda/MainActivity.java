package com.intellidev.app.googlemapslynda;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;

public class MainActivity extends AppCompatActivity {

    Button btnGoToMap;

    //TODO [100] checking for the google play service apk

    //Todo [101]
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnGoToMap = findViewById(R.id.btn_go_to_map);

        btnGoToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(servicesOk()){
                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    //Todo [102]
    public boolean servicesOk () {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable))
        {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,this,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "can't connect to mapping service", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
