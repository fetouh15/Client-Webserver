/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaWebServer implements Runnable {

    private static final int port = 4000;
    private static final String indexFile = "INDEX.HTML";
    private static final String fileNotFound = "404.HTML";
    private Socket client;
    private String methodType, src, dest;

    public JavaWebServer(Socket skt) {
        client = skt;
    }

    private static boolean copyFileUsingStream(File source, File dest ) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            
            byte[] buffer = new byte[1024];
            int length;
            
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            is.close();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    //validate header
    public String isValidHeader(String header) {
        //  header = header.toUpperCase();
        String pattern = "(GET|POST)\\s+(.+)\\s+(.+)\\s*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(header);
        
        if (m.find()) {
            methodType = m.group(1);
            return m.group(3);

        } else {
            return null;

        }

    }

    public static void main(String[] args) throws IOException {

        try {
            ServerSocket server = new ServerSocket(port);
            server.setSoTimeout(100000);
            System.out.println("Web Server has started successfully");

            while (true) {

                JavaWebServer socket = new JavaWebServer(server.accept());
                Thread clientRequest = new Thread(socket);
                clientRequest.start();
            }

        } catch (IOException e) {
            System.out.println("Server connection error : " + e);
        }

    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void run() {

        System.out.println("Connection established with " + client.getRemoteSocketAddress());
        DataInputStream in;
        DataOutputStream out;
        BufferedOutputStream dataOut;

        try {

            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            dataOut = new BufferedOutputStream(client.getOutputStream());

            String header = in.readUTF();

//            check if valid request or a bad request
            String filename = isValidHeader(header);

//            System.out.println(header);
//            System.out.println(filename);
            if (filename != null) {

                // Get or Post 
                // then send or recieve 
                if (methodType.equals("GET")) {
                    src = "src/server/" + filename;
                    dest = "src/client/" + filename;
                } else {
                    dest = "src/server/" + filename;
                    src = "src/client/" + filename;
                }

                Path p = Paths.get(dest);
                Path p2 = Paths.get(src);

                if(copyFileUsingStream(p2.toFile(), p.toFile()))
                {
                    System.out.println("File transfer successful for socket :"+client.getPort());
                    out.writeUTF("HTTP/1.0 200 OK");
                    out.flush();
                }
                else
                {
                    System.out.println("File transfer unsuccessful for socket :"+client.getPort());
                    out.writeUTF("403 File not found");
                    out.flush();
                }

                
            } else {
                out.writeUTF("404 Bad Request");
                out.flush();

            }
            
            try {
                client.close();
                System.out.println("connection is closed for socket "+client.getPort());
                System.out.println("");
            } catch (IOException ex1) {
                Logger.getLogger(JavaWebServer.class.getName()).log(Level.SEVERE, null, ex1);
            }
            

        } catch (IOException ex) {
            Logger.getLogger(JavaWebServer.class.getName()).log(Level.SEVERE, null, ex);
            try {
                client.close();
            } catch (IOException ex1) {
                Logger.getLogger(JavaWebServer.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }

    }

}
