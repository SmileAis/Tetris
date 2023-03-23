
import com.sun.tools.javac.Main;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FirstScreen{
    JButton start = new JButton("Start");
    JButton help = new JButton("Help");
    JRadioButton easy = new JRadioButton("Easy");
    JRadioButton normal = new JRadioButton("Normal");
    JRadioButton hard = new JRadioButton("Hard");
    ButtonGroup g = new ButtonGroup();
    JLabel title = new JLabel("Tetris");
    static String difficulty;
    JFrame frame = new JFrame("Tetris");
    JPanel panel = new JPanel();
    static int isStart = 1;

    JLabel lblMap = new JLabel("Maps: ");
    static String selectedMap;
    String[] Maps = {"Classic", "Stairs", "Diagonal", "Cliff"};
    JComboBox<String> Map = new JComboBox<>(Maps);

    // create Title
    public void createTitle(){
        title.setFont(new Font("serif", Font.BOLD, 50));
        title.setForeground(Color.yellow);
        title.setLocation(130, 80);
        title.setSize(200, 100);
        title.setBorder(new LineBorder(Color.yellow));
        title.setHorizontalAlignment(0);
    }

    //create Difficulty button
    public void createButton(){
        g.add(easy);
        g.add(normal);
        g.add(hard);

        easy.setLocation(170, 230);
        easy.setSize(100, 50);
        easy.setBackground(Color.darkGray);
        easy.setFont(new Font("serif", Font.BOLD, 25));
        easy.setForeground(Color.YELLOW);
        easy.addActionListener(new selectDifficulty());

        normal.setLocation(170, 300);
        normal.setSize(150, 50);
        normal.setBackground(Color.darkGray);
        normal.setFont(new Font("serif", Font.BOLD, 25));
        normal.setForeground(Color.GREEN);
        normal.addActionListener(new selectDifficulty());

        hard.setLocation(170, 370);
        hard.setSize(100, 50);
        hard.setBackground(Color.darkGray);
        hard.setFont(new Font("serif", Font.BOLD, 25));
        hard.setForeground(Color.RED);
        hard.addActionListener(new selectDifficulty());
    }

    //create start Button
    public void createStartBtn(){
        start.setLocation(150, 500);
        start.setSize(150, 50);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(difficulty == null){
                    JOptionPane.showMessageDialog(null, "���̵��� ���õ��� �ʾҽ��ϴ�!!", "���", JOptionPane.WARNING_MESSAGE);
                }else {
                    String startMessage = "������ �����Ͻðڽ��ϱ�?\n\n" +
                            "���̵� : " + difficulty +
                            "\n�� : " + selectedMap;
                    Object[] choice = {"Start", "Cancel"};
                    isStart = JOptionPane.showOptionDialog(null, startMessage, "Start",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, choice, choice[0]);

                    if(isStart == JOptionPane.OK_OPTION){
                        frame.dispose();
                    }
                }
            }
        });
    }

    //create help Button
    public void createHelpBtn(){
        help.setLocation(150, 560);
        help.setSize(150, 50);

        help.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String helpMessage = " * ���̵��� �ö󰨿� ���� �������� �ӵ� ���� �ö󰩴ϴ�.\n" +
                        " * ���̵��� ����:\n" +
                        "       easy    : 1.0��\n" +
                        "       Normal  : 1.6��\n" +
                        "       Hard    : 2.5��\n" +
                        " * �ѹ��� 2��, 3��, 4���� ���ִ� ��� ���� 10��, 20��, 30���� �߰��� ����ϴ�. \n\n" +
                        " * ���� �ð��� ������ ���� level�� �ö󰩴ϴ�.\n" +
                        " * level�� 10�ܰ���� �ֽ��ϴ�. \n\n" +
                        " * �⺻ ������ (��, ��, ��, ��)����Ű, �����̽��� �Դϴ�.\n" +
                        " * ��Ű�� �ð���� ȸ���Դϴ�.\n"+
                        " * �����̽��ٴ� drop �Դϴ�.\n";
                JOptionPane.showMessageDialog(null, helpMessage, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    //create Map Combobox
    public void createMapCombo(){
        lblMap.setLocation(150, 440);
        lblMap.setSize(150, 20);
        lblMap.setForeground(Color.yellow);
        Map.setLocation(150, 460);
        Map.setSize(150, 30);

        Map.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMap = Map.getSelectedItem().toString();
            }
        });
    }

    // Constructor
    public FirstScreen(){
        panel.setBackground(Color.darkGray);
        panel.setLayout(null);

        createTitle();
        createButton();
        createStartBtn();
        createHelpBtn();
        createMapCombo();

        frame.add(title);
        frame.add(easy);
        frame.add(normal);
        frame.add(hard);
        frame.add(start);
        frame.add(help);
        frame.add(lblMap);
        frame.add(Map);


        frame.setTitle("Tetris");
        frame.setLayout(null);
        frame.setLocation(300, 100);
        frame.setSize(470, 700);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

    }

    private class selectDifficulty implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            difficulty = e.getActionCommand();
        }
    }

}


