package com.codeitnow.edfora_cokestudio;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import info.abdolahi.CircularMusicProgressBar;

public class PlaySong extends AppCompatActivity {

    TextView songname1,songartists;
    String songname,songartist,songimage,songurl;
    private boolean playpause;
    private boolean initialStage=true;
    private MediaPlayer mediaPlayer;
    ImageButton pauseplay;
    CircularMusicProgressBar circularMusicProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        initialize();
    }

    private View.OnClickListener playsong = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!playpause)
            {
                pauseplay.setBackgroundResource(R.drawable.ic_pause_circle_normal);
                if(initialStage)
                {
                    new Player().execute(songurl);
                }
                else
                {
                    if(!mediaPlayer.isPlaying())
                    {
                        mediaPlayer.start();
                    }
                }
                playpause = true;
            }
            else
            {
                pauseplay.setBackgroundResource(R.drawable.ic_play_circle_normal);
                if(mediaPlayer.isPlaying())
                {
                   mediaPlayer.pause();
                }
                playpause = false;
            }
        }
    };

    private  void  initialize()
    {
        playpause = false;
        circularMusicProgressBar = (CircularMusicProgressBar) findViewById(R.id.album_art);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        pauseplay = (ImageButton) findViewById(R.id.play);
        songname1 = (TextView) findViewById(R.id.songname);
        songartists = (TextView) findViewById(R.id.songartists);
        pauseplay.setOnClickListener(playsong);
        Bundle bundle = getIntent().getExtras();
        songname = bundle.getString("songname");
        songartist = bundle.getString("songartist");
        songimage = bundle.getString("songimage");
        songurl = bundle.getString("songurl");
        URL url1 = null;
        try {
            url1 = new URL(songimage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url1.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setDoInput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream input = null;
        try {
            input = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedInputStream bis = new BufferedInputStream(input);
        Bitmap bitmap = BitmapFactory.decodeStream(bis);
        //circularMusicProgressBar.setImageURI();
        circularMusicProgressBar.setImageBitmap(bitmap);
        songname1.setText(songname);
        songartists.setText(songartist);
    }

        class Player extends AsyncTask<String, Integer , Boolean> {
        private ProgressDialog progress;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            Boolean prepared;
            try {

                final int[] p = {0};
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        p[0] = 1;
                    }
                });

                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                        if(p[0]==1)
                        circularMusicProgressBar.setValue(getProgressPercentage(mediaPlayer.getCurrentPosition(),mediaPlayer.getDuration()));
                    }
                });

                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        initialStage = true;
                        playpause=false;
                        pauseplay.setBackgroundResource(R.drawable.ic_play_circle_normal);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegarArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (progress.isShowing()) {
                progress.cancel();
            }
            Log.d("Prepared", "//" + result);
            mediaPlayer.start();

            initialStage = false;
        }

        public Player() {
            progress = new ProgressDialog(PlaySong.this);
        }

        public int getProgressPercentage(long currentDuration, long totalDuration){
            Double percentage = (double) 0;

            long currentSeconds = (int) (currentDuration / 1000);
            long totalSeconds = (int) (totalDuration / 1000);

            // calculating percentage
            percentage =(((double)currentSeconds)/totalSeconds)*100;

            // return percentage
            return percentage.intValue();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            this.progress.setMessage("Buffering...");
            this.progress.show();

        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}