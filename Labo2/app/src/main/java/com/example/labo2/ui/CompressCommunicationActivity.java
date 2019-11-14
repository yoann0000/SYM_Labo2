package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.labo2.R;
import com.example.labo2.ui.eventListener.CommunicationEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class CompressCommunicationActivity extends Activity {
    private TextView envoiLabel = null;
    private TextView reponseLabel = null;
    private EditText message = null;
    private EditText reponse = null;
    private Button envoiBouton = null;
    private Button retour = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);

        this.envoiLabel = findViewById(R.id.envoi);
        this.reponseLabel = findViewById(R.id.reception);
        this.message = findViewById(R.id.send);
        this.reponse = findViewById(R.id.received);
        this.envoiBouton = findViewById(R.id.env);
        this.retour = findViewById(R.id.retour);

        envoiBouton.setOnClickListener((v) -> {
            long start =  System.currentTimeMillis();
            SymComManagerCompress scm = new SymComManagerCompress();
            scm.setCommunicationEventListener(
                    response -> {
                        // Code de traitement de la réponse – dans le UI-Thread
                        if(response != null){
                            reponse.setText(response);
                            long end =  System.currentTimeMillis();
                            System.out.println("Temps : " + (end - start));
                            return true;
                        }
                        return false;
                    });
            try {
                scm.sendRequest(message.getText().toString(), "http://sym.iict.ch/rest/txt");
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
            message.setText("");
        });

        retour.setOnClickListener((v) -> {
            finish();
        });
    }

    private class SymComManagerCompress extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String... strings) {
            URL obj = null;
            try {
                obj = new URL(strings[1]);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("X-Network", "CSD");
                connection.setRequestProperty("X-Content-Encoding", "deflate");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setDoOutput(true);
                Deflater def = new Deflater(Deflater.BEST_COMPRESSION, true);
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(
                        new DeflaterOutputStream(
                        connection.getOutputStream(), def), "UTF-8"));
                os.append(strings[0]);
                os.close();
                System.out.println(connection.getResponseCode());
                System.out.println(connection.getErrorStream());
                connection.getInputStream();

                if(connection.getResponseCode() > 199 && connection.getResponseCode() < 300){
                    Inflater inf = new Inflater(true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            new InflaterInputStream(
                                    connection.getInputStream(), inf)));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }else{
                    return "Erreur " + connection.getResponseCode();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            cel.handleServerResponse(result);
        }

        /**
         * Permet d'envoyer un document request vers le serveur désigné par ur
         * @param request Le texte mis
         * @param url L'URL du serveur à joindre
         */
        public void sendRequest(String request, String url) {
            this.execute(request, url);
        }

        public void setCommunicationEventListener (CommunicationEventListener l){
            this.cel = l;
        }
    }
}
