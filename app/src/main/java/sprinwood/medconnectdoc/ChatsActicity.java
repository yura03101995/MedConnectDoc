package sprinwood.medconnectdoc;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsActicity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    ListView lvChats;
    FirebaseDatabase database;
    ArrayList<String> idChats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_acticity);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();

        DatabaseReference chatsDoctor = database.getReference("DoctorChatList/" + user.getUid());
        chatsDoctor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idChats = new ArrayList<String>();
                for(DataSnapshot dsp : dataSnapshot.getChildren()){
                    idChats.add(String.valueOf(dsp.getValue()));
                }
                DatabaseReference chats = database.getReference("Chats");
                chats.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ArrayList<String[]> forAdapter = new ArrayList<String[]>();
                        ArrayList<String> tmp = new ArrayList<String>();
                        for(String id : idChats){
                            //Log.d("MYTAG", id);
                            tmp.add(String.valueOf(dataSnapshot.child(id).child("doctorFio").getValue()));
                            tmp.add(String.valueOf(dataSnapshot.child(id).child("isDoctorRead").getValue()));
                            tmp.add(String.valueOf(dataSnapshot.child(id).child("isPatientRead").getValue()));
                            tmp.add(String.valueOf(dataSnapshot.child(id).child("patientFio").getValue()));
                            tmp.add(String.valueOf(dataSnapshot.child(id).child("patientId").getValue()));
                            tmp.add(id);
                            forAdapter.add(new String[]{tmp.get(0),tmp.get(1),tmp.get(2),tmp.get(3),tmp.get(4),tmp.get(5)});
                            tmp.clear();
                        }
                        ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(getBaseContext(),
                                R.layout.list_item_chat, R.id.tvNamePatientItemChats, forAdapter) {
                            public View getView(final int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                String[] entry = forAdapter.get(position);
                                TextView tvNamePatient = (TextView) view.findViewById(R.id.tvNamePatientItemChats);
                                tvNamePatient.setText(entry[3]);
                                final TextView tvLastMessage = (TextView) view.findViewById(R.id.tvLastMessageItemChats);
                                final String idChat = entry[5];
                                DatabaseReference messages = database.getReference("Messages");
                                messages.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(!(dataSnapshot.hasChild(idChat))){
                                            tvLastMessage.setText("Нет сообщений");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("The read failed: ", databaseError.getMessage());
                                    }
                                });
                                if(entry[1].equals("false")){
                                    LinearLayout llOnItemChats = (LinearLayout) view.findViewById(R.id.llOnChatsItem);
                                    llOnItemChats.setBackgroundColor(Color.YELLOW);
                                }
                                return view;
                            }
                        };
                        lvChats = (ListView) findViewById(R.id.lvChats);
                        lvChats.setAdapter(adapter);
                        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                database.getReference("Chats/" + forAdapter.get(position)[5] + "/isDoctorRead").setValue(true);
                                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                intent.putExtra("idChat",forAdapter.get(position)[5]);
                                intent.putExtra("doctorFio",forAdapter.get(position)[0]);
                                intent.putExtra("patientFio",forAdapter.get(position)[3]);
                                intent.putExtra("patientId",forAdapter.get(position)[4]);
                                startActivity(intent);
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
}
