package catchMind;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Protocol implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L; // 버전 관리를 위한 serialVersionUID

  public static final int CMD_CONNECT = 1;
  public static final int CMD_DRAW = 2;
  public static final int CMD_CLEAR = 3;
  public static final int CMD_DISCONNECT = 4;

  private static final int TURN_DRAW = 1;
  private static final int TURN_ANSWER = 2;

  private int cmd; // 명령어
  private String msg; // 메시지
  private ArrayList<Dot> dots; // Dot 리스트

  // 기본 생성자
  public Protocol() {
    this.dots = new ArrayList<>(); // 기본적으로 빈 리스트로 초기화
  }

  // 매개변수가 있는 생성자 통합
  public Protocol(int cmd, String msg, ArrayList<Dot> dots) {
    this.cmd = cmd;
    this.msg = msg;
    this.dots = (dots != null) ? new ArrayList<>(dots) : new ArrayList<>();
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