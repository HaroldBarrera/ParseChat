package com.example.parsechat;

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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

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

        if (listMensajes.isEmpty()){
            System.out.println("-------------------");
            System.out.println("LA LISTA ESTA VACIA");
            System.out.println("-------------------");
        }



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