package org.christmann.medievalbackend;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ToolsActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference dbRef;

    Button createButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "ToolsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        Log.e(TAG, "Entered Tools Activity");

        setupAuthListener();

        database = FirebaseDatabase.getInstance();

        createButton = (Button) findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createButton();
            }
        });

    }

    public void setupAuthListener(){
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public Enemy createEnemy(){
        Enemy enemy = new Enemy();

        EditText name = (EditText) findViewById(R.id.nameEditText);
        enemy.setName(name.getText().toString());

        EditText level = (EditText) findViewById(R.id.levelEditText);
        enemy.setLevel(Integer.parseInt(level.getText().toString()));

        EditText atk = (EditText) findViewById(R.id.atkEditText);
        enemy.setAtk(Integer.parseInt(atk.getText().toString()));

        EditText def = (EditText) findViewById(R.id.defEditText);
        enemy.setDef(Integer.parseInt(def.getText().toString()));

        EditText spd = (EditText) findViewById(R.id.spdEditText);
        enemy.setSpd(Integer.parseInt(spd.getText().toString()));

        EditText lat = (EditText) findViewById(R.id.latEditText);
        enemy.setLat(Float.parseFloat(lat.getText().toString()));

        EditText lng = (EditText) findViewById(R.id.lngEditText);
        enemy.setLng(Float.parseFloat(lng.getText().toString()));

        enemy.setMaxhp(20);
        enemy.setCurrentHP(20);

        return enemy;
    }

    public void createButton(){
        Log.e(TAG, "Button Pressed");

        Enemy enemy_to_write = createEnemy();

        dbRef = database.getReference("enemies/" + enemy_to_write.getName());
        dbRef.setValue(enemy_to_write, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.e(TAG, "Enemy could not be saved. " + databaseError.getMessage());
                } else {
                    Log.e(TAG, "Enemy saved successfully.");
                }
            }
        });
    }

}
