/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    // create client directory

    private Socket socket = null;
    private DataInputStream inputRequest = null;
    private DataOutputStream out = null;
    private DataInputStream input = null;
    private String request = "";
    private String response = "";

    public Client(String address, int port) throws IOException {

        try {

            socket = new Socket(address, port);

            System.out.println("Connected");

            input = new DataInputStream(socket.getInputStream());
            inputRequest = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
//            creating directory client . 
            new File("src/client").mkdirs();

        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println("Target host server is down !");
        }

        try {
            request = inputRequest.readLine();
            out.writeUTF(request);
        } catch (IOException i) {
            System.out.println(i);
        }

//        reading response from web server
        try {

            response = input.readUTF();
            System.out.println(response);
        } catch (IOException i) {
            System.out.println(i);
        }

//        closing socket , input and output streams
        try {
            inputRequest.close();
            input.close();
            out.close();
            socket.close();

        } catch (IOException i) {
            System.out.println(i);
        }

    }

    public static void main(String[] args) throws IOException {

        Client clientSocket = new Client("127.0.0.1", 4000);

    }

}
