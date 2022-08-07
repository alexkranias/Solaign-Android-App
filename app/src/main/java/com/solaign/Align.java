package com.solaign;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Align extends AppCompatActivity {

    ImageView alignCircleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.align);

        alignCircleView = findViewById(R.id.align_circle);

        //Thread.sleep(1000);
        alignCircleView.setY(50);
        alignCircleView.setY(100);



    }

}
