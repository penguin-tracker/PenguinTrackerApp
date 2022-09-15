package com.penguintracker.app;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.SEND_SMS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private int Port = 50000;
    private String Hosts[] =  {
            "diselectronico2022.duckdns.org",
            "3.217.109.115",
            "44.210.187.188",
            "34.228.62.220",

    };

    public String date;
    public String time;
    public String latitude;
    public String longitude;

    UDP udpMsg;
    UDPSender udpSender = new UDPSender();

    LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UDPSender udpSender = new UDPSender();
        Thread hiloUDP = new Thread(udpSender, "The Thread");
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        requestPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread hiloUDP1 = new Thread(udpSender, "The Thread");
        udpSender.requestStart();
        hiloUDP1.start();
        Toast.makeText(this, "Iniciado el envío de paquetes", Toast.LENGTH_SHORT).show();
        GetLatLon();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class UDPSender implements Runnable {
        private boolean stopRequested = false;


        public synchronized void requestStop() {
            this.stopRequested = true;
        }

        public synchronized void requestStart() {
            this.stopRequested = false;
        }

        public synchronized boolean isStopRequested() {
            return this.stopRequested;
        }

        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.d("myTag","enviando paquetes UDP");
            while (!isStopRequested()) {
                sleep((3000));
                String msg = latitude + "\n" + longitude + "\n" + time + "\n" + date;

                for (int i = 0; i < 4; i++) {
                    udpMsg = new UDP(Hosts[i], Port);
                    try {
                        udpMsg.execute(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                }
                Log.d("myTag", "Deteniendo envío");
            }
        }
    }


    public void GetLatLon() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                Date dateTime = new Date(location.getTime());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                date = ""+dateFormat.format(dateTime).toString();
                time = ""+timeFormat.format(dateTime).toString();
                latitude = "" + location.getLatitude();
                longitude = "" + location.getLongitude();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };
        // Register the listener with the location manager to receive location updates
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{SEND_SMS, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, INTERNET, READ_EXTERNAL_STORAGE}, 100);
        }
    }
}

