package sprinwood.medconnectdoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import java.util.Objects;

public class PatietsActivity extends AppCompatActivity {
    ListView lvPatients;
    Button btnAccept;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    HashMap<String,String> filtersDoc;
    ArrayList<String> patientIds;
    ArrayList<String> patientNames;
    ArrayList<String> patientsAccept;
    ArrayList<String> patientsAcceptName;
    ArrayList<String> patientsAcceptAndNotUsed;//id
    ArrayList<String> patientsAcceptAndNotUsedName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patiets);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        lvPatients = (ListView) findViewById(R.id.lvPatients);

        database = FirebaseDatabase.getInstance();
        DatabaseReference doctorFilters = database.getReference("Filters/" + String.valueOf(user.getUid()));

        doctorFilters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                filtersDoc = new HashMap<String, String>();
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    filtersDoc.put(String.valueOf(dsp.getKey()), String.valueOf(dsp.getValue()));
                }
                DatabaseReference patiensFilters = database.getReference("PatientFilters");
                patiensFilters.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        patientIds = new ArrayList<String>();
                        boolean f = true;
                        for (DataSnapshot dsp1 : dataSnapshot.getChildren()) {
                            for (DataSnapshot dsp2 : dsp1.getChildren()) {
                                if (filtersDoc.get(String.valueOf(dsp2.getKey())).equals("false") &&
                                        String.valueOf(dsp2.getValue()).equals("true")) {
                                    f = false;
                                    break;
                                }
                            }
                            if (f) {
                                patientIds.add(String.valueOf(dsp1.getKey()));
                            }
                            f = true;
                        }
                        patientNames = new ArrayList<String>();
                        final DatabaseReference patiens = database.getReference("Patients");
                        patiens.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    for (String id : patientIds) {
                                        if (id.equals(String.valueOf(dsp.getKey()))) {
                                            String name = String.valueOf(dsp.child("name").getValue());
                                            patientNames.add(name);
                                        }
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                                        R.layout.list_item_checkbox, R.id.chbPatient, patientNames) {
                                    public View getView(final int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        String entry = patientNames.get(position);
                                        CheckBox chbPat = (CheckBox) view.findViewById(R.id.chbPatient);
                                        Button btnInfo = (Button) view.findViewById(R.id.btnInfoPatient);
                                        btnInfo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(getBaseContext(), PatientInfoActivity.class);
                                                intent.putExtra("idPatient", patientIds.get(position));
                                                startActivity(intent);
                                            }
                                        });
                                        chbPat.setText(entry);
                                        return view;
                                    }
                                };
                                lvPatients.setAdapter(adapter);

                                final DatabaseReference patiensAccepted = database.getReference("Accepted/" +
                                    String.valueOf(user.getUid()));
                                patiensAccepted.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                            for(int i = 0; i < patientIds.size(); i++){
                                                if(String.valueOf(dsp.getValue()).equals(patientIds.get(i))) {
                                                    View v = getViewByPosition(i, lvPatients);
                                                    CheckBox chbPat = (CheckBox) v.findViewById(R.id.chbPatient);
                                                    chbPat.setBackgroundColor(Color.GREEN);
                                                    chbPat.setChecked(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("The read failed: ", databaseError.getMessage());
                                    }
                                });

                                btnAccept = (Button) findViewById(R.id.btnAccept);
                                btnAccept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    patientsAccept = new ArrayList<String>();
                                    patientsAcceptName = new ArrayList<String>();
                                    for (int i = 0; i < patientIds.size(); i++) {
                                        View view = getViewByPosition(i, lvPatients);
                                        CheckBox chbPat = (CheckBox) view.findViewById(R.id.chbPatient);
                                        if (chbPat.isChecked()) {
                                            chbPat.setBackgroundColor(Color.GREEN);
                                            patientsAccept.add(patientIds.get(i));
                                            patientsAcceptName.add(patientNames.get(i));
                                        }
                                    }
                                    DatabaseReference Accepted = database.getReference("Accepted/" + user.getUid());
                                    Accepted.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            patientsAcceptAndNotUsed = new ArrayList<String>();
                                            patientsAcceptAndNotUsedName = new ArrayList<String>();
                                            boolean f = true;
                                            for(int i = 0; i < patientsAccept.size(); i++){
                                                for(DataSnapshot dsp : dataSnapshot.getChildren()) {
                                                    if (String.valueOf(dsp.getValue()).equals(patientsAccept.get(i))) {
                                                        f = false;
                                                        break;
                                                    }
                                                }
                                                if(f){
                                                    patientsAcceptAndNotUsed.add(patientsAccept.get(i));
                                                    patientsAcceptAndNotUsedName.add(patientsAcceptName.get(i));
                                                }
                                                f = true;
                                            }
                                            DatabaseReference base = database.getReference();
                                            for (String id : patientsAcceptAndNotUsed) {
                                                base.child("Accepted").child(String.valueOf(user.getUid())).push().setValue(id);
                                            }
                                            DatabaseReference numChatsDoc = database.getReference("Doctors/" +
                                                    String.valueOf(user.getUid()) + "/numChats");
                                            numChatsDoc.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    int count = Integer.valueOf(String.valueOf(dataSnapshot.getValue()));
                                                    count += patientsAcceptAndNotUsed.size();
                                                    database.getReference("Doctors/" + String.valueOf(user.getUid())
                                                            + "/numChats").setValue(count);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Log.e("The read failed: ", databaseError.getMessage());
                                                }
                                            });

                                            DatabaseReference doctor = database.getReference("Doctors/" + user.getUid() + "/name");
                                            doctor.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    ArrayList<String> idChats = new ArrayList<String>();
                                                    for(int i = 0; i < patientsAcceptAndNotUsed.size(); i++) {
                                                        HashMap<String, Object> chat = new HashMap<String, Object>();
                                                        chat.put("doctorId", user.getUid());
                                                        chat.put("doctorFio", String.valueOf(dataSnapshot.getValue()));
                                                        chat.put("isDoctorRead", false);
                                                        chat.put("isPatientRead", false);
                                                        chat.put("patientId", patientsAcceptAndNotUsed.get(i));
                                                        chat.put("patientFio", patientsAcceptAndNotUsedName.get(i));
                                                        DatabaseReference chatDatabase = database.getReference("Chats").push();
                                                        chatDatabase.updateChildren(chat);
                                                        idChats.add(chatDatabase.getKey());
                                                    }
                                                    DatabaseReference chatsDoctor = database.getReference("DoctorChatList").child(user.getUid());
                                                    for(String idChat : idChats){
                                                        chatsDoctor.push().setValue(idChat);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Log.e("The read failed: ", databaseError.getMessage());
                                                }
                                            });

                                            Toast.makeText(getApplicationContext(), "Пациенты добавлены",Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("The read failed: ", databaseError.getMessage());
                                        }
                                    });/*
                                        Log.e("MYTAG", "kek");
                                        Activity context = PatietsActivity.this;
                                        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup);
                                        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        View layout = layoutInflater.inflate(R.layout.activity_patient_info, viewGroup);
                                        final PopupWindow popup = new PopupWindow(context);
                                        popup.setContentView(layout);
                                        popup.setWidth(600);
                                        popup.setHeight(800);
                                        popup.setFocusable(true);

                                        // Clear the default translucent background
                                        popup.setBackgroundDrawable(new BitmapDrawable());

                                        // Displaying the popup at the specified location, + offsets.
                                        popup.showAtLocation(layout, Gravity.CENTER, 50, 50);

                                        // Getting a reference to Close button, and close the popup when clicked.
                                        Button close = (Button) layout.findViewById(R.id.btnClosePatientInfo);
                                        close.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                popup.dismiss();
                                            }
                                        });*/
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("The read failed: ", databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("The read failed: ", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
