package org.christmann.medievalbackend;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateEnemies extends AppCompatActivity {

    public final static String TAG = "GenerateEnemies";

    Button startBtn;
    EditText enemyNameET, levelRangeET, timeIntervalET;
    ProgressBar generatingIcon;

    Thread generationThread;                // the thread that will generate the enemies
    Random randomGenerator = new Random();

    boolean enemyGeneration = false;     // state of the enemy generation

    FirebaseDatabase database;
    DatabaseReference dbRef, enemiesRef;

    String enemyName;
    int min_level, max_level, time_interval;

    ArrayList<Character> onlineCharacters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_enemies);

        setupUI();

        getOnlineCharacters();
    }

    // setup interface elements and listeners
    private void setupUI(){
        enemyNameET = (EditText) findViewById(R.id.enemyNameET);
        levelRangeET = (EditText) findViewById(R.id.enemyLevelET);
        timeIntervalET = (EditText) findViewById(R.id.timeIntervalET);

        generatingIcon = (ProgressBar) findViewById(R.id.generatingIcon);

        startBtn = (Button) findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!enemyGeneration){
                    if(onlineCharacters.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Nobody is online",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startGeneration();
                    }
                } else {
                    stopGeneration();
                }
            }
        });
    }

    // starts generating enemies nearby players as specified in the fields
    @SuppressLint("NewApi")
    private void startGeneration(){
        enemyGeneration = true;     // sets enemy generation state to true
        startBtn.setText(getResources().getText(R.string.stop));
        generatingIcon.setVisibility(View.VISIBLE);

        // getting enemy name
        enemyName = enemyNameET.getText().toString();

        // getting min and max level
        String levelRange = levelRangeET.getText().toString();
        String[] levels = levelRange.split("-");
        min_level = Integer.parseInt(levels[0]);
        max_level = Integer.parseInt(levels[1]);

        // gets time and multiplier by 1000 to convert to seconds
        time_interval = Integer.parseInt(timeIntervalET.getText().toString()) * 1000;
        Log.i(TAG, Integer.toString(time_interval));

        generationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(enemyGeneration && onlineCharacters.size() > 0) {
                    String newEnemyName = enemyName + i;    // generate new unique name to avoid overwrite when writing to database
                    int level = ThreadLocalRandom.current().nextInt(min_level, max_level);  // generate random level between range

                    Enemy newEnemy = createEnemy(newEnemyName, level);  //gets new enemy to write to database
                    enemiesRef = database.getReference("enemies/" + newEnemy.getName());
                    enemiesRef.setValue(newEnemy);
                    i++;

                    try {
                        Thread.sleep(time_interval);        // go to sleep, you're drunk
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        });

        generationThread.start();       // starts the thread that generates enemies
    }

    // stops generating enemies
    private void stopGeneration(){
        enemyGeneration = false;
        startBtn.setText(getResources().getText(R.string.start));
        generatingIcon.setVisibility(View.INVISIBLE);
    }

    // generate an enemy in a location nearby to a player
    private Enemy createEnemy(String name, int level){
        Enemy enemy = new Enemy();

        enemy.setName(name);
        enemy.setLevel(level);
        enemy.setAtk(3*level);
        enemy.setDef(3*level);
        enemy.setSpd(3*level);
        enemy.setMaxhp(5 * level);
        enemy.setCurrentHP(5 * level);
        enemy.setAlive(true);

        // get a random character from onlineCharacters
        Character unlucky_char = getRandomCharacter();
        double char_lat = unlucky_char.getLat();
        double char_lng = unlucky_char.getLng();

        // get the variance that will be applied to generate the position the enemy
        // will be created. Divided by 1000 so it doesn't vary too much.
        double pos_varianceLat = randomGenerator.nextDouble() / 1000;
        double pos_varianceLng = randomGenerator.nextDouble() / 1000;

        // decides if variance is summed or subtracted
        int sum_or_sub = randomGenerator.nextInt(10);
        if(sum_or_sub > 5){
            enemy.setLat(char_lat + pos_varianceLat);
            enemy.setLng(char_lng + pos_varianceLng);
        } else {
            enemy.setLat(char_lat - pos_varianceLat);
            enemy.setLng(char_lng - pos_varianceLng);
        }

        return enemy;
    }

    private void getOnlineCharacters(){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("characters/");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onlineCharacters = new ArrayList<Character>();    // clears list so we dont repeat characters
                Log.e("Count " , ""+dataSnapshot.getChildrenCount());
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Character character= postSnapshot.getValue(Character.class);
                    if (character != null && character.isOnline()){
                        onlineCharacters.add(character);    // add to list
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to get characters");
            }
        });
    }

    // gets a random character from onlineCharacters list
    private Character getRandomCharacter(){
        int index = randomGenerator.nextInt(onlineCharacters.size());
        Character character = onlineCharacters.get(index);
        Log.e(TAG, "Enemy will be created near " + character.getDisplayName());

        return character;
    }
}
