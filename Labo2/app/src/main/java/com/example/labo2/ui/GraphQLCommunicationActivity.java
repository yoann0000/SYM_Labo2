package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.labo2.R;
import com.example.labo2.ui.eventListener.CommunicationEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import java.util.LinkedList;
import java.util.List;

public class GraphQLCommunicationActivity extends Activity {

    private Spinner spinner;
    private Button button;
    private TextView textView;
    private LinkedList<Author> data;

    public void parseResp(String response) {
        JsonObject convertedObject = new Gson().fromJson(response, JsonObject.class);
        JsonArray authors = convertedObject.get("data").getAsJsonObject().get("allAuthors").getAsJsonArray();
        int id;
        String firstName;
        String lastName;
        for (JsonElement author : authors) {
            JsonObject authr = author.getAsJsonObject();
            id = authr.get("id").getAsInt();
            firstName = authr.get("first_name").getAsString();
            lastName = authr.get("last_name").getAsString();
            data.add(new Author(id, firstName, lastName));
        }
    }

    class Author{
        private int id;
        private String first_name;
        private String last_name;

        public Author(int id, String first_name, String last_name){
            this.id = id;
            this.first_name = first_name;
            this.last_name = last_name;
        }

        public String toString(){
            return last_name + " " + first_name;
        }

        public int getId(){
            return id;
        }
    }

    class Book{
        private String title;
        private String description;

        public Book(String title, String description){
            this.title = title;
            this.description = description;
        }

        public String toString(){
            return title + " : " + description + "\n\n";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphql);
        this.spinner = findViewById(R.id.spinner);
        this.button = findViewById(R.id.button);
        this.textView = findViewById(R.id.textView2);
        this.data = new LinkedList<>();

        SymComManager scm = new SymComManager();
        scm.setCommunicationEventListener(
                response -> {
                    // Code de traitement de la réponse – dans le UI-Thread
                    if(response != null){
                        parseResp(response);
                        System.out.println(response + " <------------------------");
                        List<String> arrayList = new ArrayList<>();
                        for (Author a : data) {
                            arrayList.add(a.toString());
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(arrayAdapter);
                        return true;
                    }
                    return false;
                });
        try {
            scm.sendRequest("{\"query\":\"{allAuthors{id first_name last_name}}\"}", "http://sym.iict.ch/api/graphql");
        } catch (Exception e) {
            System.out.println("Exception : " + e);
            e.printStackTrace();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        /*button.setOnClickListener(v -> { //{"query":"{allPostByAuthor(authorId: 1){title description}}"}

            SymComManager symComManager = new SymComManager();
            symComManager.setCommunicationEventListener(
                    response -> {
                        // Code de traitement de la réponse – dans le UI-Thread
                        if(response != null){
                            textView.setText(parseResp(response));
                            return true;
                        }
                        return false;
                    });
            String author = String.valueOf(spinner.getSelectedItem());
            String query = "allPostByAuthor(authorId: " + author + "){title description}";
            try {
                symComManager.sendRequest(query, "http://sym.iict.ch/rest/txt");
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
        });*/

        button.setOnClickListener((v) -> finish());
    }

    public class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String... strings) {
            URL obj;
            try {
                System.out.println(strings[0]);
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
