package com.example.rohan.simulplay;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import java.lang.Math;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    int currentPos = 0;
    int trackNum;
    int maxLength = 0;
    boolean isPlaying = false;

    MediaPlayer track1 = new MediaPlayer();
    MediaPlayer track2 = new MediaPlayer();
    MediaPlayer track3 = new MediaPlayer();

    TextView trackStatusTextView1;
    TextView trackStatusTextView2;
    TextView trackStatusTextView3;

    SeekBar seekBar;

    TextView elapsedTimeTextView;
    TextView totalTimeTextView;

    Button addButton;
    ImageButton playButton;
    ImageButton beginButton;

    Switch muteSwitch1;
    Switch muteSwitch2;
    Switch muteSwitch3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackStatusTextView1 = findViewById(R.id.trackStatusTextView1);
        trackStatusTextView2 = findViewById(R.id.trackStatusTextView2);
        trackStatusTextView3 = findViewById(R.id.trackStatusTextView3);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);

        elapsedTimeTextView = findViewById(R.id.elapsedTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);

        addButton = findViewById(R.id.addButton);
        playButton = findViewById(R.id.playButton);
        playButton.setEnabled(false);
        beginButton = findViewById(R.id.beginningButton);
        beginButton.setEnabled(false);

        muteSwitch1 = findViewById(R.id.muteSwitch1);
        muteSwitch2 = findViewById(R.id.muteSwitch2);
        muteSwitch3 = findViewById(R.id.muteSwitch3);

        Handler handler = new Handler();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Track");
                CharSequence[] options = {"1","2","3"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        trackNum = item+1;
                        pickFile();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //track1.start();
                if(isPlaying){
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                    isPlaying = false;
                    track1.pause();
                    track2.pause();
                    track3.pause();
                    currentPos = track1.getCurrentPosition();
                    track1.seekTo(currentPos);
                    track2.seekTo(currentPos);
                    track3.seekTo(currentPos);
                    //tracks.autoPause();
                }
                else{
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    isPlaying = true;

                    track1.start();
                    track2.start();
                    track3.start();

                    updateSeek();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    track1.seekTo(progress);
                    track2.seekTo(progress);
                    track3.seekTo(progress);
                    updateSeek();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track1.seekTo(0);
                track2.seekTo(0);
                track3.seekTo(0);

                updateSeek();
            }
        });

        muteSwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    track1.setVolume(0,0);
                }
                else{
                    track1.setVolume(1,1);
                }
            }
        });

        muteSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    track2.setVolume(0,0);
                }
                else{
                    track2.setVolume(1,1);
                }
            }
        });

        muteSwitch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    track3.setVolume(0,0);
                }
                else{
                    track3.setVolume(1,1);
                }
            }
        });

    }

    public void pickFile(){
        String path = Environment.getExternalStorageDirectory().toString();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(path); // a directory
        intent.setDataAndType(uri, "*/*");
        startActivityForResult(Intent.createChooser(intent, "Open folder"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Uri fileUri = data.getData();
        initializeTrack(trackNum, fileUri);
    }

    public void initializeTrack(int i, Uri fileUri){
        track1.pause();
        track2.pause();
        track3.pause();
        track1.seekTo(0);
        track2.seekTo(0);
        track3.seekTo(0);
        isPlaying = false;
        playButton.setImageResource(android.R.drawable.ic_media_play);
        switch(i){
            case 1: track1 = MediaPlayer.create(getApplicationContext(),fileUri);
                try {
                    track1.getDuration();
                }catch(Exception e){
                    track1 = new MediaPlayer();
                    Toast.makeText(getApplicationContext(), "Error: file format not supported", Toast.LENGTH_LONG).show();
                    break;
                }
                if (track1.getDuration() > maxLength) maxLength = track1.getDuration();
                trackStatusTextView1.setText("Track 1: " + fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/") + 1));
                setSeek(maxLength);
                break;
            case 2: track2 = MediaPlayer.create(getApplicationContext(),fileUri);
                try {
                    track2.getDuration();
                }catch(Exception e){
                    track2 = new MediaPlayer();
                    Toast.makeText(getApplicationContext(), "Error: file format not supported", Toast.LENGTH_LONG).show();
                    break;
                }
                if (track2.getDuration() > maxLength) maxLength = track2.getDuration();
                trackStatusTextView2.setText("Track 2: " + fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/") + 1));
                setSeek(maxLength);
                break;
            case 3: track3 = MediaPlayer.create(getApplicationContext(),fileUri);
                try {
                    track3.getDuration();
                }catch(Exception e){
                    track3 = new MediaPlayer();
                    Toast.makeText(getApplicationContext(), "Error: file format not supported", Toast.LENGTH_LONG).show();
                    break;
                }
                if (track3.getDuration() > maxLength) maxLength = track3.getDuration();
                trackStatusTextView3.setText("Track 3: " + fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/") + 1));
                setSeek(maxLength);
                break;
        }
    }

    private void setSeek(int length){
        seekBar.setMax(maxLength);
        totalTimeTextView.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(maxLength),
                TimeUnit.MILLISECONDS.toSeconds(maxLength) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(maxLength))));
        seekBar.setEnabled(true);
        playButton.setEnabled(true);
        beginButton.setEnabled(true);
    }

    private void updateSeek(){
        seekBar.setEnabled(true);
        int tempProgress = track1.getCurrentPosition();
        seekBar.setProgress(tempProgress);
        elapsedTimeTextView.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(tempProgress),
                TimeUnit.MILLISECONDS.toSeconds(tempProgress) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tempProgress))));
        if(isPlaying){
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    updateSeek();
                }
            };
            new Handler().postDelayed(r,1000);
        }
    }
}
