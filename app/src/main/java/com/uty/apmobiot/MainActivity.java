package com.uty.apmobiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.slider.Slider;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

public class MainActivity extends AppCompatActivity {

    VerticalSeekBar slider;
    Button button;
    Drawable draw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        slider = findViewById(R.id.button1);
////        slider.addOnSliderTouchListener(touch);
//        slider.setOnProgressChangeListener(touchListener);
        button = findViewById(R.id.buttonLampu);
        draw = button.getBackground();
        draw = DrawableCompat.wrap(draw);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean aa = button.isSelected();
                if(aa){
                    Log.d("aaa", "bb");
                    DrawableCompat.setTint(draw, Color.BLUE);
                }
                else{
                    Log.d("cccc", "ddd");
                    DrawableCompat.setTint(draw, Color.GRAY);
                }

                button.setSelected(!aa);

            }
        });
    }


}