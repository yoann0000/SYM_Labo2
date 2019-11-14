package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.labo2.R;
import com.example.labo2.ui.eventListener.CommunicationEventListener;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/*
// Serialize
JSONObject json = new JSONObject();
json.put("key", "value");
// ...
// "serialize"
Bundle bundle = new Bundle();
bundle.putString("json", json.toString());

// Deserialize
Bundle bundle = getBundleFromIntentOrWhaterver();
JSONObject json = null;
try {
    json = new JSONObject(bundle.getString("json"));
    String key = json.getString("key");
} catch (JSONException e) {
    e.printStackTrace();
}
 */
public class ObjectCommunicationActivity extends Activity {

    private class Person {
        private String name;
        private String phone;

        Person(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }

    private TextView sendLbl = null;
    private TextView responseLbl = null;
    private EditText name = null;
    private EditText phone = null;
    private RadioButton jsonBtn = null;
    private RadioButton xmlBtn = null;
    private RadioGroup radioGroup = null;
    private EditText response = null;
    private Button sendBtn = null;
    private Button back = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        this.sendLbl = findViewById(R.id.envoi);
        this.responseLbl = findViewById(R.id.reception);
        this.name = findViewById(R.id.name);
        this.phone = findViewById(R.id.phone);
        this.jsonBtn = findViewById(R.id.jsonBtn);
        this.xmlBtn = findViewById(R.id.xmlBtn);
        this.radioGroup = findViewById(R.id.radioGroup);
        this.response = findViewById(R.id.received);
        this.sendBtn = findViewById(R.id.env);
        this.back = findViewById(R.id.retour);

        sendBtn.setOnClickListener((v) -> {
            SymComManager scm = new SymComManager();
            scm.setCommunicationEventListener(
                    response -> {
                        if(response != null){
                            this.response.setText(response);
                            return true;
                        }
                        return false;
                    });
            try {
                switch(radioGroup.getCheckedRadioButtonId()) {
                    case R.id.jsonBtn:
                        Person person = new Person(this.name.getText().toString(), this.phone.getText().toString());
                        Gson gson = new Gson();
                        scm.sendRequest(gson.toJson(person), "http://sym.iict.ch/rest/txt", "application/json");
                        break;
                    case R.id.xmlBtn:
                        String xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE directory SYSTEM \"http://sym.iict.ch/directory.dtd\"><directory/>";
                        scm.sendRequest(xmlRequest, "http://sym.iict.ch/rest/xml", "application/xml");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
            this.name.setText("");
            this.phone.setText("");
        });

        back.setOnClickListener((v) -> finish());
    }

    public class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String ... strings) {
            String response = null;
            String data = strings[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[1]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                // is output buffer writter
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", strings[2]);
                connection.setRequestProperty("Accept", strings[2]);
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(data);
                // data
                writer.close();
                InputStream inputStream = connection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                response = buffer.toString();
                return response;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            cel.handleServerResponse(result);
        }

        /**
         * Permet d'envoyer un document request vers le serveur désigné par url
         * @param request Le texte mis
         * @param url L'URL du serveur à joindre
         */
        void sendRequest(String request, String url, String contentType) {
            this.execute(request, url, contentType);
        }

        void setCommunicationEventListener (CommunicationEventListener l){
            this.cel = l;
        }
    }
}
