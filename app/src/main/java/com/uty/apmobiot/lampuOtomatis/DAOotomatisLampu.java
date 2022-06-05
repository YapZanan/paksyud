package com.uty.apmobiot.lampuOtomatis;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uty.apmobiot.lampuOtomatis.otomatisLampu;

public class DAOotomatisLampu {
    private DatabaseReference databaseReference;
    public DAOotomatisLampu(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(otomatisLampu.class.getSimpleName());
    }
    public Task<Void> add(otomatisLampu otomatis){
        return databaseReference.child("/").setValue(otomatis);
    }
}
