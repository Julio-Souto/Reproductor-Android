package com.example.proxecto_juliosb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class ReproductorActivity extends AppCompatActivity {

    public static BaseDatos bd;
    String imaxe="";
    Bridge bridge;
    int reqCode = 1;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);

        bridge= new Bridge();
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        intent = new Intent(getApplicationContext(), Reproductor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(bd==null) {
            bd = new BaseDatos(getApplicationContext(), "DATOS", null, 1);
            bd.sqlLiteDB = bd.getWritableDatabase();
        }
        if(Reproductor.cancions!=null) {
            changeImage();
            updateProgress();
            getArtist();

            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            TextView texto = (TextView) findViewById(R.id.text_activity);
            texto.setText(Reproductor.lista.get(Reproductor.actpos));
            if (Reproductor.mediaplayer.isPlaying())
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
        }
        Reproductor.mediaplayer.setOnCompletionListener(mp -> {
            if(Reproductor.cancions!=null) {
                if ((Reproductor.actpos + 1) < Reproductor.cancions.size()) {
                    Reproductor.actpos = Reproductor.actpos + 1;
                    Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                    changeImage();
                    reproducir();
                    updateProgress();
                    updateView();
                    getArtist();
                    TextView texto = (TextView) findViewById(R.id.text_activity);
                    texto.setText(Reproductor.lista.get(Reproductor.actpos));
                }
                else if(Reproductor.random_active&&Reproductor.actpos+1==Reproductor.lista.size()){
                    Reproductor.actpos=0;
                    Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                    changeImage();
                    reproducir();
                    updateProgress();
                    updateView();
                    getArtist();
                    TextView texto = (TextView) findViewById(R.id.text_activity);
                    texto.setText(Reproductor.lista.get(Reproductor.actpos));
                }
                else{
                    MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                }
            }
        });
        SeekBar progress= (SeekBar) findViewById(R.id.progress);
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(Reproductor.mediaplayer != null && fromUser){
                    Reproductor.mediaplayer.seekTo(progress * 1000);
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

    public void updateView(){
        MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
        SeekBar progress = (SeekBar) findViewById(R.id.progress);
        TextView text1= (TextView) findViewById(R.id.time1);
        btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
        text1.setText("00:00");
        progress.setProgress(0);
    }

    public void getArtist(){
        String sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
        Cursor cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{Reproductor.cancions.get(Reproductor.actpos).getIdArtista()});
        if(cursor.moveToFirst())
            Reproductor.artist=cursor.getString(0);
        else
            Reproductor.artist="Desconocido";
        cursor.close();
    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode, String time, int max, int progress) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, 0);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        changeImage();
        remoteViews.setImageViewBitmap(R.id.cover,BitmapFactory.decodeFile(imaxe));
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
        notification.flags=Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notificationManager.notify(reqCode, notification); // 0 is the request code, it should be unique id
    }

    public void changeImage(){
        Cursor cursor;
        String sql ="SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
        cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{Reproductor.cancions.get(Reproductor.actpos).getIdAlbum()});
        if(cursor.moveToFirst()) {
            imaxe = cursor.getString(0);
        }
        cursor.close();
        Bitmap image= BitmapFactory.decodeFile(imaxe);
        ImageView view=(ImageView) findViewById(R.id.cover);
        view.setImageBitmap(image);
    }

    public String randomID(){
        return ""+ UUID.randomUUID().toString().replace("-", "").substring(0,15);
    }

    public void updateProgress(){
        Thread t1= new Thread(() -> {
            SeekBar progress=(SeekBar) findViewById(R.id.progress);
            progress.setMax(Reproductor.mediaplayer.getDuration()/1000);
            int prog = (Reproductor.mediaplayer.getCurrentPosition())/ 1000;
            progress.setProgress(prog);
            Message msg = new Message();
            msg.arg1=2;
            bridge.sendMessage(msg);
            msg = new Message();
            msg.arg1=1;
            bridge.sendMessage(msg);
            while(Reproductor.mediaplayer.isPlaying()){
                prog = (Reproductor.mediaplayer.getCurrentPosition())/ 1000;
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
                String seconds=""+(Reproductor.mediaplayer.getCurrentPosition()/1000%60);
                String minutes=""+(Reproductor.mediaplayer.getCurrentPosition()/60000);
                if (seconds.length()<2)
                    seconds="0"+seconds;
                if(minutes.length()<2)
                    minutes="0"+minutes;
                if((Reproductor.mediaplayer.getCurrentPosition()/60000)==(Reproductor.mediaplayer.getDuration()/60000)){
                    if((Reproductor.mediaplayer.getCurrentPosition()/1000%60)>(Reproductor.mediaplayer.getDuration()/1000%60)) {
                        seconds = "" + (Reproductor.mediaplayer.getDuration() / 1000 % 60);
                        if (seconds.length() < 2)
                            seconds = "0" + seconds;
                    }
                }
                text1.setText(minutes+":"+seconds);
                showNotification(ReproductorActivity.this, Reproductor.artist,Reproductor.cancions.get(Reproductor.actpos).getTitulo(), intent, reqCode,
                        minutes+":"+seconds, Reproductor.mediaplayer.getDuration()/1000, Reproductor.mediaplayer.getCurrentPosition() / 1000);
            }
            if(msg.arg1==2){
                TextView time2=(TextView) findViewById(R.id.time2);
                String seconds=""+(Reproductor.mediaplayer.getDuration()/1000%60);
                String minutes=""+(Reproductor.mediaplayer.getDuration()/60000);
                if (seconds.length()<2)
                    seconds="0"+seconds;
                if(minutes.length()<2)
                    minutes="0"+minutes;
                time2.setText(minutes+":"+seconds);
            }
        }
    }

    public void onBackClick(View v){
        finish();
    }

    public void onMoreCLick(View v){
        PopupMenu popup = new PopupMenu(this, v);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            if(Reproductor.cancions!=null) {
                if (item.getTitle().equals(getResources().getString(R.string.add_list))) {
                    createDialog();
                }
//                    else if (item.getTitle().equals(getResources().getString(R.string.rem_list))) {
//                        AlertDialog.Builder venta= new AlertDialog.Builder(ReproductorActivity.this);
//
//                        String idcancion=Reproductor.cancions.get(Reproductor.actpos).getIdCancion();
//                        venta.setTitle(getResources().getString(R.string.add_title));
//                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReproductorActivity.this, R.layout.list_dialog_remove, R.id.text_dialog);
//                        String sql = "SELECT DISTINCT ID_LISTA,Nome_Lista FROM LISTA INNER JOIN LISTACANCION ON IdLista=ID_LISTA WHERE IdCancion LIKE ?";
//                        Cursor cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{idcancion});
//                        ArrayList<String> ids= new ArrayList<>();
//                        if(cursor.moveToFirst()){
//                            while (!cursor.isAfterLast()){
//                                ids.add(cursor.getString(0));
//                                arrayAdapter.add(cursor.getString(1));
//                                cursor.moveToNext();
//                            }
//                        }
//
//                        venta.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                bd.sqlLiteDB.execSQL("DELETE FROM LISTACANCION WHERE IdCancion = '" + idcancion + "' AND IdLista = '" + ids.get(which) + "'");
//                            }
//                        });
//                        venta.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        venta.create().show();
//                    }
               else if (item.getTitle().equals(getResources().getString(R.string.edit))) {
                    Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                    startActivity(intent);
                }
            }
            return true;
        });

        popup.show();
    }

    public void createDialog(){
        AlertDialog.Builder venta= new AlertDialog.Builder(ReproductorActivity.this);

        String idcancion=Reproductor.cancions.get(Reproductor.actpos).getIdCancion();
        venta.setTitle(getResources().getString(R.string.add_title));
        ArrayList<String> names= new ArrayList<>();
        names.add(getResources().getString(R.string.new_list));
        String sql = "SELECT ID_LISTA,Nome_Lista FROM LISTA WHERE ID_LISTA NOT IN " +
                "(SELECT distinct ID_LISTA FROM LISTA INNER JOIN LISTACANCION ON IdLista=ID_LISTA WHERE IdCancion LIKE ?)";
        Cursor cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{idcancion});
        ArrayList<String> ids= new ArrayList<>();
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                names.add(cursor.getString(1));
                cursor.moveToNext();
            }
        }
        cursor.close();
        sql = "SELECT ID_LISTA,Nome_Lista FROM LISTA";
        cursor = bd.sqlLiteDB.rawQuery(sql, new String[]{});
        ArrayList<String> names1= new ArrayList<>();
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ids.add(cursor.getString(0));
                names1.add(cursor.getString(1));
                cursor.moveToNext();
            }
        }
        cursor.close();
        boolean[] bools= new boolean[names1.size()+1];
        String[] chars= new String[names1.size()+1];
        bools[0]=true;
        chars[0]=names.get(0);
        int i=0;
        for(String s:names1){
            bools[i+1]= !names.contains(s);
            chars[i+1]=s;
            i++;
        }

        venta.setMultiChoiceItems(chars, bools, (dialog, which, isChecked) -> {
            if(which==0){
                ((AlertDialog) dialog).getListView().setItemChecked(which,true);
                AlertDialog.Builder venta1 = new AlertDialog.Builder(ReproductorActivity.this);

                venta1.setTitle(getResources().getString(R.string.create_list));
                final EditText input = new EditText(ReproductorActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                venta1.setView(input);
                venta1.setNegativeButton(getResources().getString(R.string.cancel), (dialog12, which1) -> dialog12.dismiss());
                venta1.setPositiveButton(getResources().getString(R.string.create), (dialog2, which12) -> {
                    String listaid=randomID();

                    String sql1 = "SELECT ID_LISTA FROM LISTA WHERE Nome_Lista=?";
                    Cursor cursor1 = bd.sqlLiteDB.rawQuery(sql1, new String[]{""+input.getText()});
                    if (!cursor1.moveToFirst()) {
                        bd.sqlLiteDB.execSQL("INSERT INTO LISTA (ID_LISTA,Nome_Lista) VALUES ('" + listaid + "','" + input.getText() + "')");
                    }
                    else
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.ex_list),Toast.LENGTH_LONG).show();
                    cursor1.close();
                    dialog2.dismiss();
                    dialog.dismiss();
                    createDialog();
                });
                venta1.create().show();
            }
            else{
                if(isChecked){
                    bd.sqlLiteDB.execSQL("INSERT INTO LISTACANCION (IdLista,IdCancion) VALUES ('" + ids.get(which-1) + "','" + idcancion + "')");
                }
                else{
                    bd.sqlLiteDB.execSQL("DELETE FROM LISTACANCION WHERE IdCancion = '" + idcancion + "' AND IdLista = '" + ids.get(which-1) + "'");
                }
            }

        });
        venta.setNegativeButton(getResources().getString(R.string.edit_btn), (dialog, which) -> dialog.dismiss());
        venta.create().show();
    }

    public void onButtonBack(View v){
        if(Reproductor.cancions!=null) {
            if(Reproductor.mediaplayer.getCurrentPosition()>5000) {
                Reproductor.mediaplayer.seekTo(0);
            }
            else {
                MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
                if ((Reproductor.actpos - 1) >= 0) {
                    Reproductor.actpos = Reproductor.actpos - 1;
                    Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                    TextView texto = (TextView) findViewById(R.id.text_activity);
                    texto.setText(Reproductor.lista.get(Reproductor.actpos));
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    changeImage();
                    Reproductor.reproducir();
                    updateProgress();
                    getArtist();
                }
                else if(Reproductor.random_active&&Reproductor.actpos==0){
                    Reproductor.actpos=Reproductor.lista.size()-1;
                    Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                    TextView texto = (TextView) findViewById(R.id.text_activity);
                    texto.setText(Reproductor.lista.get(Reproductor.actpos));
                    btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                    changeImage();
                    Reproductor.reproducir();
                    updateProgress();
                    getArtist();
                }
            }
        }
    }

    public void onButtonPause(View v){
        if(Reproductor.cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            if (Reproductor.mediaplayer.isPlaying()) {
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                Reproductor.mediaplayer.pause();
            } else {
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                Reproductor.mediaplayer.start();
                updateProgress();
                getArtist();
            }
        }
    }

    public void onButtonForw(View v){
        if(Reproductor.cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_pause);
            if ((Reproductor.actpos + 1) < Reproductor.cancions.size()) {
                Reproductor.actpos = Reproductor.actpos + 1;
                Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                TextView texto = (TextView) findViewById(R.id.text_activity);
                texto.setText(Reproductor.lista.get(Reproductor.actpos));
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                changeImage();
                reproducir();
                updateProgress();
                getArtist();
            }
            else if(Reproductor.random_active&&Reproductor.actpos+1==Reproductor.lista.size()){
                Reproductor.actpos=0;
                Reproductor.rutaActual = Reproductor.rutas.get(Reproductor.actpos);
                TextView texto = (TextView) findViewById(R.id.text_activity);
                texto.setText(Reproductor.lista.get(Reproductor.actpos));
                btn.setIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.play));
                changeImage();
                reproducir();
                updateProgress();
                getArtist();
            }
        }
    }

    public void onButtonRandom(View v){
        if(Reproductor.cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_random);
            if(Reproductor.random_active){
                btn.setIconTintResource(R.color.white);
                Reproductor.cancions=new ArrayList<>(Reproductor.randSong);
                Reproductor.lista=new ArrayList<>(Reproductor.rand);
                Reproductor.rutas=new ArrayList<>(Reproductor.randRoute);
                Reproductor.random_active=false;
            }
            else{
                btn.setIconTintResource(R.color.purple_200);
                Reproductor.randSong=new ArrayList<>(Reproductor.cancions);
                Reproductor.rand=new ArrayList<>(Reproductor.lista);
                Reproductor.randRoute=new ArrayList<>(Reproductor.rutas);
                int r= new Random().nextInt();
                Collections.shuffle(Reproductor.cancions,new Random(r));
                Collections.shuffle(Reproductor.lista,new Random(r));
                Collections.shuffle(Reproductor.rutas,new Random(r));
                Reproductor.random_active=true;
            }
        }
    }

    public void onButtonLoop(View v){
        if(Reproductor.cancions!=null) {
            MaterialButton btn = (MaterialButton) findViewById(R.id.btn_loop);
            if(Reproductor.loop_active){
                btn.setIconTintResource(R.color.white);
                Reproductor.mediaplayer.setLooping(false);
                Reproductor.loop_active=false;
            }
            else{
                btn.setIconTintResource(R.color.purple_200);
                Reproductor.mediaplayer.setLooping(true);
                Reproductor.loop_active=true;
            }
        }
    }

    public static void reproducir(){
        try {
            Reproductor.mediaplayer.reset();
            Reproductor.mediaplayer.setDataSource(Reproductor.rutaActual);
            Reproductor.mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Reproductor.mediaplayer.prepare();
            Reproductor.mediaplayer.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(Reproductor.lista!=null) {
            TextView texto = (TextView) findViewById(R.id.text_activity);
            texto.setText(Reproductor.lista.get(Reproductor.actpos));
            getArtist();
        }

        if(Reproductor.mediaplayer!=null) {
            if(Reproductor.loop_active){
                MaterialButton btn = (MaterialButton) findViewById(R.id.btn_loop);
                btn.setIconTintResource(R.color.purple_200);
            }
            if(Reproductor.random_active){
                MaterialButton btn = (MaterialButton) findViewById(R.id.btn_random);
                btn.setIconTintResource(R.color.purple_200);
            }
        }

        if(Reproductor.mediaplayer.isPlaying())
            updateProgress();

        if(Reproductor.created) {
            if (bd == null) {
                bd = new BaseDatos(getApplicationContext(), "DATOS", null, 1);
                bd.sqlLiteDB = bd.getWritableDatabase();
            }
        }

    }
}