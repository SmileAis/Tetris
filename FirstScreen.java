
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
                    JOptionPane.showMessageDialog(null, "난이도가 선택되지 않았습니다!!", "경고", JOptionPane.WARNING_MESSAGE);
                }else {
                    String startMessage = "게임을 시작하시겠습니까?\n\n" +
                            "난이도 : " + difficulty +
                            "\n맵 : " + selectedMap;
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
                String helpMessage = " * 난이도가 올라감에 따라 떨어지는 속도 또한 올라갑니다.\n" +
                        " * 난이도별 점수:\n" +
                        "       easy    : 1.0배\n" +
                        "       Normal  : 1.6배\n" +
                        "       Hard    : 2.5배\n" +
                        " * 한번에 2줄, 3줄, 4줄을 없애는 경우 각각 10점, 20점, 30점을 추가로 얻습니다. \n\n" +
                        " * 일정 시간이 지남에 따라 level이 올라갑니다.\n" +
                        " * level은 10단계까지 있습니다. \n\n" +
                        " * 기본 조작은 (↑, ↓, ←, →)방향키, 스페이스바 입니다.\n" +
                        " * ↑키는 시계방향 회전입니다.\n"+
                        " * 스페이스바는 drop 입니다.\n";
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


