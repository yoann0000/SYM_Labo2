package com.example.labo2.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Xml;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.labo2.R;
import com.example.labo2.ui.eventListener.CommunicationEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ObjectCommunicationActivity extends Activity {

    private class Person {
        private String name;
        private String phone;

        Person(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        Person(Person p) {
            this.name = p.name;
            this.phone = p.phone;
        }

        public String toString() {
            return this.name + " " + this.phone;
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
            long start =  System.currentTimeMillis();
            SymComManager scm = new SymComManager();
            scm.setCommunicationEventListener(
                    response -> {
                        if(response != null) {
                            this.name.setText("");
                            this.phone.setText("");
                            Person person;

                            switch(radioGroup.getCheckedRadioButtonId()) {
                                case R.id.jsonBtn:
                                    Type type = new TypeToken<Person>(){}.getType();
                                    person = new Gson().fromJson(response, type);
                                    this.response.setText(person.toString());
                                    break;

                                case R.id.xmlBtn:
                                    person = new Person(parseXml(response));
                                    this.response.setText(person.toString());
                                    break;
                            }

                            long end =  System.currentTimeMillis();
                            System.out.println("Temps : " + (end - start));
                            return true;
                        }
                        return false;
                    });
            try {
                switch(radioGroup.getCheckedRadioButtonId()) {
                    case R.id.jsonBtn:
                        Person person = new Person(this.name.getText().toString(), this.phone.getText().toString());
                        scm.sendRequest(new Gson().toJson(person), "http://sym.iict.ch/rest/txt", "application/json");
                        break;

                    case R.id.xmlBtn:
                        String xmlRequest = getXmlRequest(this.name.getText().toString(), this.phone.getText().toString());
                        scm.sendRequest(xmlRequest, "http://sym.iict.ch/rest/xml", "application/xml");
                        break;

                    default:
                        this.response.setText("Please check JSON or XML button.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                e.printStackTrace();
            }
        });

        back.setOnClickListener((v) -> finish());
    }

    public String getXmlRequest(String name, String phone) {
        XmlSerializer xmlData = Xml.newSerializer();
        StringWriter xml = new StringWriter();
        try {
            xmlData.setOutput(xml);
            xmlData.startDocument("UTF-8", null);
            xmlData.docdecl(" directory SYSTEM \"http://sym.iict.ch/directory.dtd\"");
            xmlData.startTag("","directory");
            xmlData.startTag("", "person");

            // Name
            xmlData.startTag("", "name");
            xmlData.text(name);
            xmlData.endTag("", "name");

            // Phone
            xmlData.startTag("", "phone");
            xmlData.text(phone);
            xmlData.endTag("", "phone");

            xmlData.endTag("", "person");
            xmlData.endTag("", "directory");
            xmlData.endDocument();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return xml.toString();
    }

    public Person parseXml(String xml) {
        String name = null;
        String phone = null;
        XmlPullParserFactory xppf;
        try {
            xppf = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = xppf.newPullParser();
            xpp.setInput(new StringReader(xml));

            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT)  {
                String tag = xpp.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (tag.equals("name")) {
                        if (xpp.next() == XmlPullParser.TEXT) {
                            name = xpp.getText();
                        }
                    } else if (tag.equals("phone")) {
                        if (xpp.next() == XmlPullParser.TEXT) {
                            phone = xpp.getText();
                        }
                    }
                }
                event = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Person(name, phone);
    }

    public class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String ... strings) {
            URL obj;
            try {
                System.out.println(strings[0] + " " + strings[1] + " " + strings[2]);

                obj = new URL(strings[1]);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Request", strings[0]);
                connection.setRequestProperty("Content-Type", strings[2]);
                connection.setRequestProperty("Accept", strings[2]);
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
                System.out.println(response.toString());
                return response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
