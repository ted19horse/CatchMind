package catchMind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends JFrame {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private JPanel titlePanel, contentPanel, leftSidePanel, centerPanel, rightSidePanel, drawingPanel, controlPanel, palettePanel, scoreBoardPanel, chattingPanel;
  private final ArrayList<Dot> allDots = new ArrayList<>();
  private final ArrayList<Dot> currentStroke = new ArrayList<>();

  public Client() {
    initializeNetwork();
    initializeUI();
    startListeningThread();
  }

  private void initializeNetwork() {
    try {
      socket = new Socket("192.168.10.100", 5000);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      Protocol protocol = new Protocol();
      protocol.setCmd(Protocol.CMD_CONNECT);
      out.writeObject(protocol);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "네트워크 연결 실패: " + e.getMessage());
      System.exit(1);
    }
  }

  private void initializeUI() {
    titlePanel = new JPanel();
    titlePanel.setLayout(null);
    titlePanel.setPreferredSize(new Dimension(1500, 100));
    titlePanel.setBackground(new Color(0, 0, 0, 128));

    contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.setPreferredSize(new Dimension(1500, 900));

    leftSidePanel = new JPanel();
    leftSidePanel.setLayout(null);
    leftSidePanel.setPreferredSize(new Dimension(300, 900));
    leftSidePanel.setBackground(new Color(255, 0, 0, 128));

    centerPanel = new JPanel(new BorderLayout());
    centerPanel.setPreferredSize(new Dimension(900, 900));

    rightSidePanel = new JPanel();
    rightSidePanel.setLayout(null);
    rightSidePanel.setPreferredSize(new Dimension(300, 900));
    rightSidePanel.setBackground(new Color(0, 0, 255, 128));

    drawingPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Dot dot : allDots) {
          g.setColor(dot.color);
          g.fillOval(dot.x, dot.y, dot.wh, dot.wh);
        }
        for (Dot dot : currentStroke) {
          g.setColor(dot.color);
          g.fillOval(dot.x, dot.y, dot.wh, dot.wh);
        }
      }
    };
    drawingPanel.setPreferredSize(new Dimension(900, 600));
    drawingPanel.setBackground(Color.WHITE);

    drawingPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        currentStroke.clear();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        sendDots(currentStroke);
        allDots.addAll(currentStroke);
        currentStroke.clear();
        drawingPanel.repaint();
      }
    });

    drawingPanel.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        Dot d = new Dot(e.getX() - 1, e.getY() - 1, Color.BLACK);
        currentStroke.add(d);
        drawingPanel.repaint();
      }
    });

    controlPanel = new JPanel();
    controlPanel.setLayout(new BorderLayout());
    controlPanel.setPreferredSize(new Dimension(900, 300));
    controlPanel.setBackground(new Color(0, 255, 0, 128));

    palettePanel = new JPanel();
    palettePanel.setLayout(null);
    palettePanel.setPreferredSize(new Dimension(900, 100));

    scoreBoardPanel = new JPanel();
    scoreBoardPanel.setLayout(null);
    scoreBoardPanel.setPreferredSize(new Dimension(450, 200));
    scoreBoardPanel.setBackground(new Color(128, 128, 0, 128));

    chattingPanel = new JPanel();
    chattingPanel.setLayout(null);
    chattingPanel.setPreferredSize(new Dimension(450, 200));
    chattingPanel.setBackground(new Color(0, 128, 128, 128));


    contentPanel.add(centerPanel, BorderLayout.CENTER);
    contentPanel.add(leftSidePanel, BorderLayout.WEST);
    contentPanel.add(rightSidePanel, BorderLayout.EAST);

    centerPanel.add(drawingPanel, BorderLayout.CENTER);
    centerPanel.add(controlPanel, BorderLayout.SOUTH);

    controlPanel.add(palettePanel, BorderLayout.NORTH);
    controlPanel.add(scoreBoardPanel, BorderLayout.WEST);
    controlPanel.add(chattingPanel, BorderLayout.EAST);

    Client.this.setLayout(new BorderLayout());

    Client.this.add(titlePanel, BorderLayout.NORTH);
    Client.this.add(contentPanel, BorderLayout.CENTER);

    Client.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Client.this.setSize(1500, 1000);
    Client.this.setTitle("Catch Mind");
    Client.this.setLocationRelativeTo(null);
    Client.this.setVisible(true);
  }

  private void startListeningThread() {
    new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          Object obj = in.readObject();
          if (obj instanceof Protocol protocol) {
            handleProtocol(protocol);
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      } finally {
        closeConnection();
      }
    }).start();
  }

  private void handleProtocol(Protocol protocol) {
    SwingUtilities.invokeLater(() -> {
      switch (protocol.getCmd()) {
        case Protocol.CMD_GET_DRAWERS_ALL_DOTS:
          Protocol p = new Protocol();
          p.setCmd(Protocol.CMD_DRAW);
          p.setDots(new ArrayList<>(allDots));
          try {
            out.writeObject(p);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          break;
        case Protocol.CMD_DRAW:
          ArrayList<Dot> receivedDots = protocol.getDots();
          if (receivedDots != null) {
            allDots.addAll(receivedDots);
            drawingPanel.repaint();
          }
          break;
        case Protocol.CMD_CLEAR:
          allDots.clear();
          drawingPanel.repaint();
          break;
        case Protocol.CMD_DISCONNECT:
          closeConnection();
          System.exit(0);
          break;
      }
    });
  }

  private void sendDots(ArrayList<Dot> dots) {
    try {
      Protocol p = new Protocol();
      p.setCmd(Protocol.CMD_DRAW);
      p.setDots(new ArrayList<>(dots));
      out.writeObject(p);
      out.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Client::new);
  }
}