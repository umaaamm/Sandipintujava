package com.example.fujimiya.sandy;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    //inisialisasi variable
    String clientId;
    MqttAndroidClient client;
    TextView status_pintu;
    String pintu = "tutup";
    String Lpintu = "tutup";
    ImageView buka_pintu,tutup_pintu;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buka_pintu = (ImageView) findViewById(R.id.buka_pintu); //inisialisasi button bukapintu
        status_pintu =(TextView) findViewById(R.id.txt_pagar_stt); //inisialisasi status pintu

        //refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        cek();
//                        cek();
//                        cek();
                        finish();
                        startActivity(getIntent());
                        swipeRefreshLayout.setRefreshing(false);

                    }
                }, 2000);
            }
        });



        //pemanggilan function
        sambung();


        //untuk perintah button
        buka_pintu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payload = "the payload";
                try {
                    if (pintu.equals("buka")) {
                        payload = "tutup";
                        buka_pintu.setImageResource(R.drawable.bukabaru); //merubah gambar button
                    }
                    if (pintu.equals("tutup")) {
                        buka_pintu.setImageResource(R.drawable.bukatutup); //merubah gambar button
                        payload = "buka";
                    }

                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Error : " + e.toString(), Toast.LENGTH_LONG).show(); //pemberitahuan error
                }
                String topic = "pintu";
                byte[] encodedPayload = new byte[0];
                try {
                    //untuk publis ke mqtt
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //function untuk mengambil data dari server
    public  void sambung(){
        //"tcp://192.168.43.39:1883"
        clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://test.mosquitto.org:1883",
                        clientId);
        try {
            //ke koneksi ke server atau tidak
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"Terhubung",Toast.LENGTH_SHORT).show();
                    cek();
                    cek();
                    cek();
                    String topic = "test";
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    try {
                        client.subscribe("status_pintu",qos);
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                            }
                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                //Toast.makeText(MainActivity.this,"topic : "+topic+" message : "+message.toString(),Toast.LENGTH_SHORT).show();
                                //setMessageNotification(topic,message.toString());

                                if(topic.equals("status_pintu")){
                                    pintu = ""+message;
                                    if(pintu.equals("buka")){
                                        buka_pintu.setImageResource(R.drawable.bukabaru); //mengganti gambar button
                                        status_pintu.setText("Pagar Terbuka"); //mengganti text
                                    }
                                    if(pintu.equals("tutup")){
                                        buka_pintu.setImageResource(R.drawable.bukatutup); //mengganti gambar button
                                        status_pintu.setText("Pagar Tertutup"); //mengganti text
                                    }
                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    //Log.d(TAG, "onFailure");
                    Toast.makeText(MainActivity.this,"Tidak Terhubung",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }
    public void cek(){
        String topic = "cek";
        String payload = "Periksa Kondisi";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }


}
