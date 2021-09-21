package com.example.parsechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "777";
    private CircleImageView fotoPerfil;
    private TextView nombre;
    private static RecyclerView rvMensajes;
    private EditText txtMensajes;
    private Button btnEnviar;

    private static AdapterMensajes adapter;
    private static List<Mensaje> listMensajes;

    private static ParseQuery<Mensaje> query;
    private static final int MAXIMO_DE_MENSAJES = 50;
    private static boolean mFirstLoad = true;

    public static final String CANALPRINCIPAL = "ParseChat";

    static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(3);

    Handler myHandler = new android.os.Handler();

    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            actualizarMensajes();
            myHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParsePush.subscribeInBackground(CANALPRINCIPAL);

        fotoPerfil = (CircleImageView) findViewById(R.id.fotoPerfil);
        nombre = (TextView) findViewById(R.id.nombre);
        rvMensajes = (RecyclerView) findViewById(R.id.rvMensajes);
        txtMensajes = (EditText) findViewById(R.id.txtMensaje);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        listMensajes = new ArrayList<>();

        adapter = new AdapterMensajes(this, listMensajes);
        LinearLayoutManager l = new LinearLayoutManager(this);
        //l.setReverseLayout(true);
        rvMensajes.setLayoutManager(l);
        rvMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //databaseReference.push().setValue(new Mensaje(txtMensajes.getText().toString(), nombre.getText().toString(), "", "1", "00:00"));
                //Base de datos ParseServer
                Mensaje m = new Mensaje();
                m.guardar(txtMensajes.getText().toString(), nombre.getText().toString(), "", "1", "00:00");
                adapter.addMensaje(m);
                listMensajes.add(m);
                m.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(MainActivity.this, "Successfully created message on Parse",
                                    Toast.LENGTH_SHORT).show();
                            actualizarMensajes();
                            HashMap<String,String> map = new HashMap<String, String>();
                            map.put("Mensaje", m.getMensaje());
                            ParseCloud.callFunctionInBackground("SendPush",map, new FunctionCallback<Object>() {

                                @Override
                                public void done(Object object, ParseException e) {
                                    if (e == null){
                                        System.out.println("yay " + object);
                                    }else{
                                        System.out.println("ERROR: " + e.getMessage());
                                        System.out.println("LOCALIZACION: " + e.getLocalizedMessage());
                                        System.out.println("CAUSA: " + e.getCause());
                                        System.out.println("CODIGO: " + e.getCode());
                                    }
                                }
                            });

                            //setScrollbar();
                        } else {
                            Log.e(MainActivity.class.getSimpleName(),"Failed to save message", e);
                        }
                    }
                });
                txtMensajes.setText("");
            }
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });

    }


    public static void actualizarMensajes(){
        query = ParseQuery.getQuery(Mensaje.class);
        query.setLimit(MAXIMO_DE_MENSAJES);
        query.orderByDescending("createdAt");

        query.findInBackground(new FindCallback<Mensaje>() {

            public void done(List<Mensaje> Mensajes, ParseException e) {

                if (e == null) {
                    listMensajes.clear();
                    listMensajes.addAll(Mensajes);
                    adapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (mFirstLoad) {
                        mFirstLoad = false;
                    }

                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only start checking for new messages when the app becomes active in foreground
        myHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);
    }
    
    @Override
    protected void onPause() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        myHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    private void setScrollbar(){
        rvMensajes.scrollToPosition(adapter.getItemCount() - 1);
    }

}