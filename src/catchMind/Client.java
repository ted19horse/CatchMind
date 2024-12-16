package catchMind;

import com.sun.source.tree.NewArrayTree;

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
  String msg;
  JTextField chattingField;
  JButton chattingSend;
  JTextArea chattingArea;

  public Client() {
    initializeNetwork();
    initializeUI();
    startListeningThread();
  }

  private void initializeNetwork() {
    try {
      socket = new Socket("127.0.0.1", 5000);
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

    //bottomPanel
    JPanel bottomPanel = new JPanel(new BorderLayout());


    chattingField = new JTextField(); // 채팅 입력 필드
    chattingField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    bottomPanel.add(chattingField, BorderLayout.CENTER);


    chattingSend = new JButton("보내기");
    bottomPanel.add(chattingSend, BorderLayout.EAST);


    chattingPanel = new JPanel(new BorderLayout());
    chattingPanel.setPreferredSize(new Dimension(450, 200));
    chattingPanel.setBackground(new Color(0, 128, 128, 128));


    chattingArea = new JTextArea(); // 채팅 메시지를 보여주는 영역
    chattingArea.setEditable(false);
    chattingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    chattingPanel.add(new JScrollPane(chattingArea), BorderLayout.CENTER); // 스크롤 가능하게 설정

    chattingPanel.add(bottomPanel, BorderLayout.SOUTH);

    chattingField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int press = e.getKeyCode();
        if(press == KeyEvent.VK_ENTER) {
          System.out.println("KeyPress Check");
          msg = chattingField.getText();
          System.out.println("msg check : " + msg);
          if(!msg.isEmpty()){
            Protocol p = new Protocol();
            p.setCmd(4);
            p.setMsg(p.getMsg());
            chattingField.setText("");
            try{
              out.writeObject(p);
            } catch (IOException ex) {
              System.out.println("KeyListener Error: " + ex.getMessage());
            }
            System.out.println(p.toString());
          }
        }
      }
    });

    chattingSend.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("chk");
        msg = chattingField.getText();
        System.out.println(msg);
        if(!msg.isEmpty()) {
//          Protocol p = new Protocol();
//          p.setCmd(Protocol.CMD_MSG_SEND);
//          p.setMsg(p.getMsg());
          chattingField.setText("");
          try {
            out.writeObject(new Protocol(Protocol.CMD_MSG_SEND, msg, null));
          } catch (IOException ex) {
            System.out.println("ActionListener Error: " + ex.getMessage());
          }
          System.out.println(p);
        }
      }

    });

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
        case Protocol.CMD_MSG_SEND:
          chattingArea.append(protocol.getMsg() + "\r\n");
          System.out.println(protocol);
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