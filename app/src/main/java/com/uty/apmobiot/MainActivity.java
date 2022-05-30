package com.uty.apmobiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.animation.Animator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.uty.apmobiot.lampuKondisi.DAOkondisiLampu;
import com.uty.apmobiot.lampuKondisi.kondisiLampu;

import com.uty.apmobiot.lampuKecerahan.DAOkecerahanLampu;
import com.uty.apmobiot.lampuKecerahan.kecerahanLampu;

public class MainActivity extends AppCompatActivity {

    SeekBar seekbarLampu;
    Button button;
    Drawable draw, drawseekBarLampu, seekBarOn, seekBarOff;
    boolean state = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBarOn = ContextCompat.getDrawable(this, R.drawable.seekbar_background_on);
        seekBarOff = ContextCompat.getDrawable(this, R.drawable.seekbar_background_off);
        button = findViewById(R.id.buttonLampu);
        seekbarLampu = findViewById(R.id.seekbarLampu);

        seekbarLampu.setProgressDrawable(seekBarOn);

        draw = button.getBackground();
        draw = DrawableCompat.wrap(draw);

        drawseekBarLampu = seekbarLampu.getBackground();
        drawseekBarLampu = DrawableCompat.wrap(drawseekBarLampu);

        DAOkondisiLampu daoKondisiLampu = new DAOkondisiLampu();
        DAOkecerahanLampu daoKecerahanLampu = new DAOkecerahanLampu();
        //pertama kali si lampu bakalan mati
        kondisi(state);



        seekbarLampu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int kecerahan = seekBar.getProgress();
                pushKecerahanLampu(daoKecerahanLampu, dataKecerahanLampu(kecerahan));
                Log.d("onProgressChanged", Integer.toString(kecerahan));
            }
        }
        );



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = button.isSelected();
                pushKondisiLampu(daoKondisiLampu, dataKondisiLampu(state));
                kondisi(state);
            }
        });
    }
    private void kondisi(Boolean state){
        if(state){
            Log.d("aaa", "bb");
            DrawableCompat.setTint(draw, Color.BLUE);
            seekbarLampu.setProgressTintList(null);
            seekbarLampu.setThumbTintList(null);
        }
        else{
            Log.d("cccc", "ddd");
            DrawableCompat.setTint(draw, Color.GRAY);
            seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        button.setSelected(!state);
        seekbarLampu.setEnabled(!seekbarLampu.isEnabled());
    }

    private kondisiLampu dataKondisiLampu(Boolean state){
        kondisiLampu kond = new kondisiLampu(state);
        return kond;
    }

    private void pushKondisiLampu(DAOkondisiLampu dao, kondisiLampu kond){
        dao.add(kond).addOnSuccessListener(suc ->{
            Toast.makeText(this, "Berhasil masukkan Data kondisi", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(fail ->{
            Toast.makeText(this, "Gagal masukkan Data kondisi", Toast.LENGTH_SHORT).show();
        });
    }

    private kecerahanLampu dataKecerahanLampu(int kecerahan){
        kecerahanLampu kecer = new kecerahanLampu(kecerahan);
        return kecer;
    }

    private void pushKecerahanLampu(DAOkecerahanLampu dao, kecerahanLampu kecer){
        dao.add(kecer).addOnSuccessListener(suc ->{
            Toast.makeText(this, "Berhasil masukkan Data kecerahan", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(fail ->{
            Toast.makeText(this, "Gagal masukkan Data kecerahan", Toast.LENGTH_SHORT).show();
        });
    }
}