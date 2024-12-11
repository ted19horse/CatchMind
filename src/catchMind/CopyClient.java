package catchMind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CopyClient extends Thread {
  private final Socket socket;
  ObjectOutputStream out;
  private ObjectInputStream in;
  private final Server server;

  public CopyClient(Socket socket, Server server) {
    this.socket = socket;
    this.server = server;
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      System.err.println("Error creating streams: " + e.getMessage());
      closeConnection();
    }
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        Object obj = in.readObject();
        if (obj instanceof Protocol) {
          Protocol p = (Protocol) obj;
          handleProtocol(p);
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Error in client thread: " + e.getMessage());
    } finally {
      closeConnection();
    }
  }

  private void handleProtocol(Protocol p) {
    switch (p.getCmd()) {
      case Protocol.CMD_CONNECT:
        System.out.println("New client connected");
        break;
      case Protocol.CMD_DRAW:
        System.out.println("Received drawing data");
        server.sendProtocol(p);
        break;
      case Protocol.CMD_CLEAR:
        System.out.println("Received clear command");
        server.sendProtocol(p);
        break;
      case Protocol.CMD_DISCONNECT:
        System.out.println("Client disconnected");
        closeConnection();
        break;
    }
  }

  public void sendProtocol(Protocol p) {
    try {
      out.writeObject(p);
      out.flush();
    } catch (IOException e) {
      System.err.println("Error sending protocol: " + e.getMessage());
      closeConnection();
    }
  }

  public void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (IOException e) {
      System.err.println("Error closing connection: " + e.getMessage());
    } finally {
      server.removeClient(this);
    }
  }
}