package sprinwood.medconnectdoc;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    TextView tvNameDoc;
    TextView tvExperience;
    TextView tvInfo;
    ArrayList<Switch> schFilters;
    Button btnUpdateFilters;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /*
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);*/

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        tvNameDoc = (TextView) findViewById(R.id.tvNameDoc);
        tvExperience = (TextView) findViewById(R.id.tvExperienceProfile);
        tvInfo    = (TextView) findViewById(R.id.tvInfoProfile);
        btnUpdateFilters = (Button) findViewById(R.id.btnUpdateFiltersProfile);

        schFilters = new ArrayList<Switch>();
        schFilters.add((Switch) findViewById(R.id.schFilter1Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter2Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter3Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter4Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter5Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter6Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter7Profile));
        schFilters.add((Switch) findViewById(R.id.schFilter8Profile));



        database = FirebaseDatabase.getInstance();
        DatabaseReference doctor = database.getReference("Doctors/" + String.valueOf(user.getUid()));
        doctor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dsp : dataSnapshot.getChildren()){
                    if(String.valueOf(dsp.getKey()).equals("name")){
                        tvNameDoc.setText(String.valueOf(dsp.getValue()));
                    } else
                    if(String.valueOf(dsp.getKey()).equals("info")){
                        tvInfo.setText(String.valueOf(dsp.getValue()));
                    } else
                    if(String.valueOf(dsp.getKey()).equals("experience")){
                        tvExperience.setText("Опыт: " + String.valueOf(dsp.getValue()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

        DatabaseReference filters = database.getReference("Filters/" + String.valueOf(user.getUid()));
        filters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot dsp : dataSnapshot.getChildren()) {
                    schFilters.get(i).setText(String.valueOf(dsp.getKey()));
                    schFilters.get(i).setChecked(Boolean.valueOf(String.valueOf(dsp.getValue())));
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });


        btnUpdateFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> filtMap = new HashMap<String, Object>();
                for(Switch sch : schFilters){
                    filtMap.put(String.valueOf(sch.getText()), sch.isChecked());
                }

                DatabaseReference filters = database.getReference("Filters/" + String.valueOf(user.getUid()));
                filters.updateChildren(filtMap);
                Toast.makeText(getApplicationContext(), "Фильтры обновлены ",Toast.LENGTH_SHORT).show();

                DatabaseReference Accepts = database.getReference("Accepted");
                Accepts.child(String.valueOf(user.getUid())).removeValue();

                DatabaseReference doctor = database.getReference("Doctors/" + user.getUid());
                HashMap<String, Object> m = new HashMap<String, Object>();
                m.put("numChats", 0);
                doctor.updateChildren(m);
            }
        });
    }
}
