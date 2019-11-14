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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ObjectCommunicationActivity extends Activity {

    public class Person {
        private String name;
        private String firstname;
        private String middlename;
        private String gender;
        private String phone;

        Person(String name, String firstname, String middlename, String gender, String phone) {
            this.name = name;
            this.firstname = firstname;
            this.middlename = middlename;
            this.gender = gender;
            this.phone = phone;
        }

        Person(Person p) {
            this.name = p.name;
            this.firstname = p.firstname;
            this.middlename = p.middlename;
            this.gender = p.gender;
            this.phone = p.phone;
        }

        public String toString() {
            return this.firstname + " " + this.middlename + " " + this.name + " [" + this.gender + "]\n" + this.phone;
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
                String completeName = this.name.getText().toString();
                String[] names = completeName.split(" ");

                String name = names.length >= 2 ? completeName.substring(completeName.indexOf(" ") + 1) : "";
                String firstname = names.length >= 1 ? names[0] : "";
                String middlename = "The Rock"; // Défini arbitrairement
                String gender = "m"; // Défini arbitrairement
                String phone = this.phone.getText().toString();

                switch(radioGroup.getCheckedRadioButtonId()) {
                    case R.id.jsonBtn:
                        Person person = new Person(name, firstname, middlename, gender, phone);
                        scm.sendRequest(new Gson().toJson(person),
                                "http://sym.iict.ch/rest/json", "application/json");
                        break;

                    case R.id.xmlBtn:
                        String xmlRequest = getXmlRequest(name, firstname, middlename, gender, phone);
                        scm.sendRequest(xmlRequest,
                                "http://sym.iict.ch/rest/xml", "application/xml");
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

    public String getXmlRequest(String name, String firstname, String middlename, String gender, String phone) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter sw = new StringWriter();

        try {
            serializer.setOutput(sw);
            serializer.startDocument("UTF-8", null);
            serializer.docdecl(" directory SYSTEM \"http://sym.iict.ch/directory.dtd\"");
            serializer.startTag("","directory");
            serializer.startTag("", "person");

            // Name
            serializer.startTag("", "name");
            serializer.text(name);
            serializer.endTag("", "name");

            // Firstname
            serializer.startTag("", "firstname");
            serializer.text(firstname);
            serializer.endTag("", "firstname");

            // Middlename
            serializer.startTag("", "middlename");
            serializer.text(middlename);
            serializer.endTag("", "middlename");

            // Gender
            serializer.startTag("", "gender");
            serializer.text(gender);
            serializer.endTag("", "gender");

            // Phone
            serializer.startTag("", "phone");
            serializer.attribute("", "type", "mobile");
            serializer.text(phone);
            serializer.endTag("", "phone");

            serializer.endTag("", "person");
            serializer.endTag("", "directory");
            serializer.endDocument();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    public Person parseXml(String xml) {
        String name = "";
        String firstname = "";
        String middlename = "";
        String gender = "";
        String phone = "";
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
                    } else if (tag.equals("firstname")) {
                        if (xpp.next() == XmlPullParser.TEXT) {
                            firstname = xpp.getText();
                        }
                    } else if (tag.equals("middlename")) {
                        if (xpp.next() == XmlPullParser.TEXT) {
                            middlename = xpp.getText();
                        }
                    } else if (tag.equals("gender")) {
                        if (xpp.next() == XmlPullParser.TEXT) {
                            gender = xpp.getText();
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

        return new Person(name, firstname, middlename, gender, phone);
    }

    public class SymComManager extends AsyncTask<String, Void, String> {

        private CommunicationEventListener cel = null;

        @Override
        protected String doInBackground(String ... strings) {
            URL obj;
            try {
                System.out.println(strings[0] + "\n" + strings[1] + "\n" + strings[2]);
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
                connection.disconnect();
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
         * @param contentType le type de contenu à savoir json ou xml
         */
        void sendRequest(String request, String url, String contentType) {
            this.execute(request, url, contentType);
        }

        void setCommunicationEventListener (CommunicationEventListener l){
            this.cel = l;
        }
    }
}
