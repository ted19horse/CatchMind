package catchMind;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Protocol implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L; // 버전 관리를 위한 serialVersionUID

  public static final int CMD_CONNECT = 1;
  public static final int CMD_GET_DRAWERS_ALL_DOTS = 2;
  public static final int CMD_SET_DRAWERS_ALL_DOTS = 3;
  public static final int CMD_CAN_DRAWING = 4;
  public static final int CMD_CANNOT_DRAWING = 5;
  public static final int CMD_DRAW = 6;
  public static final int CMD_CLEAR = 7;
  public static final int CMD_MSG_SEND = 8;
  public static final int CMD_DISCONNECT = 9;
  public static final int CMD_SERVER_IS_FULL = 10;

  private int position;
  private int cmd; // 명령어
  private String msg; // 메시지
  private ArrayList<ArrayList<Dot>> allDots;
  private ArrayList<Dot> dots; // Dot 리스트

  // 기본 생성자
  public Protocol() {
    this.dots = new ArrayList<>(); // 기본적으로 빈 리스트로 초기화
  }

  public Protocol(int cmd, String msg, ArrayList<ArrayList<Dot>> allDots) {
    this.cmd = cmd;
    this.msg = msg;
    this.allDots = allDots;
  }

  // 매개변수가 있는 생성자 통합
  public Protocol(int position, int cmd, String msg, ArrayList<Dot> dots) {
    this.position = position;
    this.cmd = cmd;
    this.msg = msg;
    this.dots = (dots != null) ? new ArrayList<>(dots) : new ArrayList<>();
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getCmd() {
    return cmd;
  }

  public void setCmd(int cmd) {
    this.cmd = cmd;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public ArrayList<ArrayList<Dot>> getAllDots() {
    return allDots;
  }

  public void setAllDots(ArrayList<ArrayList<Dot>> allDots) {
    this.allDots = allDots;
  }

  public ArrayList<Dot> getDots() {
    return dots;
  }

  public void setDots(ArrayList<Dot> dots) {
    this.dots = (dots != null) ? new ArrayList<>(dots) : new ArrayList<>();
  }

  @Override
  public String toString() {
    return "Protocol{" +
        "cmd=" + cmd +
        ", msg='" + msg + '\'' +
        ", dots=" + (dots != null ? dots.size() : "null") +
        '}';
  }
}