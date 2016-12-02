package sprinwood.medconnectdoc;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientInfoActivity extends AppCompatActivity {
    TextView tvNamePat;
    TextView tvInfoPat;
    String idPat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        Intent intent = getIntent();

        idPat = intent.getStringExtra("idPatient");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference patient = database.getReference("Patients/" + idPat);
        patient.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvNamePat = (TextView) findViewById(R.id.tvNamePatient);
                tvInfoPat = (TextView) findViewById(R.id.tvInfoPatient);
                tvNamePat.setText(String.valueOf(dataSnapshot.child("name").getValue()));
                tvInfoPat.setText(String.valueOf(dataSnapshot.child("info").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), TabsActivity.class);
        myIntent.putExtra("act" , "2");
        startActivityForResult(myIntent, 0);
        return true;
    }
}
