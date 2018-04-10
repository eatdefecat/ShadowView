package com.android.dz.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.dz.shadow.ShadowView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShadowView.with(this, findViewById(R.id.shadow_layout)).initView();
    }


}
