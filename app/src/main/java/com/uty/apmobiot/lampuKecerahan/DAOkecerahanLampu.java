package com.uty.apmobiot.lampuKecerahan;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOkecerahanLampu {
    private DatabaseReference databaseReference;
    public DAOkecerahanLampu(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(kecerahanLampu.class.getSimpleName());
    }
    public Task<Void> add(kecerahanLampu kecerahan){
        return databaseReference.child("/").setValue(kecerahan);
    }
}