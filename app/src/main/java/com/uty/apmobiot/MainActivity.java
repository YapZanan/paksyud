package com.uty.apmobiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.uty.apmobiot.lampuKondisi.DAOkondisiLampu;
import com.uty.apmobiot.lampuKondisi.kondisiLampu;

import com.uty.apmobiot.lampuKecerahan.DAOkecerahanLampu;
import com.uty.apmobiot.lampuKecerahan.kecerahanLampu;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;

    SeekBar seekbarLampu;
    Button button;
    Drawable draw, drawseekBarLampu, seekBarOn, seekBarOff, drawBackground;
    boolean state = false;
    int nilai = 0;
    DatabaseReference dref;

    public static final String SHARED_PREFS = "sharedPref";
    public static final String buttonLampu = "KondisiButton";
    public static final String nilaiLampu = "nilaiLampu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBarOn = ContextCompat.getDrawable(this, R.drawable.seekbar_background_on);
        seekBarOff = ContextCompat.getDrawable(this, R.drawable.seekbar_background_off);

        button = findViewById(R.id.buttonLampu);
        seekbarLampu = findViewById(R.id.seekbarLampu);

        seekbarLampu.setProgressDrawable(seekBarOn);

        layout = findViewById(R.id.backgroundUtama);
        drawBackground = layout.getBackground();
        drawBackground = DrawableCompat.wrap(drawBackground);

        draw = button.getBackground();
        draw = DrawableCompat.wrap(draw);

        drawseekBarLampu = seekbarLampu.getBackground();
        drawseekBarLampu = DrawableCompat.wrap(drawseekBarLampu);

        DAOkondisiLampu daoKondisiLampu = new DAOkondisiLampu();
        DAOkecerahanLampu daoKecerahanLampu = new DAOkecerahanLampu();

        loadData();
        //pertama kali si lampu bakalan mati
        kondisi(state);
        seekbarLampu.setProgress(nilai);
        ambilDataKecerahan();
        ambilDataKondisi();



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
                                                        kecerahan(kecerahan);
                                                    }
                                                }
        );



        button.setOnClickListener(v -> {
            state = button.isSelected();
            pushKondisiLampu(daoKondisiLampu, dataKondisiLampu(state));
            kondisi(state);
        });
    }

    private void saveDataKondisi(Boolean state) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(buttonLampu, state);
        Log.d("simpan", "Berhasil simpan");
        Log.d("simpan", Boolean.toString(state));
        editor.apply();
    }

    private void saveDataKecerahan(int kecerahan) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(nilaiLampu, kecerahan);
        Log.d("simpan", "Berhasil simpan");
        Log.d("simpan", Integer.toString(kecerahan));
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        state = sharedPreferences.getBoolean(buttonLampu, false);
        nilai = sharedPreferences.getInt(nilaiLampu, 0);
        Log.d("load", Boolean.toString(state));
    }

    private void kecerahan(int kecerahan){
        seekbarLampu.setProgress(kecerahan);
        saveDataKecerahan(kecerahan);
        Log.d("onProgressChanged", Integer.toString(kecerahan));

    }


    private void kondisi(Boolean state){

        if(state){
            Log.d("aaa", "bb");
            DrawableCompat.setTint(draw, Color.BLUE);

            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.teal_200));
            seekbarLampu.setProgressTintList(null);
            seekbarLampu.setThumbTintList(null);
        }
        else{
            Log.d("cccc", "ddd");
            DrawableCompat.setTint(draw, Color.GRAY);
            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.TurnOff));
            seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        saveDataKondisi(state);
        button.setSelected(!state);
        seekbarLampu.setEnabled(state);
    }

    private kondisiLampu dataKondisiLampu(Boolean state){
        return new kondisiLampu(state);
    }

    private void pushKondisiLampu(DAOkondisiLampu dao, kondisiLampu kond){
        dao.add(kond).addOnSuccessListener(
                        suc -> Toast.makeText(this, "Berhasil masukkan Data kondisi", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        fail -> Toast.makeText(this, "Gagal masukkan Data kondisi", Toast.LENGTH_SHORT).show());
    }

    private kecerahanLampu dataKecerahanLampu(int kecerahan){
        return new kecerahanLampu(kecerahan);
    }

    private void pushKecerahanLampu(DAOkecerahanLampu dao, kecerahanLampu kecer){
        dao.add(kecer).addOnSuccessListener(
                        suc -> Toast.makeText(this, "Berhasil masukkan Data kecerahan", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        fail -> Toast.makeText(this, "Gagal masukkan Data kecerahan", Toast.LENGTH_SHORT).show());
    }

    private void ambilDataKecerahan(){
        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aa = Objects.requireNonNull(snapshot.child("/kecerahanLampu/kecerahan").getValue()).toString();
                Toast.makeText(getApplicationContext(), "Berhasil Update Data kecerahan", Toast.LENGTH_SHORT).show();
                int value = Integer.parseInt(aa);
                Log.d("aa", aa);
                kecerahan(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ambilDataKondisi() {
        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aa = Objects.requireNonNull(snapshot.child("/kondisiLampu/kondisi").getValue()).toString();
                Toast.makeText(getApplicationContext(), "Berhasil Update Data kondisi", Toast.LENGTH_SHORT).show();
                boolean kond = Boolean.parseBoolean(aa);
                kondisi(kond);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}