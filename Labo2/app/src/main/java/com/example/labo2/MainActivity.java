package com.example.labo2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.labo2.ui.AsynchronCommunicationActivity;
import com.example.labo2.ui.CompressCommunicationActivity;
import com.example.labo2.ui.DifferCommunicationActivity;
import com.example.labo2.ui.GraphQLCommunicationActivity;
import com.example.labo2.ui.ObjectCommunicationActivity;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Classe du menu
 */
public class MainActivity extends AppCompatActivity {

    private Button async = null;
    private Button dif = null;
    private Button obj = null;
    private Button compr = null;
    private Button graph = null;

    private Button exit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.async = findViewById(R.id.async);
        this.dif = findViewById(R.id.dif);
        this.obj = findViewById(R.id.obj);
        this.compr = findViewById(R.id.compr);
        this.graph = findViewById(R.id.graph);

        this.exit = findViewById(R.id.exit);

        async.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, AsynchronCommunicationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        dif.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, DifferCommunicationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        obj.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, ObjectCommunicationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        compr.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, CompressCommunicationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        graph.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, GraphQLCommunicationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        exit.setOnClickListener((v) -> {
            finish();
        });
    }
}