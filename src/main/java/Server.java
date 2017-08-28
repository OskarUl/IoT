
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Server {

    private ServerSocket serverSocket;
    private DataOutputStream os;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ObjectOutputStream outputA;
    private HashMap<String, DataOutputStream> clients;
    private int x = 1;
    private GpioPinDigitalOutput led1;
    private GpioPinDigitalOutput PC;
    final GpioController gpio = GpioFactory.getInstance();

    public Server() {
        led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
        PC = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }


    public void runServer() {
        String in;
        Socket socket;
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345, 100);
            System.out.println("Server is running");
            clients = new HashMap<String, DataOutputStream>();
            while (true) {
                try {
                    socket = serverSocket.accept();
                    os = new DataOutputStream(socket.getOutputStream());
                    input = new ObjectInputStream(socket.getInputStream());
                    in = (String) input.readObject();
                    System.out.println(in);
                    switch (in) {
                        case "Pc client":
                            clients.put("Pc client", os);
                            x = 0;
                            System.out.println(clients.get("Pc client"));
                            //new Controller(socket).start();
                            break;
                        case "Android client":
                            clients.put("Android client", os);
                            System.out.println(clients.get("Android client"));
                            new Controller(socket).start();
                            break;
                    }

                    System.out.println(clients);

                    System.out.println("hello client " + socket.getRemoteSocketAddress());
                }catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


    }

    private class Controller extends Thread {

        private Socket socket;
        private int num;
        private String in;

        public Controller(Socket socket) {
            this.socket = socket;
            this.num = num;
        }

        @Override
        public void run() {

            try {
                getStreams();
                output.writeObject("Hello, Welocme to Raspberry PI");
                output.flush();
                while (!(in = (String) input.readObject()).equals("close")) {
                    if (!in.equals("end")) {
                        String PcIn = in;
                        switch (in) {
                            case "10":
                                System.out.println("Light Off!");
                                led1.high();
                                break;
                            case "11":
                                System.out.println("Light On!");
                                led1.low();
                                break;

                        }

                        new Thread(new Runnable() {
                            public void run() {
                                try {

                                    switch (PcIn) {
                                        case "20":
                                            if (x == 0) {
                                                outputA = new ObjectOutputStream(clients.get("Pc client"));
                                                outputA.flush();
                                                outputA.writeObject("20");
                                                clients.remove("Pc client");
                                                System.out.println(clients);
                                                //outputA.close();

                                                System.out.println("PC Off");
                                                x = 1;
                                            } else {
                                                System.out.println("Pc not conected");
                                            }
                                            break;
                                        case "21":
                                            System.out.println("Wait");
                                            PC.low();
                                            Thread.sleep(400);
                                            PC.high();
                                            System.out.println("PC On");
                                            break;
                                    }
                                    try {
                                        output.writeObject("command" + in.toUpperCase());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    PC.high();
                                } catch (IOException e) {
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
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }  finally {
                closeConnection();
                System.out.println("Connection with client @ " + socket.getRemoteSocketAddress() + " closed");
            }
        }

        public void getStreams() throws IOException {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();


        }

        private void closeConnection() {
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
