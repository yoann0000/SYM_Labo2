//Inspiré de https://www.journaldev.com/7148/java-httpurlconnection-example-java-http-request-get-post
package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.labo2.R;
import com.example.labo2.ui.eventListener.CommunicationEventListener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Classe s'occupant de la partie asynchrone
 */
public class AsynchronCommunicationActivity extends Activity {

    private TextView envoiLabel = null;
    private TextView reponseLabel = null;
    private EditText message = null;
    private EditText reponse = null;
    private Button envoiBouton = null;
    private Button retour = null;
    private SymComManager scm = new SymComManager();

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
            scm.setCommunicationEventListener(
                    response -> {
                        // Code de traitement de la réponse – dans le UI-Thread
                        if(response != null){
                            retour.setText(response);
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

    private class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String... strings) {
            URL obj = null;
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
                StringBuffer response = new StringBuffer();

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
        public void sendRequest(String request, String url) {
            this.execute(request, url);
        }

        public void setCommunicationEventListener (CommunicationEventListener l){
            this.cel = l;
        }
    }
}