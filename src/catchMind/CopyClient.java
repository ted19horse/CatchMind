package catchMind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CopyClient extends Thread {
  private boolean isEmpty = true;
  private int position;
  private Socket socket;
  ObjectOutputStream out;
  private ObjectInputStream in;
  private Server server;
  private boolean initsDrawing;
  private boolean drawingAuthority;

  public CopyClient(Socket socket, Server server, boolean drawingAuthority, int position) {
    isEmpty = false;
    this.position = position;
    this.socket = socket;
    this.server = server;
    this.drawingAuthority = drawingAuthority;
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
      if(!drawingAuthority) server.getDrawersAllDot();
      else {
        setInitsDrawing(true);
        out.writeObject( new Protocol(position, Protocol.CMD_CAN_DRAWING, "", null));
      }
    } catch (IOException e) {
      System.err.println("Error creating streams: " + e.getMessage());
      closeConnection();
    }
  }

  public CopyClient(int position) {
    this.position = position;
    this.drawingAuthority = false;
    this.initsDrawing = false;
    setEmpty(true);
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        Object obj = in.readObject();
        if (obj instanceof Protocol p) {
          handleProtocol(p);
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Error in client thread: " + e.getMessage());
    } finally {
      closeConnection();
    }
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public void setEmpty(boolean empty) {
    isEmpty = empty;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  private void handleProtocol(Protocol p) {
    switch (p.getCmd()) {
      case Protocol.CMD_CONNECT:
        try {
          out.writeObject(p);
          if(isDrawingAuthority()) out.writeObject( new Protocol(position, Protocol.CMD_CAN_DRAWING, "", null) );
          out.flush();
        } catch (IOException e) {
          closeConnection();
          throw new RuntimeException(e);
        }
        System.out.println("New client connected");
        break;
      case Protocol.CMD_GET_DRAWERS_ALL_DOTS:
        if(!isInitsDrawing()) {
          System.out.println("Received drawers all dots");
          server.sendInitDots(p);
        }
        break;
      case Protocol.CMD_DRAW:
        if(!isInitsDrawing()) setInitsDrawing(true);
        System.out.println("Received drawing data");
        if(isDrawingAuthority()) server.sendProtocol(p);
        break;
      case Protocol.CMD_CLEAR:
        System.out.println("Received clear command");
        server.sendProtocol(p);
        break;
      case Protocol.CMD_MSG_SEND:
        System.out.println("Chatting send");
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

  public void setInitsDrawing(boolean initsDrawing) {
    this.initsDrawing = initsDrawing;
  }

  public boolean isInitsDrawing() {
    return initsDrawing;
  }

  public void setDrawingAuthority(boolean drawingAuthority) {
    this.drawingAuthority = drawingAuthority;
  }

  public boolean isDrawingAuthority() {
    return drawingAuthority;
  }
}