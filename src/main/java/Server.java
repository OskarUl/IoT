
//import com.pi4j.io.gpio.GpioController;
//import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinDigitalOutput;
//import com.pi4j.io.gpio.PinState;
//import com.pi4j.io.gpio.RaspiPin;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Server {

    private String in;
    private Socket socket;
    private ServerSocket serverSocket;
    private DataOutputStream os;
    private ObjectInputStream input;
    private ObjectInputStream inputP;
    private ObjectInputStream inputA;
    private ObjectOutputStream output;
    private HashMap<String, DataOutputStream> clients;
    private int x = 1;
    //private GpioPinDigitalOutput led1;//private GpioPinDigitalOutput PC;
    // final GpioController gpio = GpioFactory.getInstance();

    public Server() {
       // led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
       // PC = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }


    public void runServer() {
        ;
        try {
            serverSocket = new ServerSocket(12345, 100);
            System.out.println("Server is running");
            clients = new HashMap<String, DataOutputStream>();
            while (true) {
                try {
                    socket = serverSocket.accept();
                    os = new DataOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());
                    switch (readMessage("")) {
                        case "Pc client":
                            clients.put("Pc client", os);
                            sendMessage("Hello!", clients.get("Pc client"));
                            inputP = input;
                            x = 0;
                            System.out.println(clients.get("Pc client"));
                            break;
                        case "Android client":
                            clients.put("Android client", os);
                            inputA = input;
                            System.out.println(clients.get("Android client"));
                            new Controller().start();
                            break;
                    }

                    System.out.println(clients);

                    System.out.println("hello client " + socket.getRemoteSocketAddress());
                }catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


    }

    private class Controller extends Thread {

        private String in;


        @Override
        public void run() {

            try {
                while (!(in = readMessage("Android client")).equals("close"))  {
                    if (!in.equals("end")) {
                        String PcIn = in;
                        switch (in) {
                            case "10":
                                System.out.println("Light Off!");
//                                break;
                            case "11":
                                System.out.println("Light On!");
                              //  led1.low();
                                break;
                        }
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    switch (PcIn) {
                                        case "20":
                                            if (x == 0) {
                                                sendMessage("20", clients.get("Pc client"));
                                                System.out.println(clients);
                                                clients.remove("Pc client");
                                                System.out.println("PC Off");
                                                x = 1;
                                            } else {
                                                System.out.println("Pc not conected");
                                            }
                                            break;
                                        case "21":
                                            System.out.println("Wait");
                                            //PC.low();
                                            Thread.sleep(400);
                                           // PC.high();
                                            System.out.println("PC On");
                                            break;
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    //PC.high();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } else {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error handling client @ " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            }  //catch (ClassNotFoundException ex) {
            //    ex.printStackTrace();
            //}
            finally {
                //closeConnection();
                System.out.println("Connection with client @ " + socket.getRemoteSocketAddress() + " closed");
            }
        }


    }

    public void sendMessage(String message, DataOutputStream a) throws IOException {
        output = new ObjectOutputStream(a);
        output.writeObject(message);
        output.flush();
    }
    public String readMessage(String client) throws IOException {
        try {
            if (client.equals("Pc client")) {
                in = (String) inputP.readObject();

            }else if (client.equals("Android client")){
                in = (String) inputA.readObject();

            }else{
                in = (String) input.readObject();

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return in;
    }
    private void closeConnection() {
        try {

            //output.close();
            //input.close();
            inputA.close();
            //socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class updater{
    private static final String ACCESS_TOKEN = "gJKkMgm0o8AAAAAAAAAAYSuQZFy2plTSmSAt02gsXVAxn2gTn-qVBaqIMvdZwy8q";

    public static void main(String [] args) throws DbxException {
        byte[] b = {65,66,67,68,69};;

        DbxRequestConfig config = new DbxRequestConfig("dropbox", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                if (metadata.getPathLower().equals("/text.jar")) {
                    client.files().delete("/text.jar");
                }
                try {
                    InputStream in = new FileInputStream("C:\\Users\\Hule-Elev\\IdeaProjects\\GIt test\\IoT\\target\\IOT_RaspberryPI.jar");
                    client.files().uploadBuilder("/text.jar").uploadAndFinish(in);
                    System.out.println("text.jar has been uploaded to dropbox");
                    //OutputStream downloadFile = new FileOutputStream("C:\\Users\\Hule-Elev\\IdeaProjects\\text.jar");
                    /*try {
                        if (metadata.getPathLower().equals("/text.jar")) {
                            client.files().downloadBuilder("/text.jar").download(downloadFile);
                            System.out.println("text.txt has been downloaded!");
                        }
                    }  finally {
                        downloadFile.close();
                    }*/
                }
                catch (DbxException e)
                {
                    JOptionPane.showMessageDialog(null, "Unable to download file to local system\n Error: " + e);
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(null, "Unable to download file to local system\n Error: " + e);
                }
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
    }
}