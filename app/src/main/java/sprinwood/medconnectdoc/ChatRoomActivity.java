package sprinwood.medconnectdoc;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.print.PrintAttributes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.Objects;

public class ChatRoomActivity extends AppCompatActivity {
    private String idChat;
    private String doctorFio;
    private String patientFio;
    private String patientId;
    private ListView lvMessages;
    private ImageView ivSendMessage;
    private ArrayList<String[]> infoMess;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private int lastNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();

        Intent currIntent = getIntent();
        idChat = currIntent.getStringExtra("idChat");
        doctorFio = currIntent.getStringExtra("doctorFio");
        patientFio = currIntent.getStringExtra("patientFio");
        patientId = currIntent.getStringExtra("patientId");

        ivSendMessage = (ImageView) findViewById(R.id.ivSendMessage);
        ivSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MYTAG", "kek");
                EditText etSendingMessage = (EditText) findViewById(R.id.etSendingMessage);
                String text = String.valueOf(etSendingMessage.getText());
                etSendingMessage.setText("");
                if(!(text.equals(""))) {
                    HashMap<String, Object> sendMess = new HashMap<String, Object>();

                    sendMess.put("sender", "doctor");
                    sendMess.put("text", text);

                    database.getReference("Messages/" + idChat + "/" + String.valueOf(lastNum)).updateChildren(sendMess);
                    database.getReference("Chats/" + idChat + "/isPatientRead").setValue(false);
                }
            }
        });

        DatabaseReference messages = database.getReference("Messages/" + idChat);
        messages.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastNum = 0;
                infoMess = new ArrayList<String[]>();
                ArrayList<String> tmp = new ArrayList<String>();
                for(DataSnapshot dsp : dataSnapshot.getChildren()){
                    tmp.add(String.valueOf(dsp.child("sender").getValue()));
                    tmp.add(String.valueOf(dsp.child("text").getValue()));
                    lastNum++;
                    infoMess.add(new String[]{tmp.get(0), tmp.get(1)});
                    tmp.clear();
                    Log.d("MYTAG",String.valueOf(lastNum));
                }
                lvMessages = (ListView) findViewById(R.id.lvMessages);

                ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(getBaseContext(),
                        R.layout.list_item_message, R.id.tvItemMessage, infoMess) {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public View getView(final int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        String[] entry = infoMess.get(position);
                        TextView tvItemMessage = (TextView) view.findViewById(R.id.tvItemMessage);
                        if(entry[0].equals("doctor")){
                            tvItemMessage.setBackground( getResources().getDrawable(R.drawable.rounded_corner));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.FILL_PARENT);
                            params.gravity = Gravity.RIGHT;
                            params.setMargins(15,15,15,15);
                            tvItemMessage.setLayoutParams(params);
                        } else {
                            tvItemMessage.setBackground( getResources().getDrawable(R.drawable.rounded_corner1));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.FILL_PARENT);
                            params.gravity = Gravity.LEFT;
                            params.setMargins(20,20,20,20);
                            tvItemMessage.setLayoutParams(params);
                        }
                        tvItemMessage.setText(entry[1]);
                        tvItemMessage.setPadding(16,16,16,16);
                        return view;
                    }
                };
                lvMessages.setAdapter(adapter);
                lvMessages.setDivider(null);
                lvMessages.setDividerHeight(0);
                lvMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        lvMessages.setSelection(lvMessages.getCount() - 1);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), TabsActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
