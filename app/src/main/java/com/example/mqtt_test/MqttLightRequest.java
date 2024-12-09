package com.example.mqtt_test;

public class MqttLightRequest {
    public String model; // LED 型號
    public String value; // 控制指令，例如 "ON" 或 "OFF"

    public MqttLightRequest(String model, String value) {
        this.model = model;
        this.value = value;
    }
}

