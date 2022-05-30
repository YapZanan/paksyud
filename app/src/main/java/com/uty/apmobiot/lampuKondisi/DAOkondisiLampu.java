package com.uty.apmobiot.lampuKondisi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOkondisiLampu {
    private DatabaseReference databaseReference;
    public DAOkondisiLampu(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(kondisiLampu.class.getSimpleName());
    }
    public Task<Void> add(kondisiLampu kond){
        return databaseReference.child("/").setValue(kond);
    }
}
