package com.fbhack2016.beat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private boolean state = false;

    String roomName = null;
    String songURL = null;
    String songName = null;
    MediaPlayer player = null;

    private GoogleApiClient client;

    private class getReq extends AsyncTask<String,Void,String> {
        protected String doInBackground(String... params) {
            String result = "";
            try {
                URL url = new URL("http://fb-beat.herokuapp.com/"+params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("HTTP_RESPONSE",""+urlConnection.getContentType());

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + " ");
                }
                result = sb.toString();
                Log.d("HTTP_RESPONSE",""+sb);
                if (result.contains("True")) {
                    Log.d("CHANNEL","true");
                }else {
                    Log.d("CHANNEL_FAIL","fail");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Pusher pusher = new Pusher("bea8f3b8f2a17f16fefe");
        pusher.connect();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Button btnHost = (Button) findViewById(R.id.btn_host);
        final Button btnJoin = (Button) findViewById(R.id.btn_join);
        final EditText etHost = (EditText) findViewById(R.id.et_nameHost);
        final EditText etJoin = (EditText) findViewById(R.id.et_nameJoin);
        final Button btnSetSong = (Button) findViewById(R.id.btn_setsong);
        final EditText etSetSong = (EditText) findViewById(R.id.et_setSong);
        final ImageButton btnPlaySong = (ImageButton) findViewById(R.id.btn_playsong);
        final ImageButton btnPauseSong = (ImageButton) findViewById(R.id.btn_pausesong);
        final LinearLayout llHost = (LinearLayout) findViewById(R.id.ll_host);
        final LinearLayout llJoin = (LinearLayout) findViewById(R.id.ll_join);
        final LinearLayout llMain = (LinearLayout) findViewById(R.id.ll_main);
        final LinearLayout llSeek = (LinearLayout) findViewById(R.id.ll_seeek);
        final RelativeLayout rlInit = (RelativeLayout) findViewById(R.id.rl_init);
        final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
        final TextView tvSeek = (TextView) findViewById(R.id.tv_seek);
        final TextView tvSong = (TextView) findViewById(R.id.tv_songname);
        final Button btnInitHost = (Button) findViewById(R.id.btn_InitHost);
        final Button btnInitClient = (Button) findViewById(R.id.btn_InitClient);
        final ListView lv = (ListView) findViewById(R.id.listview);

        btnPlaySong.setEnabled(false);
        seekbar.setEnabled(false);

        btnInitHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMain.setVisibility(View.VISIBLE);
                rlInit.setVisibility(View.GONE);
                llHost.setVisibility(View.VISIBLE);
            }
        });

        btnInitClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMain.setVisibility(View.VISIBLE);
                rlInit.setVisibility(View.GONE);
                llJoin.setVisibility(View.VISIBLE);
            }
        });

        final Handler mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (player != null) {
                    Log.d("PLAYSHIT", "seekbar");

                    int mCurrentPosition = player.getCurrentPosition() / 1000;
                    seekbar.setProgress(mCurrentPosition);
                    tvSeek.setText(String.valueOf(mCurrentPosition));
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    //player.seekTo(progress * 1000);

                    try {
                        String result = new getReq().execute("skip/"+roomName+":"+progress).get();
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                        Log.d("SKIP", roomName + " " + result);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnPauseSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String result = new getReq().execute("pause/"+roomName).get();
                    btnPlaySong.setVisibility(View.VISIBLE);
                    btnPauseSong.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                    Log.d("BTNPAUSESONG", roomName+" "+result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String result = new getReq().execute("start/"+roomName).get();
                    seekbar.setEnabled(true);
                    btnPlaySong.setVisibility(View.GONE);
                    btnPauseSong.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                    Log.d("BTNPLAYSONG", roomName+" "+result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    roomName = etHost.getText().toString();
                    String result = new getReq().execute("host/"+roomName).get();

                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();

                    if (!result.contains("False")) {
                        lv.setVisibility(View.VISIBLE);
                        btnHost.setVisibility(View.GONE);
                        etHost.setVisibility(View.GONE);
                        llSeek.setVisibility(View.VISIBLE);

                        String songs = new getReq().execute("getsongs").get();
                        String[] songSplit = songs.split(";");

                        Log.d("SONGSPL",""+songSplit[0]);

                        final String ids[] = new String[songSplit.length];
                        String data[] = new String[songSplit.length];

                        for(int i=0; i<songSplit.length; i++) {
                            Log.d("SONGSPLi",""+songSplit[i]);
                            data[i]=songSplit[i].split(",")[1];
                            ids[i]=songSplit[i].split(",")[0];
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, data);
                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.d("SONGSPLitemclick",""+position+" "+ids[position]);
                                try {
                                    String result = new getReq().execute(roomName + ":" + ids[position]).get();
                                    btnPlaySong.setEnabled(true);
                                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    Log.d("BTNSETSONG", roomName+" "+result);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        String chan = result.split(",")[1].replace(" ", "");
                        Log.d("PUSHER", "binding join " + chan + " and " + "channel0");
                        Channel channel = pusher.subscribe(chan);
                        channel.bind("song_url", new SubscriptionEventListener() {
                            @Override
                            public void onEvent(String channelName, String eventName, final String data) {
                                try {
                                    JSONObject obj = new JSONObject(data);
                                    Log.d("PUSHER_SERVER", (String)obj.get("message"));
                                    songURL = (String)obj.get("message");
                                    resetSongSghit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("PUSHER_SERVER", data);
                            }
                        });
                        channel.bind("play_song", new SubscriptionEventListener() {
                            @Override
                            public void onEvent(String channelName, String eventName, final String data) {
                                Calendar c = Calendar.getInstance();
                                int seconds = c.get(Calendar.SECOND);
                                Log.d("PLAYSHIT", "seconds: " + seconds);

                                try {
                                    if (data.contains("play")) {
                                        Log.d("SKIPPP","playing");
                                        if (player!=null&&!player.isPlaying()) {
                                            player.start();
                                        }else {
                                            player = new MediaPlayer();
                                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            player.setDataSource(songURL);
                                            player.prepare();
                                            Log.d("PLAYSHIT", "seconds2: " + c.get(Calendar.SECOND));
                                            seekbar.setMax(player.getDuration() / 1000); // where mFileDuration is mMediaPlayer.getDuration();
                                            //Make sure you update Seekbar on UI thread
                                            Log.d("PLAYSHIT", "durr: " + player.getDuration());

                                            player.start();
                                            Thread.sleep(1000);
                                            player.seekTo(1000);


                                        }
                                    }else if (data.contains("pause")){
                                        Log.d("SKIPPP","pause");
                                        if(player.isPlaying()){
                                            player.pause();
                                        } else {
                                            player.start();
                                        }
                                    }else /*if (data.contains("skip"))*/{
                                        Log.d("SKIPPP","skipp");

                                        JSONObject obj = new JSONObject(data);
                                        String skip = (String)obj.get("skip");
                                        int foo = Integer.parseInt(skip);

                                        Log.d("SKIPPP","skintf: "+foo);
                                        player.seekTo(foo * 1000);
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                Log.d("PUSHER_SERVER_PLAYSONG", data);
                            }
                        });
                    }

                    Log.d("BTNHOST",result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    etJoin.setVisibility(View.GONE);
                    btnJoin.setVisibility(View.GONE);
                    llSeek.setVisibility(View.VISIBLE);

                    roomName = etJoin.getText().toString();

                    tvSong.setText("You are in room: " + roomName);
                    tvSong.setVisibility(View.VISIBLE);

                    String result = new getReq().execute("join/"+roomName).get();

                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();

                    Log.d("JOIN","here result"+result);

                    if (!result.contains("False")) {
                        String chan = result.split(",")[1].replace(" ","");
                        Log.d("PUSHER","binding join "+chan+" and "+"channel0");
                        Channel channel = pusher.subscribe(chan);
                        channel.bind("song_url", new SubscriptionEventListener() {
                            @Override
                            public void onEvent(String channelName, String eventName, final String data) {
                                try {
                                    JSONObject obj = new JSONObject(data);
                                    Log.d("PUSHER_SERVER", (String)obj.get("message"));
                                    songURL = (String)obj.get("message");

                                    try {
                                        String songs = null;
                                        songs = new getReq().execute("getsongs").get();
                                        String[] songSplit = songs.split(";");

                                        Log.d("SONGSPL",""+songSplit[0]);

                                        HashMap<String,String> hm = new HashMap<String, String>();

                                        for(int i=0; i<songSplit.length; i++) {
                                            Log.d("SONGSPLixx",""+songSplit[i]);
                                            hm.put(songSplit[i].split(",")[0],songSplit[i].split(",")[1]);
                                        }

                                        songName = hm.get((String)obj.get("id"));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("PUSHER_SERVER", data);
                            }
                        });
                        Log.d("PUSHER", "done");
                        channel.bind("play_song", new SubscriptionEventListener() {
                            @Override
                            public void onEvent(String channelName, String eventName, final String data) {
                                Calendar c = Calendar.getInstance();
                                int seconds = c.get(Calendar.SECOND);
                                Log.d("PLAYSHIT", "seconds: " + seconds);

                                try {
                                    if (data.contains("play")) {
                                        Log.d("SKIPPP","playing");
                                        if (player!=null&&!player.isPlaying()) {
                                            player.start();
                                        }else {
                                            player = new MediaPlayer();
                                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            player.setDataSource(songURL);
                                            player.prepare();
                                            Log.d("PLAYSHIT", "seconds2: " + c.get(Calendar.SECOND));
                                            seekbar.setMax(player.getDuration() / 1000); // where mFileDuration is mMediaPlayer.getDuration();
                                            //Make sure you update Seekbar on UI thread
                                            Log.d("PLAYSHIT", "durr: " + player.getDuration());

                                            player.start();
                                            Thread.sleep(1000);
                                            player.seekTo(1000);
                                        }
                                    }else if (data.contains("pause")){

                                        if(player.isPlaying()){
                                            player.pause();
                                        } else {
                                            player.start();
                                        }
                                    }else /*if (data.contains("skip"))*/{
                                        Log.d("SKIPPP","skipp");

                                        JSONObject obj = new JSONObject(data);
                                        String skip = (String)obj.get("skip");
                                        int foo = Integer.parseInt(skip);

                                        Log.d("SKIPPP","skintf: "+foo);
                                        player.seekTo(foo*1000);
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                                Log.d("PUSHER_SERVER_PLAYSONG", data);
                            }
                        });
                    }

                    Log.d("BTNJOIN",result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        btnSetSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String result = new getReq().execute(roomName + ":" + etSetSong.getText().toString()).get();
                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                    Log.d("BTNSETSONG", roomName+" "+result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state) {
                    llHost.setVisibility(View.VISIBLE);
                    llJoin.setVisibility(View.GONE);
                    state=false;
                }else{
                    llHost.setVisibility(View.GONE);
                    llJoin.setVisibility(View.VISIBLE);
                    state=true;
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void resetSongSghit() {
//        final ImageButton btnPlaySong = (ImageButton) findViewById(R.id.btn_playsong);
//        final ImageButton btnPauseSong = (ImageButton) findViewById(R.id.btn_pausesong);
//        final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
//        final TextView tvSeek = (TextView) findViewById(R.id.tv_seek);
//        if (player!=null) {
//            player.pause();
//            btnPlaySong.setVisibility(View.VISIBLE);
//            btnPauseSong.setVisibility(View.GONE);
//            seekbar.setProgress(0);
//            tvSeek.setText("0");
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.fbhack2016.beat/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.fbhack2016.beat/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
