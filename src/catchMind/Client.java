package catchMind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Client extends JFrame {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private Thread listeningThread;
  private int position;
  private JPanel titlePanel, contentPanel, leftSidePanel, centerPanel, rightSidePanel, drawingPanel, controlPanel, palettePanel, scoreBoardPanel, chattingPanel;
  private JButton exitBtn, clearBtn, holdBtn;
  private boolean isDrawingAuthority = false;
  private final ArrayList<Dot> allDots = new ArrayList<>();
  private final ArrayList<Dot> currentStroke = new ArrayList<>();
  String msg;
  JTextField chattingField;
  JButton chattingSend;
  JTextArea chattingArea;

  public Client() {
    initializeNetwork();
    initializeUI();
    listeningThread = startListeningThread();
    listeningThread.start();
    addEventListener();
  }

  private void initializeNetwork() {
    try {
      // socket = new Socket("192.168.10.100", 5000);
      socket = new Socket("localhost", 5000);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      out.writeObject(new Protocol(position, Protocol.CMD_CONNECT, "", null));
      out.flush();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "네트워크 연결 실패: " + e.getMessage());
      System.exit(1);
    }
  }

  private void initializeUI() {
    titlePanel = new JPanel();
    titlePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    titlePanel.setPreferredSize(new Dimension(1500, 100));
    titlePanel.setBackground(new Color(0, 0, 0, 128));

    exitBtn = new JButton("Exit");
    exitBtn.setPreferredSize(new Dimension(100, 30));
    exitBtn.addActionListener(e -> {
      try {
        out.writeObject(new Protocol(position, Protocol.CMD_DISCONNECT, "", null));
        out.flush();
      } catch (IOException ignored) {}
    });

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
        if (!isDrawingAuthority) return;
        currentStroke.clear();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (!isDrawingAuthority) return;
        sendDots(currentStroke);
        allDots.addAll(currentStroke);
        currentStroke.clear();
        drawingPanel.repaint();
      }
    });

    drawingPanel.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if (!isDrawingAuthority) return;
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
    palettePanel.setLayout(new BoxLayout(palettePanel, BoxLayout.X_AXIS));
    palettePanel.setPreferredSize(new Dimension(900, 100));

    clearBtn = new JButton("Clear");
    clearBtn.setPreferredSize(new Dimension(100, 30));
    clearBtn.addActionListener(e -> {
      try {
        out.writeObject(new Protocol(position, Protocol.CMD_CLEAR, "", null));
        out.flush();
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    });

    holdBtn = new JButton("Hold");
    holdBtn.setPreferredSize(new Dimension(100, 30));
    holdBtn.addActionListener(e -> {
      isDrawingAuthority = !isDrawingAuthority;
    });

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


    titlePanel.add(exitBtn);

    chattingArea = new JTextArea(); // 채팅 메시지를 보여주는 영역
    chattingArea.setEditable(false);
    chattingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    chattingPanel.add(new JScrollPane(chattingArea), BorderLayout.CENTER); // 스크롤 가능하게 설정

    chattingPanel.add(bottomPanel, BorderLayout.SOUTH);

    chattingSend.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("chk");
        msg = chattingField.getText();
        System.out.println(msg);
        if(!msg.isEmpty()) {
          Protocol p = new Protocol();
          p.setCmd(5);
          p.setMsg(msg);
          chattingField.setText("");
          try {
            out.writeObject(p);
          } catch (IOException ex) {
            System.out.println("addActionListener Error: " + ex.getMessage());
          }

          System.out.println("p.getCmd : " + p.getCmd());
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

    palettePanel.add(clearBtn);
    palettePanel.add(holdBtn);

    Client.this.setLayout(new BorderLayout());

    Client.this.add(titlePanel, BorderLayout.NORTH);
    Client.this.add(contentPanel, BorderLayout.CENTER);

    Client.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Client.this.setSize(1500, 1000);
    Client.this.setTitle("Catch Mind");
    Client.this.setLocationRelativeTo(null);
    Client.this.setVisible(true);
  }

  private Thread startListeningThread() {
    return new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
          try {
            Object obj = in.readObject();
            if (obj instanceof Protocol protocol) {
              handleProtocol(protocol);
            }
          } catch (EOFException e) {
            System.err.println("서버 연결 중단: " + e.getMessage());
            break;
          } catch (IOException | ClassNotFoundException e) {
            System.err.println("오류 발생: " + e.getMessage());
            break;
          }
        }
      } finally {
        closeConnection();
      }
    });
  }


  private void addEventListener() {
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        try {
          out.writeObject(new Protocol(position, Protocol.CMD_DISCONNECT, "", null));
          out.flush();
        } catch (IOException ignored) {}
      }
    });
  }

  private void handleProtocol(Protocol protocol) {
    SwingUtilities.invokeLater(() -> {
      switch (protocol.getCmd()) {
        case Protocol.CMD_CONNECT:
          this.position = protocol.getPosition();
          JOptionPane.showMessageDialog(this, "환영합니다.");
          break;
        case Protocol.CMD_GET_DRAWERS_ALL_DOTS:
          sendAllDots();
          break;
        case Protocol.CMD_CAN_DRAWING:
          isDrawingAuthority = true;
          break;
        case Protocol.CMD_CANNOT_DRAWING:
          isDrawingAuthority = false;
          break;
        case Protocol.CMD_DRAW:
          handleReceivedDots(protocol.getDots());
          break;
        case Protocol.CMD_CLEAR:
          clearAllDots();
          break;
        case Protocol.CMD_DISCONNECT:
          closeConnection();
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
      out.writeObject(new Protocol(position, Protocol.CMD_DRAW, "", dots));
      out.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendAllDots() {
    try {
      out.writeObject(new Protocol(position, Protocol.CMD_DRAW, "", allDots));
      out.flush();
    } catch (IOException e) {
      System.err.println("도트 전송 중 오류 발생: " + e.getMessage());
    }
  }

  private void handleReceivedDots(ArrayList<Dot> receivedDots) {
    if (receivedDots != null) {
      allDots.addAll(receivedDots);
      drawingPanel.repaint();
    }
  }

  private void clearAllDots() {
    allDots.clear();
    currentStroke.clear();
    drawingPanel.repaint();
  }

  private void closeConnection() {
    try {
      if (listeningThread != null && listeningThread.isAlive()) {
        listeningThread.interrupt();
        if (socket != null && !socket.isClosed()) {
          socket.shutdownInput();
        }
        listeningThread.join(5000);
      }
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (IOException | InterruptedException e) {
      System.err.println("연결 종료 중 오류 발생: " + e.getMessage());
    } finally {
      SwingUtilities.invokeLater(() -> {
        if (isDisplayable()) {
          JOptionPane.showMessageDialog(this, "서버와의 연결이 종료되었습니다.", "연결 종료", JOptionPane.INFORMATION_MESSAGE);
          dispose();
        }
        System.exit(0);
      });
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Client::new);
  }
}