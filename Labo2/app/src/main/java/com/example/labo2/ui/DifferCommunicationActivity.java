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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DifferCommunicationActivity extends Activity {
    private EditText message = null;
    private int cap =  128;
    private Queue<String> messageBuffer = new LinkedBlockingQueue<>(cap);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_differ);

        TextView envoiLabel = findViewById(R.id.envoi);
        TextView reponseLabel = findViewById(R.id.reception);
        this.message = findViewById(R.id.send);
        EditText reponse = findViewById(R.id.received);
        Button envoiBouton = findViewById(R.id.env);
        Button retour = findViewById(R.id.retour);

        envoiBouton.setOnClickListener((v) -> {
            SymComManager scm = new SymComManager();
            scm.setCommunicationEventListener(
                    response -> {
                        // Code de traitement de la réponse – dans le UI-Thread
                        if(response != null){
                            reponse.setText(response);
                            return true;
                        }
                        return false;
                    });
            try {
                while (!messageBuffer.isEmpty()) {
                    String msg = messageBuffer.peek();
                    scm.sendRequest(msg, "http://sym.iict.ch/rest/txt");
                    messageBuffer.poll();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (InetAddress.getByName("http://sym.iict.ch/rest/txt").isReachable(10)) {
                    scm.sendRequest(message.getText().toString(), "http://sym.iict.ch/rest/txt");
                } else {
                    messageBuffer.add(message.getText().toString());
                }

            }
            catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
            message.setText("");
        });

        retour.setOnClickListener((v) -> finish());
    }

    private class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String... strings) {
            URL obj;
            try {
                obj = new URL(strings[1]);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                System.out.println(strings[0]);
                connection.setRequestProperty("Request", strings[0]);
                connection.setDoOutput(true);
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(
                        connection.getOutputStream(), "UTF-8"));
                os.append(strings[0]);
                os.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                os.close();
                return response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // print result
            return null;
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
        void sendRequest(String request, String url) {
            this.execute(request, url);
        }

        void setCommunicationEventListener (CommunicationEventListener l){
            this.cel = l;
        }
    }
}
