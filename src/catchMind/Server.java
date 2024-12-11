package catchMind;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
  private ServerSocket ss;
  private final List<CopyClient> clients = new ArrayList<>();

  public Server() {
    try {
      ss = new ServerSocket(5000);
      System.out.println("Server started on port 5000");
      while (!Thread.currentThread().isInterrupted()) {
        Socket s = ss.accept();
        System.out.println("New client connected: " + s.getInetAddress().getHostAddress());
        CopyClient cc = new CopyClient(s, this);
        cc.start();
        clients.add(cc);
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    } finally {
      closeServer();
    }
  }

  public void sendProtocol(Protocol p) {
    Iterator<CopyClient> iterator = clients.iterator();
    while (iterator.hasNext()) {
      CopyClient cc = iterator.next();
      try {
        cc.sendProtocol(p);
      } catch (Exception e) {
        System.err.println("Error sending protocol to client: " + e.getMessage());
        iterator.remove();
        cc.closeConnection();
      }
    }
  }

  public void removeClient(CopyClient client) {
    clients.remove(client);
    System.out.println("Client removed. Total clients: " + clients.size());
  }

  private void closeServer() {
    for (CopyClient cc : clients) {
      cc.closeConnection();
    }
    if (ss != null && !ss.isClosed()) {
      try {
        ss.close();
      } catch (IOException e) {
        System.err.println("Error closing server socket: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    new Server();
  }
}