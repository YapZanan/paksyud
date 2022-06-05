package com.uty.apmobiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
import android.widget.TextView;
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

import com.uty.apmobiot.lampuOtomatis.DAOotomatisLampu;
import com.uty.apmobiot.lampuOtomatis.otomatisLampu;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;

    TextView textStatus, textViewKecepatan;



    SwitchCompat switchOtomatis;

    SeekBar seekbarLampu;
    Button button;
    Drawable draw, drawseekBarLampu, seekBarOn, seekBarOff, drawBackground;
    boolean stateButton = false;
    boolean stateOtomatis = false;
    int nilai = 0;
    DatabaseReference dref;

    public static final String SHARED_PREFS = "sharedPref";
    public static final String buttonLampu = "KondisiButton";
    public static final String buttonOtomatis = "KondisiOtomatis";
    public static final String nilaiLampu = "nilaiLampu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewKecepatan = findViewById(R.id.textViewKecepatan);
        textViewKecepatan.setText("0");

        seekBarOn = ContextCompat.getDrawable(this, R.drawable.seekbar_background_on);
        seekBarOff = ContextCompat.getDrawable(this, R.drawable.seekbar_background_off);

        switchOtomatis = findViewById(R.id.switchOtomatis);

        button = findViewById(R.id.buttonLampu);
        seekbarLampu = findViewById(R.id.seekbarLampu);

        textStatus = findViewById(R.id.textViewStatus);

        seekbarLampu.setProgressDrawable(seekBarOn);

        layout = findViewById(R.id.backgroundUtama);
        drawBackground = layout.getBackground();
        drawBackground = DrawableCompat.wrap(drawBackground);

        draw = button.getBackground();
        draw = DrawableCompat.wrap(draw);

        drawseekBarLampu = seekbarLampu.getBackground();
        drawseekBarLampu = DrawableCompat.wrap(drawseekBarLampu);

        DAOkondisiLampu daoKondisiLampu = new DAOkondisiLampu();
        DAOotomatisLampu daoOtomatisLampu = new DAOotomatisLampu();
        DAOkecerahanLampu daoKecerahanLampu = new DAOkecerahanLampu();

        loadData(); //Akan load data dengan shared preferences
        kondisiButton(stateButton); //Setelah data diload, akan menerapkan data tersebut\
        Log.d("state", Boolean.toString(stateOtomatis));
        switchOtomatis(stateOtomatis, stateButton);
        seekbarLampu.setProgress(nilai); //menerapkan nilai seekbar
        ambilDataKecerahan(); //mengambil data kecerahan dari firebase
        ambilDataKondisi();//mengambil data kondisi dari firebase
        ambilDataOtomatis();


        //listener ketika seekbar ditekan
        seekbarLampu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                                                    @Override
                                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                    }

                                                    @Override
                                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                                    }


                                                    //listener ketika seekbar dilepaskan
                                                    @Override
                                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                                        if(!stateOtomatis){
                                                            int kecerahan = seekBar.getProgress(); //akan mengambil value dari seekbar
                                                            pushKecerahanLampu(daoKecerahanLampu, dataKecerahanLampu(kecerahan)); //mengirimkan data ke firebase
                                                            textViewKecepatan.setText(Integer.toString(kecerahan)); //mengirimkan
                                                            kecerahan(kecerahan); //mengatur kecerahan lampu dari data kecerahan yang sudah diambil sebelumnya
                                                        }
                                                    }
                                                }
        );

        //listener button
        button.setOnClickListener(v -> {
            stateButton = button.isSelected(); //mencari tau kondisi button
            pushKondisiLampu(daoKondisiLampu, dataKondisiLampu(stateButton)); //mengirimkan data button ke firebase
            kondisiButton(stateButton); //membalikkan posisi button dan seekbar
        });

        switchOtomatis.setOnClickListener(v -> {
            stateOtomatis = switchOtomatis.isChecked();
            Log.d("state", Boolean.toString(stateOtomatis));

            switchOtomatis(stateOtomatis, stateButton);
            saveDataOtomatis(stateOtomatis);

            pushOtomatisLampu(daoOtomatisLampu, dataOtomatisLampu(stateOtomatis));
        });
    }

    private void updateKecerahan(){

    }

    private void switchOtomatis(Boolean stateOtomatis, Boolean stateButton){

        switchOtomatis.setChecked(stateOtomatis);

        if(stateOtomatis && stateButton){
            switchOtomatis.setTextOn("Otomatis");
            seekbarLampu.setEnabled(false);
            seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        else if(!stateOtomatis && stateButton){
            switchOtomatis.setTextOff("Manual");
            seekbarLampu.setEnabled(true);
            seekbarLampu.setProgressTintList(null); //mengatur warna seekbar ke null
            seekbarLampu.setThumbTintList(null); //mengatur warna thum ke null
        }

    }


    //function untuk menympan kondisi button dengan shared preferences
    private void saveDataKondisi(Boolean state) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE); //membuat sharedpref dengan mode private
        SharedPreferences.Editor editor = sharedPreferences.edit(); //memberikan hak edit ke shared pref
        editor.putBoolean(buttonLampu, state); //menyimpan kondisi boolean ke shared pref
        editor.apply(); //apply shared pref
    }

    private void saveDataOtomatis(Boolean state) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE); //membuat sharedpref dengan mode private
        SharedPreferences.Editor editor = sharedPreferences.edit(); //memberikan hak edit ke shared pref
        editor.putBoolean(buttonOtomatis, state); //menyimpan kondisi boolean ke shared pref
        editor.apply(); //apply shared pref
    }

    //function untuk menympan data kecerahan dengan shared preferences
    private void saveDataKecerahan(int kecerahan) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(nilaiLampu, kecerahan); //menyimpan data kecerahan ke shared pref
        Log.d("simpan", "Berhasil simpan");
        Log.d("simpan", Integer.toString(kecerahan));
        editor.apply();
    }

    //akan dipanggil ketika aplikasi pertama dijalankan, memanggil shared pref yang sudah tersimpan
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        stateButton = sharedPreferences.getBoolean(buttonLampu, false); //ketika aplikasi pertama dijalankan, defalt akan ke false
        stateOtomatis = sharedPreferences.getBoolean(buttonOtomatis, false); //ketika aplikasi pertama dijalankan, defalt akan ke false
        nilai = sharedPreferences.getInt(nilaiLampu, 0); //ketika aplikasi pertama dijalankan, default kecerahan akan ke 0
    }

    //fungsi untuk mengatur seekbar kecerahan
    private void kecerahan(int kecerahan){
        seekbarLampu.setProgress(kecerahan); //mengatur kecerahan lampu
        saveDataKecerahan(kecerahan); //save kecerahan lampu ke shared pref
    }

    //fungsi ketika button ditekan
    private void kondisiButton(Boolean stateButton){

        if(stateButton){ //jika true
            DrawableCompat.setTint(draw, Color.BLUE); //set warna button ke warna yang sudah ditentukan
            textStatus.setText("Nyala");//set text view
            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.teal_700)); //set warna halaman ke warna yang sudah ditentukan
            switchOtomatis.setThumbTintList(null);
            switchOtomatis.setTrackTintList(null);
            switchOtomatis.setEnabled(true);
            if(stateOtomatis){
                seekbarLampu.setEnabled(false); //mengatur state seekbar menjadi sesuai state
                seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
                seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));
            }
            else{
                seekbarLampu.setEnabled(true); //mengatur state seekbar menjadi sesuai state
                seekbarLampu.setProgressTintList(null); //mengatur warna seekbar ke null
                seekbarLampu.setThumbTintList(null); //mengatur warna thum ke null
            }
        }
        else{
            DrawableCompat.setTint(draw, Color.GRAY);
            textStatus.setText("Mati");
            seekbarLampu.setEnabled(false); //mengatur state seekbar menjadi sesuai state
            seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));

            switchOtomatis.setEnabled(false); //mengatur state
            switchOtomatis.setThumbTintList(ColorStateList.valueOf(Color.GRAY));
            switchOtomatis.setTrackTintList(ColorStateList.valueOf(Color.GRAY));

            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.TurnOff));

        }
        saveDataKondisi(stateButton); //memanggil fungsi save kondisi
        button.setSelected(!stateButton); //mengatur state button agar menjadi sebaliknya
    }

    //memgambil kondisi lampu
    private kondisiLampu dataKondisiLampu(Boolean stateButton){
        return new kondisiLampu(stateButton);
    }

    private otomatisLampu dataOtomatisLampu(Boolean stateButton){
        return new otomatisLampu(stateButton);
    }

    //mengambil kondisi kecerahan
    private kecerahanLampu dataKecerahanLampu(int kecerahan){
        return new kecerahanLampu(kecerahan);
    }

    private void pushOtomatisLampu(DAOotomatisLampu dao, otomatisLampu kondOto){
        //memanggil fungsi add pada dao
        dao.add(kondOto).addOnSuccessListener(
                        //ketika sukses
                        suc -> Toast.makeText(this, "Berhasil masukkan Data Otomatis", Toast.LENGTH_SHORT).show())
                //ketika gagal
                .addOnFailureListener(
                        fail -> Toast.makeText(this, "Gagal masukkan Data Otomatis", Toast.LENGTH_SHORT).show());
    }


    //fungsi untuk memanggil dao kondisi lampu
    private void pushKondisiLampu(DAOkondisiLampu dao, kondisiLampu kond){
        //memanggil fungsi add pada dao
        dao.add(kond).addOnSuccessListener(
                        //ketika sukses
                        suc -> Toast.makeText(this, "Berhasil masukkan Data kondisi", Toast.LENGTH_SHORT).show())
                //ketika gagal
                .addOnFailureListener(
                        fail -> Toast.makeText(this, "Gagal masukkan Data kondisi", Toast.LENGTH_SHORT).show());
    }

    //fungsi untuk memanggil dao kondisi kecerahan lampu
    private void pushKecerahanLampu(DAOkecerahanLampu dao, kecerahanLampu kecer){
        dao.add(kecer).addOnSuccessListener(
                        suc -> Toast.makeText(this, "Berhasil masukkan Data kecerahan", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        fail -> Toast.makeText(this, "Gagal masukkan Data kecerahan", Toast.LENGTH_SHORT).show());
    }

    //mengambil data kecerahan ketika runtime
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
                textViewKecepatan.setText(Integer.toString(value));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //mengambil data kondisi ketika runtime
    private void ambilDataKondisi() {
        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aa = Objects.requireNonNull(snapshot.child("/kondisiLampu/kondisi").getValue()).toString();
                Toast.makeText(getApplicationContext(), "Berhasil Update Data kondisi", Toast.LENGTH_SHORT).show();
                boolean kond = Boolean.parseBoolean(aa);
                kondisiButton(kond);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ambilDataOtomatis() {
        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aa = Objects.requireNonNull(snapshot.child("/otomatisLampu/otomatis").getValue()).toString();
                Toast.makeText(getApplicationContext(), "Berhasil Update Data otomatis", Toast.LENGTH_SHORT).show();
                boolean kondOto = Boolean.parseBoolean(aa);
                switchOtomatis(kondOto, stateButton);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}