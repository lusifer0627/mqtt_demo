package com.example.mqtt_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private MqttHandler mqttHandler = new MqttHandler();

    private Button btn_light_connect,btn_light_on,btn_light_off,btn_temperature_connect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_light_connect = findViewById(R.id.btn_light_connect);
        btn_temperature_connect = findViewById(R.id.btn_temperature_connect);
        btn_light_on = findViewById(R.id.btn_light_on);
        btn_light_off = findViewById(R.id.btn_light_off);


        btn_light_connect.setOnClickListener(v -> {
            // 開始連線到 MQTT Broker
            mqttHandler.connect("home/light");
        });
        btn_temperature_connect.setOnClickListener(v -> {
            // 開始連線到 MQTT Broker
            mqttHandler.connect("home/temperature");
        });
        // 設置連線完成的回調邏輯
        mqttHandler.setConnectionCallback(() -> {
            Log.d("TAG", "onCreate: setConnectionCallback 成功");
            btn_light_on.setOnClickListener(v -> {
                // 連線成功後執行開燈的訊息發佈
                MqttLightRequest request = new MqttLightRequest("LED123", "ON");
                mqttHandler.publishLed("home/light", request);
            });
            btn_light_off.setOnClickListener(v -> {
                // 連線成功後執行關燈的訊息發佈
                MqttLightRequest request = new MqttLightRequest("LED123", "OFF");
                mqttHandler.publishLed("home/light", request);
            });
        });

    }
}