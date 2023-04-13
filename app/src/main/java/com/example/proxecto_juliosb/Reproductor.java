package com.example.proxecto_juliosb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class Reproductor extends AppCompatActivity {

    public static BaseDatos bd=null;
    static boolean created=false;
    boolean descargado=false;
    ArrayList<String> groups=new ArrayList<>();
    String search="";
    String url="";
    File ruta=null;
    static ArrayList<Cancion> cancions=null;
    static ArrayList<Cancion> randSong=null;
    static ArrayList<String> rand=null;
    static ArrayList<String> randRoute=null;
    static ArrayList<String> rutas=null;
    static ArrayList<String> lista=null;
    static String rutaActual="";
    static String rutaImaxe="";
    static int actpos=0;
    static int completed=0;
    static MediaPlayer mediaplayer= new MediaPlayer();
    static String crearArtista="CREATE TABLE IF NOT EXISTS ARTISTA ( "+"ID_ARTISTA VARCHAR(50) PRIMARY KEY, Nome_Artista VARCHAR(150))";
    static String crearAlbum="CREATE TABLE IF NOT EXISTS ALBUM ( "+"ID_ALBUM VARCHAR(50) PRIMARY KEY, Nome_Album VARCHAR(150), Imaxe VARCHAR(150))";
    static String crearCancion="CREATE TABLE IF NOT EXISTS CANCION ( "+"ID_CANCION VARCHAR(50) PRIMARY KEY, Titulo VARCHAR(150), Data VARCHAR(150), Duracion INTEGER(10), Ruta VARCHAR(150), " +
            "IdArtista VARCHAR(50) DEFAULT 'Desconocido', IdAlbum VARCHAR(50) DEFAULT 'Desconocido', FOREIGN KEY (IdArtista) REFERENCES ARTISTA(ID_ARTISTA) ON UPDATE CASCADE ON DELETE SET DEFAULT," +
            "FOREIGN KEY (IdAlbum) REFERENCES ALBUM(ID_ALBUM) ON UPDATE CASCADE ON DELETE SET DEFAULT)";
    static String crearLista="CREATE TABLE IF NOT EXISTS LISTA ("+"ID_LISTA VARCHAR(50) PRIMARY KEY, Nome_Lista VARCHAR(150))";
    static String ListaCancion="CREATE TABLE IF NOT EXISTS LISTACANCION ("+"IdLista VARCHAR(50), IdCancion VARCHAR(50), PRIMARY KEY(IdLista,IdCancion), FOREIGN KEY (IdLista) REFERENCES LISTA(ID_LISTA)" +
            "ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY (IdCancion) REFERENCES CANCION(ID_CANCION))";
    Bridge bridge;
    static boolean loop_active=false;
    static boolean random_active=false;
    boolean doubleBackToExitPressedOnce = false;
    String previous="";
    int reqCode = 1;
    Intent intent;
    static String artist="Desconocido";
    RemoteViews remoteViews;
    String CHANNEL_ID = "channel_name";// The id of the channel.
    String time= "00:00";
    boolean completed1;
    String defaultImage="";
    Toast mtoast=null;
    int count=0;
    int fileNumber=0;
    String error="";
    boolean first=false;
    File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bridge= new Bridge();
        if(getSupportActionBar()!=null) {
            getSupportActionBar().hide();
        }
        fillSpinners();

        String[] permission={Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if((ActivityCompat.checkSelfPermission(this,permission[0])== -1) || (ActivityCompat.checkSelfPermission(this,permission[1])== -1))
            ActivityCompat.requestPermissions(this, permission,100);

        if(bd==null) {
            bd = new BaseDatos(getApplicationContext(), "DATOS", null, 1);
            bd.sqlLiteDB = bd.getWritableDatabase();
            bd.sqlLiteDB.execSQL(crearArtista);
            bd.sqlLiteDB.execSQL(crearAlbum);
            bd.sqlLiteDB.execSQL(crearCancion);
            bd.sqlLiteDB.execSQL(crearLista);
            bd.sqlLiteDB.execSQL(ListaCancion);
            created=true;
        }

        cancions= bd.obterCancions();
        if(cancions!=null){
            TextView text=(TextView) findViewById(R.id.txt_empty);
            Button btn=(Button) findViewById(R.id.btn_add);
            text.setVisibility(View.GONE);
            btn.setVisibility(View.GONE);
            lista= new ArrayList<>();
            rutas= new ArrayList<>();
            String lista_nombre="Default";
            String listaid=(randomID());

            String sql = "SELECT ID_LISTA FROM LISTA WHERE Nome_Lista=?";
            Cursor cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{lista_nombre});
            if (!cursor.moveToFirst())
                bd.sqlLiteDB.execSQL("INSERT INTO LISTA (ID_LISTA,Nome_Lista) VALUES ('" + listaid + "','" + lista_nombre + "')");
            else
                listaid = cursor.getString(0);
            cursor.close();

            for(Cancion c:cancions) {
//                for(Artista art:artistas){
//                    Log.i("t",c.getIdArtista()+" "+art.getIdArtista());
//                    if(art.getIdArtista().equals(c.getIdArtista())) {
//                        lista.add(""+art.getNomeArtista() + " - " + c.getTitulo());
//                        break;
//                    }
//                }
                sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{c.getIdArtista()});
                if(cursor.moveToFirst()){
                    lista.add(cursor.getString(0) + " - " + c.getTitulo());}
                else
                    lista.add(c.getTitulo());
                cursor.close();
                rutas.add(c.getRuta());
                if(!listaid.equals(""))
                    bd.sqlLiteDB.execSQL("INSERT OR IGNORE INTO LISTACANCION (IdLista,IdCancion) VALUES ('" + listaid + "','" + c.getIdCancion() + "')");
            }
            rutaActual=rutas.get(actpos);
            reproducir();
            updateProgress();
            mediaplayer.pause();
            intent = new Intent(getApplicationContext(), Reproductor.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getArtist();
            showNotification(this, artist,cancions.get(actpos).getTitulo(), intent, reqCode,time,0,0);
        }

        if(lista!=null) {
            TextView texto = (TextView) findViewById(R.id.text_song);
            texto.setText(lista.get(actpos));
            fillList();
        }
        RelativeLayout rel= (RelativeLayout) findViewById(R.id.rel1);
        rel.setOnClickListener(v -> startReproductorActivity());

        mediaplayer.setOnCompletionListener(mp -> {
            if(cancions!=null) {
                if ((actpos + 1) < cancions.size()) {
                    actpos = actpos + 1;
                    rutaActual = rutas.get(actpos);
                    completed++;
                    reproducir();
                    updateProgress();
                    getArtist();
                    TextView texto = (TextView) findViewById(R.id.text_song);
                    texto.setText(lista.get(actpos));
                }
                else if(random_active&&actpos+1==lista.size()){
                    actpos=0;
                    rutaActual = rutas.get(actpos);
                    reproducir();
                    updateProgress();
                    getArtist();
                    TextView texto = (TextView) findViewById(R.id.text_song);
                    texto.setText(lista.get(actpos));
                }
                else{
                    MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                }
            }
        });
        Spinner spinner=(Spinner) findViewById(R.id.orders);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner.getItemAtPosition(position).equals(getResources().getString(R.string.name))) {
                    sortName();
                    previous="sort";
                }
                if(spinner.getItemAtPosition(position).equals(getResources().getString(R.string.date))) {
                    sortDate();
                    previous="sort";
                }
                if(spinner.getItemAtPosition(position).equals(getResources().getString(R.string.artist))) {
                    sortArtista();
                    previous="sort";
                }
                if(spinner.getItemAtPosition(position).equals(getResources().getString(R.string.album))) {
                    sortAlbum();
                    previous="sort";
                }
                spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner spinner1=(Spinner) findViewById(R.id.groups);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner1.getItemAtPosition(position).equals(getResources().getString(R.string.artist))) {
                    previous="groupArtist";
                    groupArtist();
                }
                if(spinner1.getItemAtPosition(position).equals(getResources().getString(R.string.album))) {
                    previous="groupAlbum";
                    groupAlbum();
                }
                if(spinner1.getItemAtPosition(position).equals(getResources().getString(R.string.date))) {
                    previous="groupDate";
                    groupDate();
                }
                spinner1.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        SeekBar progress= (SeekBar) findViewById(R.id.progress);
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaplayer != null && fromUser){
                    mediaplayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public String randomID(){
        return ""+ UUID.randomUUID().toString().replace("-", "").substring(0,15);
    }

    public void getArtist(){
        String sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
        Cursor cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{cancions.get(actpos).getIdArtista()});
        if(cursor.moveToFirst())
            artist=cursor.getString(0);
        else
            artist="Desconocido";
        cursor.close();
    }

    /**
     *
     * @param context -->
     * @param title  --> title to show
     * @param message --> details to show
     * @param intent --> What should happen on clicking the notification
     * @param reqCode --> unique code for the notification
     */

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode, String time, int max, int progress) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, 0);
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        changeImage();
        remoteViews.setImageViewBitmap(R.id.cover,BitmapFactory.decodeFile(rutaImaxe));
        remoteViews.setTextViewText(R.id.text_artist,title);
        remoteViews.setTextViewText(R.id.text_title,message);
        remoteViews.setTextViewText(R.id.time1, time);
        remoteViews.setProgressBar(R.id.progress, max, progress, false);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.play)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setSound(null,null);
            mChannel.enableVibration(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        Notification notification=notificationBuilder.build();
        notification.flags= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(reqCode, notification); // 0 is the request code, it should be unique id
    }

    public void fillSpinners(){
        Spinner spinner=(Spinner) findViewById(R.id.orders);

        ArrayList<String> lista= new ArrayList<>();
        lista.add(getResources().getString(R.string.order));
        lista.add(getResources().getString(R.string.name));
        lista.add(getResources().getString(R.string.date));
        lista.add(getResources().getString(R.string.artist));
        lista.add(getResources().getString(R.string.album));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, lista);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(arrayAdapter);

        spinner=(Spinner) findViewById(R.id.groups);

        lista= new ArrayList<>();
        lista.add(getResources().getString(R.string.group));
        lista.add(getResources().getString(R.string.artist));
        lista.add(getResources().getString(R.string.album));
        lista.add(getResources().getString(R.string.date));

        arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, lista);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(arrayAdapter);

    }

    public void startReproductorActivity(){
        Intent intent= new Intent(this, ReproductorActivity.class);
        startActivity(intent);
    }

    public void onButtonAdd(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 101);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                try {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();

                    // Get the path
                    f = new File(Environment.getExternalStorageDirectory().getPath() + "/" + uri.getPath().split(":")[1]);
                    if (!f.exists()) {
                        File[] files = getExternalFilesDirs(null);
                        String sd = "/" + files[1].toString().split("/")[1] + "/" + files[1].toString().split("/")[2];
                        f = new File(sd + "/" + uri.getPath().split(":")[1]);
                    }
                    if (!f.exists()) {
                        File[] files = getExternalFilesDirs(null);
                        String sd = "/" + files[2].toString().split("/")[1] + "/" + files[2].toString().split("/")[2];
                        f = new File(sd + "/" + uri.getPath().split(":")[1]);
                    }
                    if (f.isDirectory()) {
                        MediaMetadataRetriever retrieve = new MediaMetadataRetriever();
                        count = 0;
                        fileNumber = f.list().length;
                        mtoast = Toast.makeText(Reproductor.this, "" + getResources().getString(R.string.toast_count) + ": " + count + " " + getResources().getString(R.string.of) + " " + fileNumber, Toast.LENGTH_LONG);
                        mtoast.show();
                        first = true;
                        Thread t1 = new Thread(() -> {
                            try {
                                for (File f1 : f.listFiles()) {
                                    if (f1.getAbsolutePath().startsWith("mp3", f1.getAbsolutePath().length() - 3)) {
                                        try {
                                            count++;
                                            if (count % 25 == 0) {
                                                runOnUiThread(() -> {
                                                    mtoast.cancel();
                                                    mtoast = Toast.makeText(Reproductor.this, "" + getResources().getString(R.string.toast_count) + ": " + count + " " + getResources().getString(R.string.of) + " " + fileNumber, Toast.LENGTH_SHORT);
                                                    mtoast.show();
                                                });
                                            }
                                            Log.i("", f1.getAbsolutePath());
                                            try {
                                                retrieve.setDataSource(f1.getAbsolutePath());
                                            } catch (Exception e) {
                                                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            String hasAudio = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
                                            boolean isAudio = "yes".equals(hasAudio);
                                            if (isAudio) {
                                                String album = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                                String artist = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                                String title = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                                String duration = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                String date = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                                                String path = f1.getPath();
                                                byte[] image = retrieve.getEmbeddedPicture();
                                                if (album == null)
                                                    album = "Desconocido";
                                                if (artist == null)
                                                    artist = "Desconocido";
                                                if (date == null)
                                                    date = "Desconocido";
                                                if (title == null)
                                                    title = f1.getName();
                                                String imagepath;
                                                if ((image == null) && (first)) {
                                                    Bitmap src = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.default_img, null)).getBitmap();
                                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                    src.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                    image = stream.toByteArray();
                                                    stream.close();
                                                    File newimage = new File(getFilesDir() + "/covers");
                                                    newimage.mkdirs();
                                                    newimage = new File(newimage.getPath() + "/" + randomID() + ".jpg");
                                                    imagepath = newimage.getPath();
                                                    defaultImage = imagepath;
                                                    FileOutputStream fos = new FileOutputStream(newimage);
                                                    fos.write(image);
                                                    fos.close();
                                                    first = false;
                                                } else if (first) {
                                                    File newimage = new File(getFilesDir() + "/covers");
                                                    newimage.mkdirs();
                                                    newimage = new File(newimage.getPath() + "/" + randomID() + ".jpg");
                                                    imagepath = newimage.getPath();
                                                    defaultImage = imagepath;
                                                    FileOutputStream fos = new FileOutputStream(newimage);
                                                    fos.write(image);
                                                    fos.close();
                                                    first = false;
                                                } else if (image == null) {
                                                    imagepath = defaultImage;
                                                } else {
                                                    File newimage = new File(getFilesDir() + "/covers");
                                                    newimage.mkdirs();
                                                    newimage = new File(newimage.getPath() + "/" + randomID() + ".jpg");
                                                    imagepath = newimage.getPath();
                                                    FileOutputStream fos = new FileOutputStream(newimage);
                                                    fos.write(image);
                                                    fos.close();
                                                }
                                                String albumid = (randomID());
                                                String artistid = (randomID());
                                                String cancionid = (randomID());
                                                Cursor cursor;

                                                String sql = "SELECT ID_ALBUM FROM ALBUM WHERE Nome_Album=?";
                                                cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{album});
                                                if (!cursor.moveToFirst())
                                                    bd.sqlLiteDB.execSQL("INSERT INTO ALBUM (ID_ALBUM,Nome_Album,Imaxe) VALUES ('" + albumid + "',?,?)", new String[]{album, imagepath});
                                                else
                                                    albumid = cursor.getString(0);
                                                cursor.close();
                                                sql = "SELECT ID_ARTISTA FROM ARTISTA WHERE Nome_Artista=?";
                                                cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{artist});
                                                if (!cursor.moveToFirst())
                                                    bd.sqlLiteDB.execSQL("INSERT INTO ARTISTA (ID_ARTISTA,Nome_Artista) VALUES ('" + artistid + "',?)", new String[]{artist});
                                                else
                                                    artistid = cursor.getString(0);
                                                cursor.close();
                                                sql = "SELECT ID_CANCION FROM CANCION WHERE Ruta=?";
                                                cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{path});
                                                if (!cursor.moveToFirst())
                                                    bd.sqlLiteDB.execSQL("INSERT INTO CANCION (ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum) VALUES " +
                                                            "('" + cancionid + "',?,'" + date + "','" + Integer.parseInt(duration) + "',?,'" + artistid + "','" + albumid + "')", new String[]{title, path});
                                                cursor.close();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            } finally {
                                runOnUiThread(this::end);
                            }
                        });
                        t1.start();
                        t1.wait(10);
                        t1.join();

                    } else {
                        error = f.getAbsolutePath();
                        Toast.makeText(Reproductor.this, "Not a folder " + error, Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void end(){
        TextView text = (TextView) findViewById(R.id.txt_empty);
        Button btn = (Button) findViewById(R.id.btn_add);
        text.setVisibility(View.GONE);
        btn.setVisibility(View.GONE);
        cancions = bd.obterCancions();
        lista = new ArrayList<>();
        rutas = new ArrayList<>();
        String lista_nombre="Default";
        String listaid=(randomID());

        String sql = "SELECT ID_LISTA FROM LISTA WHERE Nome_Lista=?";
        Cursor cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{lista_nombre});
        if (!cursor.moveToFirst())
            bd.sqlLiteDB.execSQL("INSERT INTO LISTA (ID_LISTA,Nome_Lista) VALUES ('" + listaid + "','" + lista_nombre + "')");
        else
            listaid = cursor.getString(0);
        cursor.close();
        for (Cancion c : cancions) {
            sql = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
            cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{c.getIdArtista()});
            if (cursor.moveToFirst())
                lista.add(cursor.getString(0) + " - " + c.getTitulo());
            else
                lista.add(c.getTitulo());
            rutas.add(c.getRuta());
            cursor.close();
            if(!listaid.equals(""))
                bd.sqlLiteDB.execSQL("INSERT OR IGNORE INTO LISTACANCION (IdLista,IdCancion) VALUES ('" + listaid + "','" + c.getIdCancion() + "')");
            if(lista!=null){
                fillList();
                TextView texto=(TextView) findViewById(R.id.text_song);
                actpos=0;
                reproducir();
                mediaplayer.stop();
                texto.setText(lista.get(actpos));
            }
        }
        intent = new Intent(getApplicationContext(), Reproductor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getArtist();
        showNotification(Reproductor.this, artist,cancions.get(actpos).getTitulo(), intent, reqCode,time,0,0);
    }

    public void fillList(){

        TextView texto=(TextView) findViewById(R.id.text_song);

        if(random_active){
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_random);
            btn.setIconTintResource(R.color.white);
            random_active=false;
        }

        ListView list = (ListView) findViewById(R.id.list1);
        ArrayAdapter<Cancion> arrayAdapter = new CustomList(this, cancions);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
            actpos=position;
            texto.setText(lista.get(actpos));
            rutaActual=rutas.get(actpos);
            completed++;
            reproducir();
            updateProgress();
            getArtist();
        });
        list.setSelectionAfterHeaderView();
    }

    public void updateProgress(){
        Thread t1= new Thread(() -> {
            SeekBar progress=(SeekBar) findViewById(R.id.progress);
            progress.setMax(mediaplayer.getDuration()/1000);
            Message msg = new Message();
            msg.arg1=2;
            bridge.sendMessage(msg);
            while(mediaplayer.isPlaying()){
                int prog = (mediaplayer.getCurrentPosition() / 1000);
                progress.setProgress(prog);
                msg = new Message();
                msg.arg1=1;
                bridge.sendMessage(msg);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }

    private class Bridge extends Handler {

        Bridge() {
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1==1){
                TextView text1=(TextView) findViewById(R.id.time1);
                String seconds=""+(mediaplayer.getCurrentPosition()/1000%60);
                String minutes=""+(mediaplayer.getCurrentPosition()/60000);
                if (seconds.length()<2)
                    seconds="0"+seconds;
                if(minutes.length()<2)
                    minutes="0"+minutes;
                if((mediaplayer.getCurrentPosition()/60000)==(mediaplayer.getDuration()/60000)){
                    if((mediaplayer.getCurrentPosition()/1000%60)>(mediaplayer.getDuration()/1000%60)) {
                        seconds = "" + (mediaplayer.getDuration() / 1000 % 60);
                        if (seconds.length() < 2)
                            seconds = "0" + seconds;
                    }
                }
                text1.setText(minutes+":"+seconds);
                showNotification(Reproductor.this, artist,cancions.get(actpos).getTitulo(), intent, reqCode,minutes+":"+seconds,
                        mediaplayer.getDuration()/1000, mediaplayer.getCurrentPosition() / 1000);
            }
            if(msg.arg1==2){
                TextView time2=(TextView) findViewById(R.id.time2);
                String seconds=""+(mediaplayer.getDuration()/1000%60);
                String minutes=""+(mediaplayer.getDuration()/60000);
                if (seconds.length()<2)
                    seconds="0"+seconds;
                if(minutes.length()<2)
                    minutes="0"+minutes;
                time2.setText(minutes+":"+seconds);
            }
            if(msg.arg1==3){
                mtoast= Toast.makeText(Reproductor.this, ""+getResources().getString(R.string.toast_count)+": "+count+" "+getResources().getString(R.string.of)+" "+fileNumber, Toast.LENGTH_SHORT);
                mtoast.show();
            }
            if(msg.arg1==6){
                mtoast.cancel();
                mtoast.setText(""+getResources().getString(R.string.toast_count)+": "+count+" "+getResources().getString(R.string.of)+" "+fileNumber);
                mtoast.show();
            }
        }
    }

    public static void reproducir(){
        try {
            mediaplayer.reset();
            mediaplayer.setDataSource(rutaActual);
            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaplayer.prepare();
            mediaplayer.start();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onButtonBack(View v){
        if(cancions!=null) {
            if(mediaplayer.getCurrentPosition()>5000) {
                mediaplayer.seekTo(0);
            }
            else {
                MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
                if ((actpos - 1) >= 0) {
                    mediaplayer.stop();
                    actpos = actpos - 1;
                    rutaActual = rutas.get(actpos);
                    TextView texto = (TextView) findViewById(R.id.text_song);
                    texto.setText(lista.get(actpos));
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    completed++;
                    changeImage();
                    reproducir();
                    updateProgress();
                    getArtist();
                }
                else if(random_active&&actpos==0){
                    actpos=lista.size()-1;
                    mediaplayer.stop();
                    rutaActual = rutas.get(actpos);
                    TextView texto = (TextView) findViewById(R.id.text_song);
                    texto.setText(lista.get(actpos));
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    completed++;
                    changeImage();
                    reproducir();
                    updateProgress();
                    getArtist();
                }
            }
        }
    }

    public void onButtonPause(View v){
        if(cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            if(completed==0){
                mediaplayer.stop();
                rutaActual = rutas.get(actpos);
                TextView texto = (TextView) findViewById(R.id.text_song);
                texto.setText(lista.get(actpos));
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                completed++;
                changeImage();
                reproducir();
                updateProgress();
                getArtist();
            }
            else {
                if (mediaplayer.isPlaying()) {
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    mediaplayer.pause();
                } else {
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    mediaplayer.start();
                    updateProgress();
                    getArtist();
                }
            }
        }
    }

    public void onButtonForw(View v){
        if(cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            if ((actpos + 1) < cancions.size()) {
                mediaplayer.stop();
                actpos = actpos + 1;
                rutaActual = rutas.get(actpos);
                TextView texto = (TextView) findViewById(R.id.text_song);
                texto.setText(lista.get(actpos));
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                completed++;
                changeImage();
                reproducir();
                updateProgress();
                getArtist();
            }
            else if(random_active&&actpos+1==lista.size()){
                actpos=0;
                mediaplayer.stop();
                rutaActual = rutas.get(actpos);
                TextView texto = (TextView) findViewById(R.id.text_song);
                texto.setText(lista.get(actpos));
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                completed++;
                changeImage();
                reproducir();
                updateProgress();
                getArtist();
            }
        }
    }

    public void onButtonRandom(View v){
        if(cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_random);
            if(random_active){
                btn.setIconTintResource(R.color.white);
                cancions=new ArrayList<>(randSong);
                lista=new ArrayList<>(rand);
                rutas=new ArrayList<>(randRoute);
                random_active=false;
            }
            else{
                btn.setIconTintResource(R.color.purple_200);
                randSong=new ArrayList<>(cancions);
                rand=new ArrayList<>(lista);
                randRoute=new ArrayList<>(rutas);
                int r= new Random().nextInt();
                Collections.shuffle(cancions,new Random(r));
                Collections.shuffle(lista,new Random(r));
                Collections.shuffle(rutas,new Random(r));
                random_active=true;
            }
        }
    }

    public void onButtonLoop(View v){
        if(cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_loop);
            if(loop_active){
                btn.setIconTintResource(R.color.white);
                mediaplayer.setLooping(false);
                loop_active=false;
            }
            else{
                btn.setIconTintResource(R.color.purple_200);
                mediaplayer.setLooping(true);
                loop_active=true;
            }
        }
    }

    public void changeImage(){
        Cursor cursor;
        String sql ="SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
        cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{cancions.get(actpos).getIdAlbum()});
        if(cursor.moveToFirst())
            rutaImaxe=cursor.getString(0);
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menus, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(reqCode);
    }

    @Override
    protected void onResume(){
        super.onResume();

        MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
        if(mediaplayer.isPlaying()) {
            btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
            updateProgress();
            getArtist();
        }
        else{
            btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
        }

        if(mediaplayer!=null) {
            MaterialButton btn1 = (MaterialButton) findViewById(R.id.btn_loop);
            MaterialButton btn2 = (MaterialButton) findViewById(R.id.btn_random);
            if(loop_active)
                btn1.setIconTintResource(R.color.purple_200);
            else
                btn1.setIconTintResource(R.color.white);
            if(random_active)
                btn2.setIconTintResource(R.color.purple_200);
            else
                btn2.setIconTintResource(R.color.white);

        }

        if(cancions !=null && lista!=null) {
            TextView texto = (TextView) findViewById(R.id.text_song);
            texto.setText(lista.get(actpos));
            fillList();
        }

        if(created) {
            if (bd == null) {
                bd = new BaseDatos(getApplicationContext(), "DATOS", null, 1);
                bd.sqlLiteDB = bd.getWritableDatabase();
            }
        }

    }

    public void updateView(){
        MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
        SeekBar progress = (SeekBar) findViewById(R.id.progress);
        TextView text1= (TextView) findViewById(R.id.time1);
        btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
        text1.setText("00:00");
        progress.setProgress(0);
    }

    public void sortName(){
        cancions= bd.obterCancionsNome();
        sorts();
    }

    public void sortDate(){
        cancions= bd.obterCancionsOrdearData();
        sorts();
    }

    public void sortAlbum(){
        cancions= bd.obterCancionsOrdearAlbum();
        sorts();
    }

    public void sortArtista(){
        cancions= bd.obterCancionsOrdearArtista();
        sorts();
    }

    public void sorts(){
        if(cancions!=null){
            lista= new ArrayList<>();
            rutas= new ArrayList<>();
            actpos=0;

            for(Cancion c:cancions) {
                Cursor cursor;
                String sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{c.getIdArtista()});
                if(cursor.moveToFirst())
                    lista.add(cursor.getString(0) + " - " + c.getTitulo());
                else
                    lista.add(c.getTitulo());
                cursor.close();
                rutas.add(c.getRuta());
            }
            rutaActual=rutas.get(actpos);
        }

        if(lista!=null) {
            TextView texto = (TextView) findViewById(R.id.text_song);
            texto.setText(lista.get(actpos));
            updateView();
            reproducir();
            mediaplayer.pause();
            updateProgress();
            getArtist();
            fillList();
        }
    }

    public void groupDate(){
        ArrayList<Group> lista1= new ArrayList<>();
        groups= new ArrayList<>();
        Cursor cursor;
        String sql ="SELECT Data, COUNT(*) FROM CANCION GROUP BY Data";
        cursor= bd.sqlLiteDB.rawQuery(sql,null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Cursor cursor1;
                sql ="SELECT IdAlbum FROM CANCION WHERE Data=?";
                cursor1= Reproductor.bd.sqlLiteDB.rawQuery(sql,new String[]{cursor.getString(0)});
                String album="";
                if(cursor1.moveToFirst()) {
                    album = cursor1.getString(0);
                }
                cursor1.close();
                sql ="SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
                cursor1= Reproductor.bd.sqlLiteDB.rawQuery(sql,new String[]{album});
                String imaxe="";
                if(cursor1.moveToFirst()) {
                    imaxe = cursor1.getString(0);
                }
                cursor1.close();
                Bitmap image= BitmapFactory.decodeFile(imaxe);
                groups.add(cursor.getString(0));
                lista1.add(new Group(cursor.getString(0),cursor.getString(1)+" "+getResources().getString(R.string.songs),image));
                cursor.moveToNext();
            }
        }
        cursor.close();
        ListView list = (ListView) findViewById(R.id.list1);
        ArrayAdapter<Group> arrayAdapter = new CustomListGroup(this,lista1);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            cancions= bd.obterCancionsData(groups.get(position));
            if(cancions!=null) {
                lista = new ArrayList<>();
                rutas = new ArrayList<>();
                actpos=0;

                for (Cancion c : cancions) {
                    Cursor cursor12;
                    String sql1 = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                    cursor12 = bd.sqlLiteDB.rawQuery(sql1, new String[]{c.getIdArtista()});
                    if (cursor12.moveToFirst())
                        lista.add(cursor12.getString(0) + " - " + c.getTitulo());
                    else
                        lista.add(c.getTitulo());
                    cursor12.close();
                    rutas.add(c.getRuta());
                }
                rutaActual = rutas.get(actpos);
            }
            if(lista!=null) {
                TextView texto = (TextView) findViewById(R.id.text_song);
                texto.setText(lista.get(actpos));
                updateView();
                reproducir();
                mediaplayer.pause();
                updateProgress();
                getArtist();
                fillList();
            }
        });
        list.setSelectionAfterHeaderView();
    }

    public void groupArtist(){
        try {
            ArrayList<Group> lista1 = new ArrayList<>();
            groups = new ArrayList<>();
            completed1=false;
            Thread t1= new Thread(() -> {
                Cursor cursor;
                String sql = "SELECT IdArtista,(Select Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=IdArtista), COUNT(*), IdAlbum FROM CANCION GROUP BY IdArtista";
                cursor = bd.sqlLiteDB.rawQuery(sql, null);
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        Cursor cursor1;
                        sql = "SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
                        cursor1 = Reproductor.bd.sqlLiteDB.rawQuery(sql, new String[]{cursor.getString(3)});
                        String imaxe = "";
                        if (cursor1.moveToFirst()) {
                            imaxe = cursor1.getString(0);
                        }
                        cursor1.close();
                        groups.add(cursor.getString(0));
                        Bitmap image = BitmapFactory.decodeFile(imaxe);
                        lista1.add(new Group(cursor.getString(1), cursor.getString(2) + " " + getResources().getString(R.string.songs), image));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
                completed1=true;
            });
            t1.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(completed1) {
                ListView list = (ListView) findViewById(R.id.list1);
                ArrayAdapter<Group> arrayAdapter = new CustomListGroup(this, lista1);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                list.setAdapter(arrayAdapter);

                list.setOnItemClickListener((parent, view, position, id) -> {
                    cancions = bd.obterCancionsArtista(groups.get(position));
                    if (cancions != null) {
                        lista = new ArrayList<>();
                        rutas = new ArrayList<>();
                        actpos = 0;

                        for (Cancion c : cancions) {
                            lista.add(c.getTitulo());
                            rutas.add(c.getRuta());
                        }
                        rutaActual = rutas.get(actpos);
                    }
                    if (lista != null) {
                        TextView texto = (TextView) findViewById(R.id.text_song);
                        texto.setText(lista.get(actpos));
                        updateView();
                        reproducir();
                        updateProgress();
                        getArtist();
                        mediaplayer.pause();
                        fillList();
                    }
                });
                list.setSelectionAfterHeaderView();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void groupAlbum(){
        ArrayList<Group> lista1= new ArrayList<>();
        groups= new ArrayList<>();
        Cursor cursor;
        String sql ="SELECT IdAlbum,(Select Nome_Album FROM ALBUM WHERE ID_ALBUM=IdAlbum), COUNT(*) FROM CANCION GROUP BY IdAlbum";
        cursor= bd.sqlLiteDB.rawQuery(sql,null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Cursor cursor1;
                sql ="SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
                cursor1= Reproductor.bd.sqlLiteDB.rawQuery(sql,new String[]{cursor.getString(0)});
                String imaxe="";
                if(cursor1.moveToFirst()) {
                    imaxe = cursor1.getString(0);
                }
                cursor1.close();
                groups.add(cursor.getString(0));
                Bitmap image= BitmapFactory.decodeFile(imaxe);
                lista1.add(new Group(cursor.getString(1), cursor.getString(2) +" "+getResources().getString(R.string.songs),image));
                cursor.moveToNext();
            }
        }
        cursor.close();
        ListView list = (ListView) findViewById(R.id.list1);
        ArrayAdapter<Group> arrayAdapter = new CustomListGroup(this,lista1);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            cancions= bd.obterCancionsAlbum(groups.get(position));
            if(cancions!=null) {
                lista = new ArrayList<>();
                rutas = new ArrayList<>();
                actpos=0;

                for (Cancion c : cancions) {
                    Cursor cursor12;
                    String sql1 = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                    cursor12 = bd.sqlLiteDB.rawQuery(sql1, new String[]{c.getIdArtista()});
                    if (cursor12.moveToFirst())
                        lista.add(cursor12.getString(0) + " - " + c.getTitulo());
                    else
                        lista.add(c.getTitulo());
                    rutas.add(c.getRuta());
                    cursor12.close();
                }
                rutaActual = rutas.get(actpos);
            }
            if(lista!=null) {
                TextView texto = (TextView) findViewById(R.id.text_song);
                texto.setText(lista.get(actpos));
                updateView();
                reproducir();
                updateProgress();
                getArtist();
                mediaplayer.pause();
                fillList();
            }
        });
        list.setSelectionAfterHeaderView();
    }

    public void onSearchClick(View v){
        AlertDialog.Builder venta= new AlertDialog.Builder(this);

        venta.setTitle(getResources().getString(R.string.int_search));
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        venta.setView(input);
        venta.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        venta.setPositiveButton(getResources().getString(R.string.search), (dialog, which) -> {
            search=input.getText().toString();
            cancions= bd.obterCancionsQuery(search);
            if(cancions!=null) {
                lista = new ArrayList<>();
                rutas = new ArrayList<>();
                actpos=0;

                for (Cancion c : cancions) {
                    Cursor cursor;
                    String sql = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                    cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{c.getIdArtista()});
                    if (cursor.moveToFirst())
                        lista.add(cursor.getString(0) + " - " + c.getTitulo());
                    else
                        lista.add(c.getTitulo());
                    rutas.add(c.getRuta());
                    cursor.close();
                }
                rutaActual = rutas.get(actpos);
                if(lista!=null) {
                    TextView texto = (TextView) findViewById(R.id.text_song);
                    texto.setText(lista.get(actpos));
                    updateView();
                    reproducir();
                    updateProgress();
                    getArtist();
                    mediaplayer.pause();
                    fillList();
                }
            }
            else
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_found), Toast.LENGTH_LONG).show();
        });
        venta.create().show();
    }

    public void onDownloadClick(View v){
        AlertDialog.Builder venta= new AlertDialog.Builder(this);

        venta.setTitle(getResources().getString(R.string.int_down));
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        venta.setView(input);
        venta.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        venta.setPositiveButton(getResources().getString(R.string.down), (dialog, which) -> {
            url=input.getText().toString();
            descargar();
        });
        venta.create().show();
    }

    public void onAddClick(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 101);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void onBackClick(View v){
        if(!previous.equals("")) {
            switch (previous) {
                case "groupAlbum":
                    groupAlbum();
                    break;
                case "groupArtist":
                    groupArtist();
                    break;
                case "groupDate":
                    groupDate();
                    break;
                case "list":
                    onListClick(v);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if(!previous.equals("")) {
            switch (previous) {
                case "groupAlbum":
                    groupAlbum();
                    break;
                case "groupArtist":
                    groupArtist();
                    break;
                case "groupDate":
                    groupDate();
                    break;
                case "list":
                    onListClick(getCurrentFocus());
                    break;
            }
        }

        this.doubleBackToExitPressedOnce = true;

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 1000);
    }

    public void onListClick(View v){
        ArrayList<Group> lista1 = new ArrayList<>();

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.add);
        Bitmap image1 = BitmapFactory.decodeResource(getResources(), R.drawable.remove);
        lista1.add(new Group(getResources().getString(R.string.new_list),"  ",image));
        lista1.add(new Group(getResources().getString(R.string.delete),"  ",image1));
        previous="list";

        groups = new ArrayList<>();
        groups.add("");
        groups.add("");
        String idalbum = "";
        String count="";
        Cursor cursor;
        String sql = "SELECT ID_LISTA, Nome_Lista FROM LISTA";
        cursor = bd.sqlLiteDB.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Cursor cursor1;
                sql = "SELECT IdAlbum,COUNT(*) FROM CANCION INNER JOIN LISTACANCION ON IdCancion=ID_CANCION WHERE IdLista=?";
                cursor1 = Reproductor.bd.sqlLiteDB.rawQuery(sql, new String[]{cursor.getString(0)});

                if (cursor1.moveToFirst()) {
                    if(cursor1.getString(0)==null) {
                        idalbum = "";
                        count= "0";
                    }
                    else {
                        idalbum = cursor1.getString(0);
                        count = cursor1.getString(1);
                    }
                }
                cursor1.close();
                String imaxe = "";
                if(!idalbum.equals("")) {
                    sql = "SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
                    cursor1 = Reproductor.bd.sqlLiteDB.rawQuery(sql, new String[]{idalbum});
                    if (cursor1.moveToFirst()) {
                        imaxe = cursor1.getString(0);
                    }
                }
                cursor1.close();
                groups.add(cursor.getString(0));
                image = BitmapFactory.decodeFile(imaxe);
                lista1.add(new Group(cursor.getString(1), count + " " + getResources().getString(R.string.songs), image));
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (lista != null) {
            ListView list = (ListView) findViewById(R.id.list1);
            ArrayAdapter<Group> arrayAdapter = new CustomListGroup(this, lista1);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            list.setAdapter(arrayAdapter);

            list.setOnItemClickListener((parent, view, position, id) -> {
                if(position==0){
                    AlertDialog.Builder venta= new AlertDialog.Builder(Reproductor.this);

                    venta.setTitle(getResources().getString(R.string.create_list));
                    final EditText input = new EditText(Reproductor.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    venta.setView(input);
                    venta.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
                    venta.setPositiveButton(getResources().getString(R.string.create), (dialog, which) -> {
                        String listaid=(randomID());

                        String sql1 = "SELECT ID_LISTA FROM LISTA WHERE Nome_Lista=?";
                        Cursor cursor12 = bd.sqlLiteDB.rawQuery(sql1, new String[]{""+input.getText()});
                        if (!cursor12.moveToFirst()) {
                            bd.sqlLiteDB.execSQL("INSERT INTO LISTA (ID_LISTA,Nome_Lista) VALUES ('" + listaid + "','" + input.getText() + "')");
                            onListClick(v);
                        }
                        else
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.ex_list),Toast.LENGTH_LONG).show();
                        cursor12.close();
                    });
                    venta.create().show();
                }
                else if(position==1){
                    AlertDialog.Builder venta= new AlertDialog.Builder(Reproductor.this);

                    venta.setTitle(getResources().getString(R.string.add_title));
                    final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(Reproductor.this, R.layout.list_dialog_remove, R.id.text_dialog);
                    String sql12 = "SELECT ID_LISTA,Nome_Lista FROM LISTA";
                    Cursor cursor13 = bd.sqlLiteDB.rawQuery(sql12, new String[]{});
                    ArrayList<String> ids= new ArrayList<>();
                    if(cursor13.moveToFirst()){
                        while (!cursor13.isAfterLast()){
                            ids.add(cursor13.getString(0));
                            arrayAdapter1.add(cursor13.getString(1));
                            cursor13.moveToNext();
                        }
                    }
                    cursor13.close();

                    venta.setAdapter(arrayAdapter1, (dialog, which) -> {
                        bd.sqlLiteDB.execSQL("DELETE FROM LISTA WHERE ID_LISTA = '" + ids.get(which) + "'");
                        onListClick(v);
                    });
                    venta.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
                    venta.create().show();
                }
                else {
                    if (bd.obterCancionsLista(groups.get(position)) != null) {
                        cancions = bd.obterCancionsLista(groups.get(position));
                        lista = new ArrayList<>();
                        rutas = new ArrayList<>();
                        actpos = 0;

                        for (Cancion c : cancions) {
                            Cursor cursor13;
                            String sql12 = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                            cursor13 = bd.sqlLiteDB.rawQuery(sql12, new String[]{c.getIdArtista()});
                            if (cursor13.moveToFirst())
                                lista.add(cursor13.getString(0) + " - " + c.getTitulo());
                            else
                                lista.add(c.getTitulo());
                            rutas.add(c.getRuta());
                            cursor13.close();
                        }
                        rutaActual = rutas.get(actpos);
                        if (lista != null) {
                            TextView texto = (TextView) findViewById(R.id.text_song);
                            texto.setText(lista.get(actpos));
                            updateView();
                            reproducir();
                            updateProgress();
                            getArtist();
                            mediaplayer.pause();
                            fillList();
                        }
                    }
                }
            });
            list.setSelectionAfterHeaderView();
        }
    }

    public void descargar(){
        URL uri;

        try {
            uri= new URL(url);
        } catch (Exception e1) {
            Toast.makeText(getApplicationContext(),getResources().getText(R.string.ex_url), Toast.LENGTH_LONG).show();
            e1.printStackTrace();
            return;
        }
        Thread t1= new Thread(){
            public void run(){
                descargado=false;
                HttpURLConnection conn;
                String nomeArquivo = Uri.parse(""+uri).getLastPathSegment();
                ruta= new File(getFilesDir()+"//musica//"+nomeArquivo);
                new File(getFilesDir()+"//musica//").mkdirs();
                try {

                    conn = (HttpURLConnection) uri.openConnection();
                    conn.setReadTimeout(10000);  	/* milliseconds */
                    conn.setConnectTimeout(150000);  /* milliseconds */
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);			/* Indicamos que a conexión vai recibir datos */

                    conn.connect();

                    int response = conn.getResponseCode();
                    if (response != HttpURLConnection.HTTP_OK){
                        return;
                    }
                    OutputStream os = new FileOutputStream(ruta);
                    InputStream in = conn.getInputStream();
                    byte[] data = new byte[1024];	// Buffer a utilizar
                    int count;
                    while ((count = in.read(data)) != -1) {
                        os.write(data, 0, count);
                    }
                    os.flush();
                    os.close();
                    in.close();
                    conn.disconnect();
                    Log.i("Arquivo descargado en: ", ruta.getAbsolutePath());
                    descargado=true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(descargado){
            if(ruta!=null) {
                try {
                    MediaMetadataRetriever retrieve = new MediaMetadataRetriever();

                    retrieve.setDataSource(ruta.getPath());
                    String hasAudio = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
                    boolean isAudio = "yes".equals(hasAudio);
                    if(isAudio) {
                        String album = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        String artist = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String title = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String duration = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String date = retrieve.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                        String path = ruta.getPath();
                        byte[] image = retrieve.getEmbeddedPicture();
                        if (album == null)
                            album = "Desconocido";
                        if (artist == null)
                            artist = "Desconocido";
                        if (date == null)
                            date = "Desconocido";
                        if (title == null)
                            title = ruta.getName();
                        if (image == null) {
                            Bitmap src = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.default_img, null)).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            src.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            image = stream.toByteArray();
                            stream.close();
                        }
                        File newimage = new File(getFilesDir() + "/covers");
                        newimage.mkdirs();
                        newimage = new File(newimage.getPath() + "/" + randomID() + ".jpg");
                        String imagepath = newimage.getPath();
                        FileOutputStream fos = new FileOutputStream(newimage);
                        fos.write(image);
                        fos.close();
                        String albumid = (randomID());
                        String artistid = (randomID());
                        String cancionid = (randomID());
                        Cursor cursor;

                        String sql = "SELECT ID_ALBUM FROM ALBUM WHERE Nome_Album=?";
                        cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{album});
                        if (!cursor.moveToFirst())
                            bd.sqlLiteDB.execSQL("INSERT INTO ALBUM (ID_ALBUM,Nome_Album,Imaxe) VALUES ('" + albumid + "','" + album + "','" + imagepath + "')");
                        else
                            albumid = cursor.getString(0);
                        cursor.close();

                        sql = "SELECT ID_ARTISTA FROM ARTISTA WHERE Nome_Artista=?";
                        cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{artist});
                        if (!cursor.moveToFirst())
                            bd.sqlLiteDB.execSQL("INSERT INTO ARTISTA (ID_ARTISTA,Nome_Artista) VALUES ('" + artistid + "','" + artist + "')");
                        else
                            artistid = cursor.getString(0);
                        cursor.close();

                        sql = "SELECT ID_CANCION FROM CANCION WHERE Ruta=?";
                        cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{path});
                        if (!cursor.moveToFirst())
                            bd.sqlLiteDB.execSQL("INSERT INTO CANCION (ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum) VALUES " +
                                    "('" + cancionid + "',?,'" + date + "','" + Integer.parseInt(duration) + "',?,'" + artistid + "','" + albumid + "')", new String[]{title, path});
                        cursor.close();
                    } else{
                        Toast.makeText(getApplicationContext(),getResources().getText(R.string.ex_file), Toast.LENGTH_LONG).show();
                    }
                    TextView text = (TextView) findViewById(R.id.txt_empty);
                    Button btn = (Button) findViewById(R.id.btn_add);
                    text.setVisibility(View.GONE);
                    btn.setVisibility(View.GONE);
                    cancions = bd.obterCancions();
                    lista = new ArrayList<>();
                    rutas = new ArrayList<>();
                    String lista_nombre="Default";
                    String listaid=(randomID());

                    String sql = "SELECT ID_LISTA FROM LISTA WHERE Nome_Lista=?";
                    Cursor cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{lista_nombre});
                    if (!cursor.moveToFirst())
                        bd.sqlLiteDB.execSQL("INSERT INTO LISTA (ID_LISTA,Nome_Lista) VALUES ('" + listaid + "','" + lista_nombre + "')");
                    else
                        listaid = cursor.getString(0);
                    cursor.close();
                    for (Cancion c : cancions) {
                        sql = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                        cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{c.getIdArtista()});
                        if (cursor.moveToFirst())
                            lista.add(cursor.getString(0) + " - " + c.getTitulo());
                        else
                            lista.add(c.getTitulo());
                        rutas.add(c.getRuta());
                        cursor.close();
                        if(!listaid.equals(""))
                            bd.sqlLiteDB.execSQL("INSERT OR IGNORE INTO LISTACANCION (IdLista,IdCancion) VALUES ('" + listaid + "','" + c.getIdCancion() + "')");
                        if(lista!=null)
                            fillList();
                    }
                    intent = new Intent(getApplicationContext(), Reproductor.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    getArtist();
                    showNotification(this, artist,cancions.get(actpos).getTitulo(), intent, reqCode,time,0,0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else{
            Toast.makeText(getApplicationContext(),getResources().getText(R.string.ex_down), Toast.LENGTH_LONG).show();
        }
    }
}