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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;
    TextView textStatus;

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
        DAOkecerahanLampu daoKecerahanLampu = new DAOkecerahanLampu();

        loadData(); //Akan load data dengan shared preferences
        kondisi(state); //Setelah data diload, akan menerapkan data tersebut
        seekbarLampu.setProgress(nilai); //menerapkan nilai seekbar
        ambilDataKecerahan(); //mengambil data kecerahan dari firebase
        ambilDataKondisi();//mengambil data kondisi dari firebase


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
            int kecerahan = seekBar.getProgress(); //akan mengambil value dari seekbar
            pushKecerahanLampu(daoKecerahanLampu, dataKecerahanLampu(kecerahan)); //mengirimkan data ke firebase
            kecerahan(kecerahan); //mengatur kecerahan lampu dari data kecerahan yang sudah diambil sebelumnya
        }
        }
    );

        //listener button
        button.setOnClickListener(v -> {
            state = button.isSelected(); //mencari tau kondisi button
            pushKondisiLampu(daoKondisiLampu, dataKondisiLampu(state)); //mengirimkan data button ke firebase
            kondisi(state); //membalikkan posisi button dan seekbar
        });
    }


    //function untuk menympan kondisi button dengan shared preferences
    private void saveDataKondisi(Boolean state) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE); //membuat sharedpref dengan mode private
        SharedPreferences.Editor editor = sharedPreferences.edit(); //memberikan hak edit ke shared pref
        editor.putBoolean(buttonLampu, state); //menyimpan kondisi boolean ke shared pref
        Log.d("simpan", "Berhasil simpan");
        Log.d("simpan", Boolean.toString(state));
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
        state = sharedPreferences.getBoolean(buttonLampu, false); //ketika aplikasi pertama dijalankan, defalt akan ke false
        nilai = sharedPreferences.getInt(nilaiLampu, 0); //ketika aplikasi pertama dijalankan, default kecerahan akan ke 0
    }

    //fungsi untuk mengatur seekbar kecerahan
    private void kecerahan(int kecerahan){
        seekbarLampu.setProgress(kecerahan); //mengatur kecerahan lampu
        saveDataKecerahan(kecerahan); //save kecerahan lampu ke shared pref
    }

    //fungsi ketika button ditekan
    private void kondisi(Boolean state){

        if(state){ //jika true
            DrawableCompat.setTint(draw, Color.BLUE); //set warna button ke warna yang sudah ditentukan
            textStatus.setText("Nyala");//set text view
            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.teal_700)); //set warna halaman ke warna yang sudah ditentukan
            seekbarLampu.setProgressTintList(null); //mengatur warna seekbar ke null
            seekbarLampu.setThumbTintList(null); //mengatur warna thum ke null
        }
        else{
            DrawableCompat.setTint(draw, Color.GRAY);
            textStatus.setText("Mati");
            DrawableCompat.setTint(drawBackground, ContextCompat.getColor(this, R.color.TurnOff));
            seekbarLampu.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            seekbarLampu.setThumbTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        saveDataKondisi(state); //memanggil fungsi save kondisi
        button.setSelected(!state); //mengatur state button agar menjadi sebaliknya
        seekbarLampu.setEnabled(state); //mengatur state seekbar menjadi sesuai state
    }

    //memgambil kondisi lampu
    private kondisiLampu dataKondisiLampu(Boolean state){
        return new kondisiLampu(state);
    }
    //mengambil kondisi kecerahan
    private kecerahanLampu dataKecerahanLampu(int kecerahan){
        return new kecerahanLampu(kecerahan);
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
                kondisi(kond);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}