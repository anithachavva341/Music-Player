package com.codeitnow.edfora_cokestudio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.codeitnow.edfora_cokestudio.CokeStudioAdapters.SongAdapter;
import com.codeitnow.edfora_cokestudio.CokeStudioObjects.Song;
import com.codeitnow.edfora_cokestudio.CokeStudioUtilities.VolleySingleton;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SongAdapter songAdapter;
    RecyclerView songslistview;
    ArrayList<Song> songslist;
    private TextView empty;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions Granted...!!", Toast.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);

        initialize();
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        adddatatoview();

    }

    private void adddatatoview()
    {
        final CircularProgressView progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.startAnimation();
        String url = "http://starlord.hackerearth.com/edfora/cokestudio";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressView.stopAnimation();
                progressView.setVisibility(View.INVISIBLE);
                Gson gson = new Gson();
                for (int i=0;i<response.length();i++)
                {
                    Song song = null;
                    try {
                        song = gson.fromJson(response.getJSONObject(i).toString(),Song.class);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    songslist.add(song);
                }
                if(response.length()==0)
                {
                    empty.setVisibility(View.VISIBLE);
                    empty.setText("No Results Found!");
                }
                else
                {
                    songAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressView.stopAnimation();
                progressView.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.VISIBLE);
                empty.setText("Network Error");
                Toast.makeText(MainActivity.this, "Error..!! Please check your network connection and try again later", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("yoyo",response.toString());
                JSONArray array;
                try {
                    array = new JSONArray(response.toString());
                    progressView.stopAnimation();
                    Gson gson = new Gson();
                    for (int i=0;i<array.length();i++)
                    {
                        Song song = gson.fromJson(array.getJSONObject(i).toString(),Song.class);
                        songslist.add(song);
                    }
                    if(array.length()==0)
                    {
                        empty.setVisibility(View.VISIBLE);
                        empty.setText("No Results Found!");
                    }
                    else
                    {
                        songAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("yoyo",error.toString());
                progressView.stopAnimation();
                progressView.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.VISIBLE);
                empty.setText("Network Error");
                Toast.makeText(MainActivity.this, "Error..!! Please check your network connection and try again later", Toast.LENGTH_SHORT).show();
            }
        });*/
        VolleySingleton.getInstance().getRequestQueue().add(jsonArrayRequest);
    }

    public void initialize()
    {
        empty = (TextView) findViewById(R.id.empty);
        empty.setVisibility(View.GONE);
        songslistview = (RecyclerView) findViewById(R.id.songlist);
        songslistview.setHasFixedSize(true);
        songslistview.setLayoutManager(new LinearLayoutManager(this));
        songslist = new ArrayList<>();
        songAdapter = new SongAdapter(this,songslist);
        songslistview.setAdapter(songAdapter);
    }
}
