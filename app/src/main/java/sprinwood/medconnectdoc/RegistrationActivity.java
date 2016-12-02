package sprinwood.medconnectdoc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    EditText etEmail;
    EditText etPass;
    EditText etRepPass;
    EditText etLastName;
    EditText etFirstName;
    EditText etMiddleName;
    EditText etExperience;
    EditText etInfo;
    Button btnRegistration;
    Button btnBackOnRegistraion;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        btnRegistration = (Button) findViewById(R.id.btnRegistrationOnRegistration);
        btnBackOnRegistraion = (Button) findViewById(R.id.btnBackOnRegistration);

        etEmail = (EditText) findViewById(R.id.etEmailOnRegistration);
        etPass = (EditText) findViewById(R.id.etPassOnRegistration);
        etRepPass = (EditText) findViewById(R.id.etRepPassOnRegistration);
        etLastName = (EditText) findViewById(R.id.etLastNameOnRegistration);
        etFirstName = (EditText) findViewById(R.id.etFirstNameOnRegistration);
        etMiddleName = (EditText) findViewById(R.id.etMiddleNameOnRegistration);
        etExperience = (EditText) findViewById(R.id.etExperienceOnRegistration);
        etInfo = (EditText) findViewById(R.id.etInfoOnRegistration);

        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("TAG", "onAuthStateChanged:signed_in:" + user.getUid());

                    String FIO = String.valueOf(etLastName.getText()) + " " +
                                String.valueOf(etFirstName.getText()) + " " +
                                String.valueOf(etMiddleName.getText());
                    /*
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(FIO).build();
                    user.updateProfile(profileUpdates);
                    */
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String uid = String.valueOf(user.getUid());
                    DatabaseReference Doctors = database.getReference("Doctors").child(uid);
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("name", FIO);
                    result.put("experience", String.valueOf(etExperience.getText()));
                    result.put("info", String.valueOf(etInfo.getText()));
                    result.put("email", String.valueOf(etEmail.getText()));
                    Doctors.updateChildren(result);

                    DatabaseReference Filters = database.getReference("Filters").child(uid);
                    HashMap<String, Object> filt = new HashMap<>();
                    filt.put("filter1", false);
                    filt.put("filter2", false);
                    filt.put("filter3", false);
                    filt.put("filter4", false);
                    filt.put("filter5", false);
                    filt.put("filter6", false);
                    filt.put("filter7", false);
                    filt.put("filter8", false);
                    Filters.updateChildren(filt);

                    Intent intent = new Intent(RegistrationActivity.this, TabsActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };
        btnBackOnRegistraion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),AuthActivity.class);
                startActivity(intent);
            }
        });

        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(etEmail.getText()).equals("") || String.valueOf(etPass.getText()).equals("")
                        || String.valueOf(etRepPass.getText()).equals("")){
                    Toast.makeText(getApplicationContext(), "Field 'E-mail' or 'Password' or 'Repeat Password' is empty",Toast.LENGTH_SHORT).show();
                }
                else if(!(String.valueOf(etRepPass.getText()).equals(String.valueOf(etPass.getText())))){
                    Toast.makeText(getApplicationContext(), "Field 'Password' or 'Repeat Password' is not equals",Toast.LENGTH_SHORT).show();
                }
                else if(String.valueOf(etLastName.getText()).equals("") || String.valueOf(etFirstName.getText()).equals("") ||
                        String.valueOf(etExperience.getText()).equals("") || String.valueOf(etInfo.getText()).equals("")){
                    Toast.makeText(getApplicationContext(), "Заполните ВСЕ поля(мб кроме отчества)",Toast.LENGTH_SHORT).show();
                }
                else{
                    String email = String.valueOf(etEmail.getText());
                    String password = String.valueOf(etPass.getText());
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("TAG", "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.e("TAG", "signInWithEmail:failed", task.getException());
                                    String str = String.valueOf(task.getException());
                                    String out = "";
                                    for(int i = 0; i < str.length(); i++){
                                        if(str.charAt(i) == '['){
                                            for(int j = i + 1; j < str.length(); j++){
                                                if(str.charAt(j) == ']'){
                                                    break;
                                                }
                                                out += str.charAt(j);
                                            }
                                            break;
                                        }
                                    }
                                    Toast.makeText(RegistrationActivity.this, "Failed: " + out,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

