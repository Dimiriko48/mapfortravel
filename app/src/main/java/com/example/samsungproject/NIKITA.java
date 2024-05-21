package com.example.samsungproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NIKITA extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nikita);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnGoToLogin = (Button) findViewById(R.id.btnGoToLogin);
        View.OnClickListener oclbtnGoToLogin = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NIKITA.this, MainActivity2.class);
                startActivity(intent);
            }
        };
        btnGoToLogin.setOnClickListener(oclbtnGoToLogin);

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        View.OnClickListener oclbtnRegister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NIKITA.this, MainActivity.class);
                startActivity(intent);
            }
        };
        btnRegister.setOnClickListener(oclbtnRegister);
    }
}