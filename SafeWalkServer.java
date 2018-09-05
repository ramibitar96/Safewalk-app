   /**	
   * Project 5
   * @author Rami Bitar, rbitar, 806
   */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
public class SafeWalkServer implements Runnable {
    ServerSocket serverSocket;
    ArrayList<Socket> sockets = new ArrayList<Socket>();
    ArrayList<String[]> messages = new ArrayList<String[]>();
    
    public SafeWalkServer(int port) throws SocketException, IOException {
        serverSocket = new ServerSocket(port);
    }
    
    public SafeWalkServer() throws SocketException, IOException {
        serverSocket = new ServerSocket(0);
        System.out.println("This server is running on port: "  + serverSocket.getLocalPort());
    }
    
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
    
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.printf("connection received from %s%n", socket); 
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                String message = in.readLine();
                
                boolean matched = false;
                String[] splitMessage = message.split(",");
                if (isValid(splitMessage) && splitMessage.length == 4) {
                    for(int i = 0; i < messages.size(); i++) {
                        if(splitMessage[1].equals(messages.get(i)[1])) {
                            if (((splitMessage[2].equals("*") && !messages.get(i)[2].equals("*")) || (!splitMessage[2].equals("*") && messages.get(i)[2].equals("*"))) || splitMessage[2].equals(messages.get(i)[2])) {
                                if (splitMessage[2].equals("*") && messages.get(i)[2].equals("*")) {
                                } else {
                                matched = true;
                                pw.println("RESPONSE: " + messages.get(i)[0] + "," + messages.get(i)[1]+ "," + messages.get(i)[2]+ "," + messages.get(i)[3]);
                                PrintWriter pw2 = new PrintWriter(sockets.get(i).getOutputStream(), true);
                                pw2.println("RESPONSE: " + message);
                                messages.remove(i);
                                pw2.close();
                                sockets.get(i).close();
                                sockets.remove(i);
                                socket.close();
                                }
                            }
                        }
                    }
                    if  (!matched) {
                        sockets.add(socket);
                        messages.add(splitMessage);
                    }
                }
                if (splitMessage[0].equals(":SHUTDOWN") && isValid(splitMessage)) {
                    for(int i = 0; i < sockets.size(); i++) {
                        PrintWriter pw2 = new PrintWriter(sockets.get(i).getOutputStream(), true);
                        pw2.println("ERROR: connection reset");
                        sockets.get(i).close();
                        sockets.remove(i);
                        messages.remove(i);
                        pw2.close();
                    }     
                    pw.println("RESPONSE: success");
                    pw.close();
                    in.close();
                    socket.close();
                    serverSocket.close();
                    return;
                }
                if (splitMessage[0].equals(":RESET") && isValid(splitMessage)) {
                    for(int i = 0; i < sockets.size(); i++) {
                        PrintWriter pw2 = new PrintWriter(sockets.get(i).getOutputStream(), true);
                        pw2.println("ERROR: connection reset");
                        sockets.get(i).close();
                        sockets.remove(i);
                        messages.remove(i);
                        pw2.close();
                    }
                    pw.println("RESPONSE: success");
                    socket.close();
                }
                if (splitMessage[0].equals(":LIST_PENDING_REQUESTS") && isValid(splitMessage)) {
                    String output= "[";
                    for (int i = 0; i < messages.size(); i++)  {
                        if (i != (messages.size() - 1))
                            output += "[" + messages.get(i)[0] + ", " +  messages.get(i)[1] + ", " +  messages.get(i)[2] + ", " +  messages.get(i)[3] + "], ";
                        else
                            output += "[" + messages.get(i)[0] + ", " +  messages.get(i)[1] + ", " +  messages.get(i)[2] + ", " +  messages.get(i)[3] + "]";
                    }
                     output += "]";
                     pw.println(output);
                } 
                
                if(!isValid(splitMessage)) {
                    pw.println("ERROR: invalid request");
                    socket.close();
                }
                
            } catch (IOException e) {
                System.out.println("Failure");
            }
        }
    }
    
    public static boolean isValid(String[] test) {
        if (test.length == 4) {
            if (test[1].equals("PMU") || test[1].equals("CL50") || test[1].equals("LWSN") || test[1].equals("EE") || test[1].equals("PUSH")) {
                if (test[2].equals("PMU") || test[2].equals("CL50") || test[2].equals("LWSN") || test[2].equals("EE") || test[2].equals("PUSH") || test[2].equals("*")) {
                    if (!test[1].equals(test[2])) {
                        if (test[3].equals("0") || test[3].equals("1") || test[3].equals("2"))
                            return true;
                    }
                }
            }
        }
        if (test.length == 1) {
            if (test[0].equals(":SHUTDOWN") || test[0].equals(":RESET")  || test[0].equals(":LIST_PENDING_REQUESTS"))
                return true;
        }
        return false;
    }
    
    public static void main(String[] args) throws IOException {
        SafeWalkServer server = null;
        int port = 0;
        if (args.length != 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
            }
        }
        else {
            server = new SafeWalkServer();
            server.run();
        }
        if (port < 65535 && port > 1025) {
            server = new SafeWalkServer(port);
            server.run();
        }
        else
            System.out.println("Invalid port");
    }
}
