package com.koiti.aforo;

import android.content.Context;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Context context;
    private boolean isConnected = false;
    private SPData spData = new SPData();

    Client(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            String host = spData.getValueString("socketIp", context);
            int port = spData.getValueInt("socketPort", context);
            Socket socket = new Socket(host, port);
            isConnected = true;
            spData.save("Conectado", "connected", context);

            while (true) {
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                output.write(spData.getValueString("available", context) + "\n");
                output.flush();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            isConnected = false;
            spData.save("No conectado", "connected", context);
        }
    }

    public boolean getConnected() {
        return isConnected;
    }
}
