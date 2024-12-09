package com.example.mqtt_test;

import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHandler {
    private String brokerUrl = "wss://broker.hivemq.com:8884"; // 公網設置
    private String clientId = MqttClient.generateClientId(); // 自動生成唯一 clientId
    private String username = "nutc_imac_e22_ithome"; // user名稱
    private String password = "00000000"; // mqtt密碼
    private String topic = "";
    public MqttClient client;
    private MqttListener mqttListener;
    private ConnectionCallback connectionCallback; // 連線成功後的回調

    // 開始連線，並接收要連線的主題
    public void connect(String topic) {
        Log.d("TAG", "connect: ");
        this.topic = topic;
        new Thread(() -> {
            try {
                Log.d("TAG", "connect: 有");
                MemoryPersistence persistence = new MemoryPersistence();
                client = new MqttClient(brokerUrl, clientId, persistence);

                MqttConnectOptions connectOptions = new MqttConnectOptions();
                connectOptions.setCleanSession(true);
                connectOptions.setUserName(username);
                connectOptions.setPassword(password.toCharArray());

                // 設置連線成功的callback
                client.setCallback(mqttCallback);
                client.connect(connectOptions);
                Log.d("TAG", "Connecting to broker...");
            } catch (MqttException e) {
                Log.d("TAG", "connect: "+e);
                e.printStackTrace();
            }
        }).start();
    }

    private MqttCallbackExtended mqttCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.e("MqttHandler", "connectComplete: Connected to broker");
            // 訂閱主題
            subscribe(topic);

            // 觸發連線成功回調
            if (connectionCallback != null) {
                connectionCallback.onConnected();
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            // 連線丟失
            Log.e("MqttHandler", "connectionLost: Connection lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // 處理返回的訊息，如溫濕度
            Log.e("MqttHandler", "connectionLost: messageArrived");
            // 顯示收到的訊息
            String payload = new String(message.getPayload());
            Log.d("MQTT", "Message received: " + payload);
            if (mqttListener != null) {
                mqttListener.messageArrived(message);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.e("MqttHandler", "deliveryComplete: Message delivered");
        }
    };

    //關閉連線
    public void disconnect() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 發送訊息到MQTT Broker
    public void publishLed(String topic, MqttLightRequest request) {
        if (client == null || !client.isConnected()) {
            Log.e("MqttHandler", "Client not connected, cannot publish message");
            return;
        }

        Log.e("MqttHandler", "publishLed: Publishing " + request.model + " " + request.value);
        Gson gson = new Gson();
        String json = gson.toJson(request);

        try {
            MqttMessage mqttMessage = new MqttMessage(json.getBytes());
            // 發送MQTT訊息
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            // 訂閱主題
            client.subscribe(topic, 0);
            Log.e("MqttHandler", "subscribe: Subscribed to topic: " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unSubscribe(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }
    public void setMqttListener(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    public  interface MqttListener {
        void messageArrived(MqttMessage message) throws Exception;
    }
    public interface ConnectionCallback {
        void onConnected();
    }
}

