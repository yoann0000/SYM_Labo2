package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import java.util.ArrayList;
import java.util.List;

public class GraphQLCommunicationActivity extends Activity {

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);
        this.spinner = findViewById(R.id.spinner);

        SymComManager scm = new SymComManager();
        scm.setCommunicationEventListener(
                response -> {
                    // Code de traitement de la réponse – dans le UI-Thread
                    if(true){
                        List<String> arrayList = new ArrayList<>();
                        arrayList.add("JAVA");
                        arrayList.add("ANDROID");
                        arrayList.add("C Language");
                        arrayList.add("CPP Language");
                        arrayList.add("Go Language");
                        arrayList.add("AVN SYSTEMS");
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(arrayAdapter);
                        return true;
                    }
                    return false;
                });
        try {
            scm.sendRequest("{allAuthors{id first_name last_name}}", "http://sym.iict.ch/api/graphql");
        } catch (Exception e) {
            System.out.println("Exception : " + e);
            e.printStackTrace();
        }


        spinner.setOnClickListener((v) -> { //{"query":"{allPostByAuthor(authorId: 1){title description}}"}
            SymComManager symComManager = new SymComManager();
            symComManager.setCommunicationEventListener(
                    response -> {
                        // Code de traitement de la réponse – dans le UI-Thread
                        if(response != null){
                            //response.setText(response);
                            return true;
                        }
                        return false;
                    });
            try {
                //symComManager.sendRequest(message.getText().toString(), "http://sym.iict.ch/rest/txt");
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
            //message.setText("");
        });

        //retour.setOnClickListener((v) -> finish());
    }

    public class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String... strings) {
            URL obj;
            try {
                obj = new URL(strings[1]);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Request", strings[0]);
                connection.setDoOutput(true);
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(
                        connection.getOutputStream(), "UTF-8"));
                os.append(strings[0]);
                os.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
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
