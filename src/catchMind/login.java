package catchMind;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Reader;




public class login extends JFrame {
    SqlSessionFactory factory;


        public login() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1500,900);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        dbConnect();

        JPanel login_p = new JPanel();
        login_p.setLayout(new GridLayout(3,2,10,10));

        JLabel idLabel = new JLabel("ID : ");
        JTextArea idText = new JTextArea();

        JLabel pwLabel = new JLabel("PW : ");
        JTextArea pwText = new JTextArea();

        JButton loginBt = new JButton("Login");

        loginBt.addActionListener(new ActionListener() {
            @Override
            //로그인 로직은 MyBatis를 이용해 구현. (SELECT문으로 ID와 PW를 비교.)
            public void actionPerformed(ActionEvent e) {
                String id = idText.getText();
                String pw = pwText.getText();
            }
        });

        login_p.add(idLabel);
        login_p.add(idText);
        login_p.add(pwLabel);
        login_p.add(pwText);

        JPanel btn_p = new JPanel();
        btn_p.add(loginBt);

        add(login_p, BorderLayout.CENTER);
        add(btn_p, BorderLayout.SOUTH);
    }

    private void dbConnect() {
        try{
            Reader r = Resources.getResourceAsReader("config/config.xml");
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            factory = builder.build(r);
            r.close();
            this.setTitle("Welcome to catchMind!");
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        new login();
    }
}
