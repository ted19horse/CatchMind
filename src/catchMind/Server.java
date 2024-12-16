package catchMind;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class Server {
  private ServerSocket ss;
  private final ArrayList<CopyClient> clients = new ArrayList<>();

  public Server() {
    try {
      ss = new ServerSocket(5000);
      System.out.println("Server started on port 5000");
      initClientsList();

      while (!Thread.currentThread().isInterrupted()) {
        Socket s = ss.accept();

        if(getActiveClientCount() >= 8) {
          System.out.println("Connection rejected: server is full");
          CopyClient tmpCopyClient = new CopyClient(-1);
          tmpCopyClient.sendProtocol(new Protocol(tmpCopyClient.getPosition(), Protocol.CMD_SERVER_IS_FULL, "", null));
          tmpCopyClient.closeConnection();
          continue;
        }

        Optional<CopyClient> result = clients.stream().filter(CopyClient::isEmpty).findFirst();
        result.ifPresent(oldCopyClient -> {
          System.out.println("New client connected: " + s.getInetAddress().getHostAddress());
          int index = clients.indexOf(oldCopyClient);
          CopyClient newCopyClient = new CopyClient(s, this, false, index);
          clients.set(index, newCopyClient);
          if(getActiveClientCount() == 1) {
            newCopyClient.setDrawingAuthority(true);
            System.out.println("Drawing authority granted to client: " + s.getInetAddress().getHostAddress());
          }
          else getDrawersAllDot();
          System.out.println("Total clients: " + getActiveClientCount());
          newCopyClient.start();
        });
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    } finally {
      closeServer();
    }
  }

  private void initClientsList() {
    for(int i = 0; i < 8; i++) {
      clients.add(new CopyClient(i));
    }
  }

  private int getActiveClientCount() {
    return (int) clients.stream().filter(client -> !client.isEmpty()).count();
  }

  public void getDrawersAllDot() {
    Optional<CopyClient> result = clients.stream()
        .filter(CopyClient::isDrawingAuthority)
        .findFirst();
    result.ifPresent(cc -> {
      cc.sendProtocol(new Protocol(cc.getPosition(), Protocol.CMD_GET_DRAWERS_ALL_DOTS, "", null));
    });
  }

  public void sendInitDots(Protocol p) {
    Iterator<CopyClient> iterator = clients.iterator();
    while (iterator.hasNext()) {
      CopyClient cc = iterator.next();
      if(!cc.isInitsDrawing()) {
        try {
          cc.sendProtocol(p);
          cc.setInitsDrawing(true);
        } catch (Exception e) {
          System.err.println("Error sending init dots to client: " + e.getMessage());
          iterator.remove();
          cc.closeConnection();
        }
      }
    }
  }

  public void sendProtocol(Protocol p) {
    Iterator<CopyClient> iterator = clients.iterator();
    while (iterator.hasNext()) {
      CopyClient cc = iterator.next();
      if(!cc.isEmpty()) {
        try {
          cc.sendProtocol(p);
        } catch (Exception e) {
          System.err.println("Error sending protocol to client: " + e.getMessage());
          iterator.remove();
          cc.closeConnection();
        }
      }
    }
  }

  public void removeClient(CopyClient client) {
    int index = clients.indexOf(client);
    if(index >= 0) {
      clients.set(index, new CopyClient(index));
      System.out.println("Client removed. Total clients: " + getActiveClientCount());
    }
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