import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.sql.Blob;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

class Tetris extends JFrame implements KeyListener {
    final int BLOCK_SIZE = 30;      // ���ũ��
    final int LEFT_MARGIN = 30;     // ���� ���� ����
    final int TOP_MARGIN = 60;      // ���� ���� ����
    Color[] blockColor = {Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, new Color(102,224,255)};   // �� ��� ��

    int[][] MainBoard = new int[10][18];    // ���κ��� 0/1 ǥ��
    int[][] MainBoardX = new int[10][18];   // ���κ��� x�� ��ǥ��
    int[][] MainBoardY = new int[10][18];   // ���κ��� y�� ��ǥ��

    int[] CurrentBlockX = new int[4];       // ���� �����̴� ��� X��ǥ
    int[] CurrentBlockY = new int[4];       // ���� �����̴� ��� Y��ǥ
    int CurrentBlockRotate = 0;         // ���� �����̴� ��� ȸ�� ����
    int[] tmpX = new int[4];    // ����� ������X ��ġ
    int[] tmpY = new int[4];    // ����� ������Y ��ġ

    int nowTypeNum;         // ���� ��� Ÿ��
    int nextTypeNum;        // ���� ��� Ÿ��

    int score = 0;          // ����
    int level = 1;          // ����
    int delay;              // �������� �ӵ�
    static String difficulty ;  // ���̵�

    boolean confirmDraw = false;

    boolean drop = false;   // drop�ߴ��� ����
    static boolean restart = false; // ������ ���� �� ����� ����
    int time = 0;   // ���� ������ ���� Ÿ�̸�

    JLabel diffLabel, levelLabel, scoreLabel;
    JTextField diffTf, levelTf, scoreTf;

    Object[] endOptions={"Retry", "End"};   // ���� ����

    //ȭ�� ������ �� �ذ��ϱ� ���� ���� ���۸� �ʿ�
    Image buffImage;
    Graphics bg;

    static String selectedMap;

    public Tetris() {
    	restart = false;
    	
        //���� �ʱ�ȭ
        createInfo();
        initBoard();
        setDelay();
        setMap();

        // ������ �ʱ�ȭ
        setTitle("Tetris");
        setLayout(null);
        setLocation(300, 100);
        setSize(560, 700);
        setVisible(true);
        getContentPane().setBackground(Color.DARK_GRAY);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        addKeyListener(this);
        setFocusable(true);

        // �ð� üũ ������ ����.
        TimeCounterThread timer = new TimeCounterThread();
        Thread t = new Thread(timer);
        t.start();

        //���� ����
        nextType();
        while (true) {
            nowTypeNum = nextTypeNum;
            blockType(nowTypeNum);
            nextType();

            if (!drop) {
                fall();
            }

            clearLine();
            drop = false;

            //���� ������
            if (isEnd()) {
            	restart = true;
                String info = "���̵� : " + difficulty +
                        "\n����   :  " + level +
                        "\n����   :  " + score +
                        "\n��       :  "+ selectedMap;
                int n = JOptionPane.showOptionDialog(null, info, "End", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, endOptions, endOptions[0]);
                if (n == JOptionPane.YES_OPTION) {
                    restart = true;
                    difficulty= null;
                    initBoard();
                    dispose();
                    break;

                } else {
                    System.exit(0);
                }
            }

        }

    }


    // ���� �� Ÿ�� ����
    public void nextType(){
        Random x = new Random();
        nextTypeNum = x.nextInt(7);
    }


    ///////////////////////
    //  ���� �ʱ�ȭ
    ///////////////////////
    public void initBoard(){    // ó�� ���� ����
        for(int i=0; i<10; i++){
            for(int j=0; j<18; j++){
                MainBoard[i][j] = 0;
                MainBoardX[i][j] = LEFT_MARGIN + i * 30;
                MainBoardY[i][j] = TOP_MARGIN + j * 30;
            }
        }
        score = 0;
        level = 1;
        scoreTf.setText(score + "");
        levelTf.setText(level + "");
        diffTf.setText(difficulty);
        if(difficulty == "Easy")
            diffTf.setForeground(Color.YELLOW);
        else if(difficulty == "Normal")
            diffTf.setForeground(Color.green);
        else
            diffTf.setForeground(Color.RED);
    }
    public void createInfo(){   // ���� ���� ����
        diffLabel = new JLabel("Difficulty :");
        diffTf = new JTextField(10);
        levelLabel = new JLabel("Level :");
        levelTf = new JTextField(10);
        scoreLabel = new JLabel("Score :");
        scoreTf = new JTextField(10);

        diffLabel.setLocation(BLOCK_SIZE*13, 370);
        diffLabel.setSize(130, 15);
        diffLabel.setForeground(Color.cyan);
        add(diffLabel);

        diffTf.setLocation(BLOCK_SIZE*13, 385);
        diffTf.setSize(130, 20);
        diffTf.setEditable(false);
        diffTf.setText(difficulty);
        diffTf.setBackground(Color.gray);
        diffTf.setHorizontalAlignment(SwingConstants.RIGHT);
        add(diffTf);

        levelLabel.setLocation(BLOCK_SIZE*13, 425);
        levelLabel.setSize(130, 15);
        levelLabel.setForeground(Color.cyan);
        add(levelLabel);

        levelTf.setLocation(BLOCK_SIZE*13, 440);
        levelTf.setSize(130, 20);
        levelTf.setEditable(false);
        levelTf.setHorizontalAlignment(SwingConstants.RIGHT);
        levelTf.setBackground(Color.gray);
        levelTf.setText(level+"");
        levelTf.setForeground(Color.CYAN);
        add(levelTf);

        scoreLabel.setLocation(BLOCK_SIZE*13, 470);
        scoreLabel.setSize(130, 25);
        scoreLabel.setForeground(Color.cyan);
        add(scoreLabel);

        scoreTf.setLocation(BLOCK_SIZE*13, 495);
        scoreTf.setSize(130, 20);
        scoreTf.setEditable(false);
        scoreTf.setHorizontalAlignment(SwingConstants.RIGHT);
        scoreTf.setBackground(Color.gray);
        scoreTf.setForeground(Color.cyan);
        scoreTf.setText(score + "");
        add(scoreTf);

    }
    public void setDelay(){     // �������� �ӵ� ����
        if(difficulty == "Easy")
            delay = (int)(500 - 30 * level);
        else if(difficulty == "Normal")
            delay = (int)(400 - 25 * level);
        else
            delay = (int)(300 - 20 * level);
    }
    public void setMap(){
        new Map();
        if(selectedMap == "Classic")
            MainBoard = Map.classic;
        else if(selectedMap == "Stairs")
            MainBoard = Map.stairs;
        else if(selectedMap == "Diagonal")
            MainBoard = Map.diagonal;
        else if(selectedMap == "Cliff")
            MainBoard = Map.cliff;
    }

    // ��� ����
    public void blockType(int typeNum){
        CurrentBlockRotate = 0;
        if(typeNum == 0){   // s
            CurrentBlockX[0] = 150; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 180; CurrentBlockY[1] = 0;
            CurrentBlockX[2] = 120; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 150; CurrentBlockY[3] = 30;
        }else if (typeNum == 1){    // z
            CurrentBlockX[0] = 120; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 150; CurrentBlockY[1] = 0;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 2){    // ����
            CurrentBlockX[0] = 120; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 120; CurrentBlockY[1] = 30;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 3){    // ����
            CurrentBlockX[0] = 180; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 120; CurrentBlockY[1] = 30;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 4){    // ��
            CurrentBlockX[0] = 150; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 180; CurrentBlockY[1] = 0;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 5){    // ��
            CurrentBlockX[0] = 150; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 120; CurrentBlockY[1] = 30;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 6){
            CurrentBlockX[0] = 120; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 150; CurrentBlockY[1] = 0;
            CurrentBlockX[2] = 180; CurrentBlockY[2] = 0;
            CurrentBlockX[3] = 210; CurrentBlockY[3] = 0;
        }
    }

    //////////////////////////
    //   ���� �� ��� �׸���
    /////////////////////////
    public void paint(Graphics g){
        buffImage = createImage(560, 700);;
        bg = buffImage.getGraphics();

        paintComponents(bg);
        drawMainBoard(bg);
        drawLine(bg);
        drawNextBoard(bg);

        drawStopBlock(bg);
        drawBlock(bg);
        drawNextBlock(bg);

        bg.drawImage(buffImage, 0, 0, this);
        g.drawImage(buffImage, 0, 0, null);
    }
    public void drawMainBoard(Graphics g) { //���Ӻ��� �׸���
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 18; j++) {
                g.drawRect(LEFT_MARGIN + i * BLOCK_SIZE, TOP_MARGIN + j * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        g.setColor(Color.CYAN);
        g.drawRect(LEFT_MARGIN, TOP_MARGIN, BLOCK_SIZE * 10, BLOCK_SIZE * 18);
    }
    public void drawLine(Graphics g) {  // ������ �׸���
        g.setColor(Color.cyan);
        g.drawLine(30 * 12, 0, 30 * 12, 700);
    }
    public void drawNextBoard(Graphics g) {     //���� ��� ���� �׸���
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                g.drawRect(30 * 13 + i * BLOCK_SIZE, 30 * 4 + j * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        g.setColor(Color.CYAN);
        g.drawRect(30 * 13, 30 * 4, BLOCK_SIZE * 4, BLOCK_SIZE * 4);
    }
    public void drawStopBlock(Graphics g) {      // ���� �����ִ� ��ϵ� �׸���
        if(confirmDraw && !isEnd() )
            for (int i = 0; i < 4; i++) {
                if(!((tmpY[i] - TOP_MARGIN) / BLOCK_SIZE < 0))
                    MainBoard[(tmpX[i] - LEFT_MARGIN) / BLOCK_SIZE][(tmpY[i] - TOP_MARGIN) / BLOCK_SIZE] = 1;
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 18; j++) {
                if (MainBoard[i][j] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(MainBoardX[i][j], MainBoardY[i][j], BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawRect(MainBoardX[i][j], MainBoardY[i][j], BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
        if(!drop){
            clearLine();
            repaint();
        }
    }


    public void drawNextBlock(Graphics g){      // ���� ��� �׸���
        g.setColor(blockColor[nextTypeNum]);
        if(nextTypeNum == 0){
            g.fillRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 1){
            g.fillRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 2){
            g.fillRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 3){
            g.fillRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 4){
            g.fillRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 5){
            g.fillRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*14, 30*5, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }else if(nextTypeNum == 6){
            g.fillRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.fillRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.setColor(Color.WHITE);
            g.drawRect(30*13, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*14, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*15, 30*6, BLOCK_SIZE, BLOCK_SIZE);
            g.drawRect(30*16, 30*6, BLOCK_SIZE, BLOCK_SIZE);
        }
    }
    public void drawBlock(Graphics g){  // ���� ��� �׸���
        if(!isEnd())
            for(int i=0; i<4; i++){
                g.setColor(blockColor[nowTypeNum]);
                g.fillRect(CurrentBlockX[i], CurrentBlockY[i], BLOCK_SIZE, BLOCK_SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(CurrentBlockX[i], CurrentBlockY[i], BLOCK_SIZE, BLOCK_SIZE);
            }
    }


    // ��� ������ ����
    public void fall(){
        while(!drop) {
            for(int i=0; i<4; i++)
                CurrentBlockY[i] += BLOCK_SIZE;

            try {
                Thread.sleep(delay);
                repaint();
            } catch (InterruptedException ex) {}

            if(isFloor()){
                if(!confirmDraw)
                    confirmDraw = true;
                for(int i=0; i<4; i++){
                    tmpX[i] = CurrentBlockX[i];
                    tmpY[i] = CurrentBlockY[i];
                }
                return;
            }else if(checkStop()) {
                if(!confirmDraw)
                    confirmDraw = true;
                for(int i=0; i<4; i++){
                    tmpX[i] = CurrentBlockX[i];
                    tmpY[i] = CurrentBlockY[i];
                }
                return;
            }
        }
    }


    ///////////////////////
    // Ű �Է� Event
    ///////////////////////
    @Override
    public void keyPressed(KeyEvent e) {
        if (!drop){
            // '��'Ű �Է� ��
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                if (isFloor() || checkStop())
                    return;
//                System.out.println("U");


                switch (nowTypeNum) {
                    case 0:
                        if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3) {
                            if ((CurrentBlockX[2] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE > 0 && (CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE > 0)
                                if (MainBoard[(CurrentBlockX[2] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                } else
                                    return;
                            else{   // ���� ��ĭ�ǰ��
                                    CurrentBlockRotate++;
                            }
                        }
                        else {    // ��� ȸ�����°� 0 �Ǵ� 2�ΰ��
                            CurrentBlockRotate++;
                        }
                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock0();
                        break;
                    case 1:
                        if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3) {
                            if ((CurrentBlockX[3] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE < 10 && (CurrentBlockY[3] - TOP_MARGIN) / BLOCK_SIZE > 0)
                                if (MainBoard[(CurrentBlockX[3] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[3] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                } else
                                    return;
                            else{   // ������ ��ĭ�ǰ��
                                CurrentBlockRotate++;
                            }
                        }
                        else {    // ��� ȸ�����°� 0 �Ǵ� 2�ΰ��
                            CurrentBlockRotate++;
                        }

                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock1();
                        break;
                    case 2:
                        if(CurrentBlockRotate == 1){
                            if ((CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE > 0 && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else if(CurrentBlockRotate == 3){
                            if ((CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE > 0
                                    && (CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE < 9
                                    && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else{
                            CurrentBlockRotate++;
                        }
                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock2();
                        break;
                    case 3:
                        if(CurrentBlockRotate == 1){
                            if ((CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE > 0
                                    &&(CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE < 9
                                    && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else if(CurrentBlockRotate == 3){
                            if ((CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE > 0 && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else{
                            CurrentBlockRotate++;
                        }

                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock3();
                        break;
                    case 4:     // �� block.
                        break;
                    case 5:
                        if(CurrentBlockRotate == 1){
                            if ((CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE > 0
                                    &&(CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE < 9
                                    && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else if(CurrentBlockRotate == 3){
                            if ((CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE > 0 && (CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE > 0) {
                                if (MainBoard[(CurrentBlockX[1] - LEFT_MARGIN - BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[1] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                }
                                else
                                    return;
                            }
                            else
                                CurrentBlockRotate++;
                        }
                        else{
                            CurrentBlockRotate++;
                        }

                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock5();
                        break;
                    case 6:
                        if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3) {
                            if ((CurrentBlockX[2] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE < 7
                                    && (CurrentBlockX[2] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE > 0  && (CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE > 0)
                                if (MainBoard[(CurrentBlockX[2] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE] != 1
                                        && MainBoard[(CurrentBlockX[2] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE] != 1
                                        && MainBoard[(CurrentBlockX[2] - LEFT_MARGIN + BLOCK_SIZE * 2) / BLOCK_SIZE][(CurrentBlockY[2] - TOP_MARGIN) / BLOCK_SIZE] != 1) {
                                    CurrentBlockRotate++;
                                } else
                                    return;
                            else{   // ������ ��ĭ�ǰ��
                                CurrentBlockRotate++;
                            }
                        }
                        else {    // ��� ȸ�����°� 0 �Ǵ� 2�ΰ��
                            CurrentBlockRotate++;
                        }

                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock6();
                        break;
                    }
                }
            // '��'Ű �Է� ��
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                System.out.println("D");
                if (!isFloor() && !checkStop()) {
                    CurrentBlockY[0] += BLOCK_SIZE;
                    CurrentBlockY[1] += BLOCK_SIZE;
                    CurrentBlockY[2] += BLOCK_SIZE;
                    CurrentBlockY[3] += BLOCK_SIZE;
                }
            }
            // '��'Ű �Է� ��
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                System.out.println("L");
                if (checkLeft()) {
                    for (int i = 0; i < 4; i++) {
                        CurrentBlockX[i] -= 30;
                    }
                }
            }
            // '��'Ű �Է� ��
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                System.out.println("R");
                if (checkRight()) {
                    for (int i = 0; i < 4; i++) {
                        CurrentBlockX[i] += 30;
                    }
                }
            }
            // '�����̽���' �Է� ��
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                drop = true;
                while (true) {
                    if (!isFloor() && !checkStop()) {
                        for (int i = 0; i < 4; i++) {
                            CurrentBlockY[i] += BLOCK_SIZE;
                        }
                    }
                    if (checkStop() || isFloor()) {
                        for (int i = 0; i < 4; i++) {     // ���� �ȱ׷����� ���� ������ �߰�
                            if(CurrentBlockY[i] < 60)
                                continue;
                            MainBoard[(CurrentBlockX[i] - LEFT_MARGIN) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE] = 1;
                        }
                        break;
                    }

                }
//                System.out.println("Space");
            }
        }
        repaint();
    }
    @Override
    public void keyReleased(KeyEvent e){}
    @Override
    public void keyTyped(KeyEvent e){}


    ///////////////////////
    // ������ ���� Ȯ��
    ///////////////////////
    public boolean checkLeft(){
        // ������ ���� �����ΰ��
        if(CurrentBlockX[0] == LEFT_MARGIN || CurrentBlockX[1] == LEFT_MARGIN
                || CurrentBlockX[2] == LEFT_MARGIN || CurrentBlockX[3] == LEFT_MARGIN)
            return false;

        // �����̴� ��� �ٷ� ���ʿ� ����� �ִ°��, ���� �������� ����� �ʴ���.
        for(int i=0; i<4; i++) {
            if(CurrentBlockX[i] != LEFT_MARGIN && !((CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE < 0)) {
                if (MainBoard[(CurrentBlockX[i] - LEFT_MARGIN - BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE] == 1) {
                    return false;
                }
            }
        }

        return true;
    }
    public boolean checkRight(){
        // ������ ���� �������̸� ������ x
        if(CurrentBlockX[0] == BLOCK_SIZE*10 || CurrentBlockX[1] == BLOCK_SIZE*10 ||
                CurrentBlockX[2] == BLOCK_SIZE*10 ||CurrentBlockX[3] == BLOCK_SIZE*10 )
            return false;

        // �����̴� ��� �����ʿ� ����� �׷��� �ִ°��
        for(int i=0; i<4; i++) {
            if(CurrentBlockX[i] != LEFT_MARGIN + BLOCK_SIZE*9 && !((CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE < 0)) {
                if (MainBoard[(CurrentBlockX[i] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE] == 1) {
                    return false;
                }
            }
        }
        return true;
    }public void checkTurn(){        // ȸ���������� Ȯ��
        switch(nowTypeNum) {
            case 0:
                if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
                    if(CurrentBlockX[0] == LEFT_MARGIN || CurrentBlockX[1] == LEFT_MARGIN){
                        for(int i=0; i<4; i++)
                            CurrentBlockX[i] += BLOCK_SIZE;
                    }
                }   // ���ʺ����� ��
                break;
            case 1:
                if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
                    if(CurrentBlockX[0] == BLOCK_SIZE*10 || CurrentBlockX[1] == BLOCK_SIZE*10){
                        for(int i=0; i<4; i++)
                            CurrentBlockX[i] -= BLOCK_SIZE;
                    }
                }
                break;
            case 2:
                if(CurrentBlockRotate == 2 && CurrentBlockX[3] == LEFT_MARGIN)
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] += BLOCK_SIZE;

                if(CurrentBlockRotate == 0 && CurrentBlockX[3] == BLOCK_SIZE*10)
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] -= BLOCK_SIZE;
                break;
            case 3:
                if(CurrentBlockRotate == 0 && CurrentBlockX[0] == LEFT_MARGIN)
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] += BLOCK_SIZE;
                if(CurrentBlockRotate == 2 && CurrentBlockX[0] == BLOCK_SIZE*10 )
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] -= BLOCK_SIZE;
                break;
            case 4:     // �� block.
                break;
            case 5:
                if(CurrentBlockRotate == 0 && CurrentBlockX[0] == LEFT_MARGIN)
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] += BLOCK_SIZE;

                if(CurrentBlockRotate == 2 && CurrentBlockX[0] == BLOCK_SIZE*10)
                    for (int i = 0; i < 4; i++)
                        CurrentBlockX[i] -= BLOCK_SIZE;
                break;
            case 6:
                if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
                    if(CurrentBlockX[0] == LEFT_MARGIN)
                        for (int i = 0; i < 4; i++)
                            CurrentBlockX[i] += BLOCK_SIZE;

                    if(CurrentBlockX[0] == BLOCK_SIZE*9)
                        for (int i = 0; i < 4; i++)
                            CurrentBlockX[i] -= BLOCK_SIZE;

                    if(CurrentBlockX[0] == BLOCK_SIZE*10)
                        for (int i = 0; i < 4; i++)
                            CurrentBlockX[i] -= BLOCK_SIZE*2;
                }
                break;
        }
    }


    /////////////////////
    // ��� ȸ��
    ////////////////////
    public void turnBlock0(){
        if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
            CurrentBlockX[1] += BLOCK_SIZE; CurrentBlockY[1] += BLOCK_SIZE;
            CurrentBlockX[2] -= BLOCK_SIZE*2; CurrentBlockY[2] += 0;
            CurrentBlockX[3] -= BLOCK_SIZE; CurrentBlockY[3] += BLOCK_SIZE;
        }else if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3){
            CurrentBlockX[1] -= BLOCK_SIZE; CurrentBlockY[1] -= BLOCK_SIZE;
            CurrentBlockX[2] += BLOCK_SIZE*2; CurrentBlockY[2] += 0;
            CurrentBlockX[3] += BLOCK_SIZE; CurrentBlockY[3] -= BLOCK_SIZE;
        }

    }
    public void turnBlock1(){
        if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
            CurrentBlockX[0] -= BLOCK_SIZE; CurrentBlockY[0] += BLOCK_SIZE;
            CurrentBlockX[2] += BLOCK_SIZE; CurrentBlockY[2] += BLOCK_SIZE;
            CurrentBlockX[3] += BLOCK_SIZE*2; CurrentBlockY[3] += 0;
        }else if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3){
            CurrentBlockX[0] += BLOCK_SIZE; CurrentBlockY[0] -= BLOCK_SIZE;
            CurrentBlockX[2] -= BLOCK_SIZE; CurrentBlockY[2] -= BLOCK_SIZE;
            CurrentBlockX[3] -= BLOCK_SIZE*2; CurrentBlockY[3] += 0;
        }
    }
    public void turnBlock2(){
        if(CurrentBlockRotate == 0){
            CurrentBlockY[0] -= BLOCK_SIZE;
            CurrentBlockX[1] -= BLOCK_SIZE;
            CurrentBlockY[2] += BLOCK_SIZE;
            CurrentBlockX[3] += BLOCK_SIZE; CurrentBlockY[3] += BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 1){
            CurrentBlockX[0] += BLOCK_SIZE;
            CurrentBlockY[1] -= BLOCK_SIZE;
            CurrentBlockX[2] -= BLOCK_SIZE;
            CurrentBlockX[3] -= BLOCK_SIZE*2; CurrentBlockY[3] += BLOCK_SIZE;
        }else if(CurrentBlockRotate == 2){
            CurrentBlockY[0] += BLOCK_SIZE;
            CurrentBlockX[1] += BLOCK_SIZE;
            CurrentBlockY[2] -= BLOCK_SIZE;
            CurrentBlockX[3] -= BLOCK_SIZE; CurrentBlockY[3] -= BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 3){
            CurrentBlockX[0] -= BLOCK_SIZE;
            CurrentBlockY[1] += BLOCK_SIZE;
            CurrentBlockX[2] += BLOCK_SIZE;
            CurrentBlockX[3] += BLOCK_SIZE*2; CurrentBlockY[3] -= BLOCK_SIZE;
        }
    }
    public void turnBlock3(){
        if(CurrentBlockRotate == 0){
            CurrentBlockX[0] += BLOCK_SIZE;
            CurrentBlockY[3] += BLOCK_SIZE;
            CurrentBlockX[2] -= BLOCK_SIZE;
            CurrentBlockX[1] -= BLOCK_SIZE*2; CurrentBlockY[1] -= BLOCK_SIZE;
        }else if(CurrentBlockRotate == 1){
            CurrentBlockY[0] += BLOCK_SIZE;
            CurrentBlockX[3] -= BLOCK_SIZE;
            CurrentBlockY[2] -= BLOCK_SIZE;
            CurrentBlockX[1] += BLOCK_SIZE; CurrentBlockY[1] -= BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 2){
            CurrentBlockX[0] -= BLOCK_SIZE;
            CurrentBlockY[3] -= BLOCK_SIZE;
            CurrentBlockX[2] += BLOCK_SIZE;
            CurrentBlockX[1] += BLOCK_SIZE*2; CurrentBlockY[1] += BLOCK_SIZE;
        }else if(CurrentBlockRotate == 3){
            CurrentBlockY[0] -= BLOCK_SIZE;
            CurrentBlockX[3] += BLOCK_SIZE;
            CurrentBlockY[2] += BLOCK_SIZE;
            CurrentBlockX[1] -= BLOCK_SIZE; CurrentBlockY[1] += BLOCK_SIZE*2;
        }
    }
    public void turnBlock5(){
        if(CurrentBlockRotate == 0){
            CurrentBlockX[1] -= BLOCK_SIZE *2;
            CurrentBlockX[2] -= BLOCK_SIZE; CurrentBlockY[2] += BLOCK_SIZE;
            CurrentBlockY[3] += BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 1){
            CurrentBlockY[1] -= BLOCK_SIZE *2;
            CurrentBlockX[2] -= BLOCK_SIZE; CurrentBlockY[2] -= BLOCK_SIZE;
            CurrentBlockX[3] -= BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 2){
            CurrentBlockX[1] += BLOCK_SIZE *2;
            CurrentBlockX[2] += BLOCK_SIZE; CurrentBlockY[2] -= BLOCK_SIZE;
            CurrentBlockY[3] -= BLOCK_SIZE*2;
        }else if(CurrentBlockRotate == 3){
            CurrentBlockY[1] += BLOCK_SIZE *2;
            CurrentBlockX[2] += BLOCK_SIZE; CurrentBlockY[2] += BLOCK_SIZE;
            CurrentBlockX[3] += BLOCK_SIZE*2;
        }
    }
    public void turnBlock6(){
        if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
            CurrentBlockX[0] -= BLOCK_SIZE; CurrentBlockY[0] += BLOCK_SIZE*2;
            CurrentBlockX[1] += 0; CurrentBlockY[1] += BLOCK_SIZE;
            CurrentBlockX[2] += BLOCK_SIZE; CurrentBlockY[2] += 0;
            CurrentBlockX[3] += BLOCK_SIZE*2; CurrentBlockY[3] -= BLOCK_SIZE;
        } else if(CurrentBlockRotate == 1 || CurrentBlockRotate == 3){
            CurrentBlockX[0] += BLOCK_SIZE; CurrentBlockY[0] -= BLOCK_SIZE*2;
            CurrentBlockX[1] += 0; CurrentBlockY[1] -= BLOCK_SIZE;
            CurrentBlockX[2] -= BLOCK_SIZE; CurrentBlockY[2]+= 0;
            CurrentBlockX[3] -= BLOCK_SIZE*2; CurrentBlockY[3] += BLOCK_SIZE;
        }
    }

    // ���� ����� �ٴ����� Ȯ��
    public boolean isFloor(){
        if(CurrentBlockY[0] == TOP_MARGIN + BLOCK_SIZE * 17 || CurrentBlockY[1] == TOP_MARGIN + BLOCK_SIZE * 17 ||
               CurrentBlockY[2] == TOP_MARGIN + BLOCK_SIZE * 17 || CurrentBlockY[3] == TOP_MARGIN + BLOCK_SIZE * 17) {
            return true;
        }
        return false;
    }
    // ���� ĭ�� ����ִ��� Ȯ��
    public boolean checkStop(){
        for(int i=0; i<4; i++) {
            // �ٴ��� �ƴϰ�, ������ �ε������� �Ѿ�� �ʴ°��
            if((!isFloor()) && !((CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE < 0))
                if (MainBoard[(CurrentBlockX[i] - LEFT_MARGIN) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN + BLOCK_SIZE) / BLOCK_SIZE ] == 1) {
                    return true;
                }
        }
        return false;
    }

    // �� ���ٿ� ����� ��Ҵ��� Ȯ��
    public boolean isEnd(){
        for(int i=0; i<10; i++){
            // ���� ���� �� ��ĭ�̶� ���ִ� ���
            if(MainBoard[i][0] == 1) {
                return true;
            }
        }
         return false;
    }

    // �� ������ �������� �� ����
    public void clearLine(){
        int bonus = 0;
        boolean isFull; // �� ���� ���� ���ִ��� ����.

        for(int i=17; i>-1; i--){
            isFull = true;

            // ��ĭ�̶� ��������� false �ݺ��� Ż��
            for(int j=0; j<10; j++){
                if (MainBoard[j][i] == 0) {
                    isFull = false;
                    break;
                }
            }

            // ���� i��° ���� ���� �� ���.
            if(isFull) {
                bonus++;
                confirmDraw =false;
                // i��° �� ����
                for(int j=0; j<10; j++) {
                    MainBoard[j][i] = 0;
                }
                // i��° �� ��ĭ�� ��� ���� ��ĭ ������.
                for(int j=i; j > 0; j--){
                    for(int k=0; k<10; k++){
                        MainBoard[k][j] = MainBoard[k][j-1];
                    }
                }
                // ���� �� �� ����
                for(int j=0; j<10; j++){
                    MainBoard[j][0] = 0;
                }

                // ���̵��� ���� ���� ����
                if(difficulty == "Easy"){
                    score += 10;
                }else if(difficulty == "Normal"){
                    score += 10 *1.6;
                }else{
                    score += 10 * 2.5;
                }
                i++;
            }
        }
        if(bonus > 1)
            score += (bonus-1) * 10;
        scoreTf.setText(score + "");

    }

    public class TimeCounterThread implements Runnable{
        public void run() {
            while (time <= 90) {        //10�ܰ� ���� ������ ������ ����
                try {
                    if(restart) {   // ���� ����� �� ������ ����.
                        restart = false;
                        break;
                    }
                    Thread.sleep(1000);
                    time++;
//                    System.out.println(time);
                    if (time % 10 == 0) {
                        level++;
                        levelTf.setText(level + "");
                        setDelay();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
