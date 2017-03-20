package com.codeitnow.edfora_cokestudio.CokeStudioAdapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.codeitnow.edfora_cokestudio.CokeStudioObjects.Song;
import com.codeitnow.edfora_cokestudio.CokeStudioUtilities.CustomVolleyRequest;
import com.codeitnow.edfora_cokestudio.PlaySong;
import com.codeitnow.edfora_cokestudio.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Rahul Malhotra on 3/19/2017.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    Context context;
    LayoutInflater inflater;
    ArrayList<Song> songs;
    ImageLoader imageLoader;
    LinearLayout linearlayout;

    public SongAdapter(Context context, ArrayList<Song> songs)
    {
        this.context = context;
        this.songs = songs;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.song,parent,false);
        linearlayout = (LinearLayout) view.findViewById(R.id.llay);
        SongViewHolder viewHolder = new SongViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SongAdapter.SongViewHolder holder, final int position) {

        holder.songname.setText(songs.get(position).song);
        imageLoader = CustomVolleyRequest.getInstance(context.getApplicationContext()).getImageLoader();
        String url = songs.get(position).cover_image;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            url = expandUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageLoader.get(url, imageLoader.getImageListener(holder.songimage,R.mipmap.ic_launcher,android.R.drawable.ic_dialog_alert));
        holder.songimage.setImageUrl(url,imageLoader);
        holder.songartists.setText("Artists: "+songs.get(position).artists);

        final String finalUrl = url;

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadSong1 downloadSong1 = null;
                try {
                    downloadSong1 = new DownloadSong1(songs.get(position).song,expandUrl(songs.get(position).url));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                downloadSong1.execute();
            }
        });

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaySong.class);
                intent.putExtra("songname",songs.get(position).song);
                intent.putExtra("songartist",songs.get(position).artists);
                intent.putExtra("songimage", finalUrl);
                try {
                    intent.putExtra("songurl",expandUrl(songs.get(position).url));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static String expandUrl(String shortenedUrl) throws IOException {
        URL url = new URL(shortenedUrl);
        // open connection
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);

        // stop following browser redirect
        httpURLConnection.setInstanceFollowRedirects(false);

        // extract location header containing the actual destination URL
        String expandedURL = httpURLConnection.getHeaderField("location");
        Log.d("pp",expandedURL);
        httpURLConnection.disconnect();

        return expandedURL;
    }

    class SongViewHolder extends RecyclerView.ViewHolder
    {
        public NetworkImageView songimage;
        public TextView songname,songartists;
        public ImageView play,download;
        public CardView root;
        public SongViewHolder(View itemView) {
            super(itemView);
            root = (CardView) itemView.findViewById(R.id.row);
            songimage = (NetworkImageView) itemView.findViewById(R.id.songimage);
            songname = (TextView) itemView.findViewById(R.id.songname);
            songartists = (TextView) itemView.findViewById(R.id.songartists);
            play = (ImageView) itemView.findViewById(R.id.songplay);
            download = (ImageView) itemView.findViewById(R.id.songdownload);
        }
    }

    private class DownloadSong1 extends AsyncTask<String, Integer, String> {

        Snackbar snackbar;
        private String songname,songurl;
        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Downloading Song....", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        DownloadSong1(String songname,String songurl)
        {
            this.songname = songname;
            this.songurl = songurl;
            Log.d("ppppppp",songname+songurl);
        }
        @Override
        protected String doInBackground(String... url) {
            int count;
            try {
                URL urlp = new URL(songurl);
                URLConnection conexion = urlp.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(urlp.openStream());
                OutputStream output = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath()+"/" + songname + ".mp3"));
                // this will be useful so that you can show a tipical 0-100% progress bar
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.d("kkkkk",e.getLocalizedMessage());
                Toast.makeText(context, "kkkkk", Toast.LENGTH_SHORT).show();
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Toast.makeText(context, "Downloading...."+values[0]+"%", Toast.LENGTH_SHORT).show();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(context, "Song Downloaded and stored in phone memory", Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }
    }

}