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
    final int BLOCK_SIZE = 30;      // 블록크기
    final int LEFT_MARGIN = 30;     // 보드 왼쪽 마진
    final int TOP_MARGIN = 60;      // 보드 위쪽 마진
    Color[] blockColor = {Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, new Color(102,224,255)};   // 각 블록 색

    int[][] MainBoard = new int[10][18];    // 메인보드 0/1 표시
    int[][] MainBoardX = new int[10][18];   // 메인보드 x의 좌표값
    int[][] MainBoardY = new int[10][18];   // 메인보드 y의 좌표값

    int[] CurrentBlockX = new int[4];       // 현재 움직이는 블록 X좌표
    int[] CurrentBlockY = new int[4];       // 현재 움지이는 블록 Y좌표
    int CurrentBlockRotate = 0;         // 현재 움직이는 블록 회전 상태
    int[] tmpX = new int[4];    // 블록의 마지막X 위치
    int[] tmpY = new int[4];    // 블록의 마지막Y 위치

    int nowTypeNum;         // 현재 블록 타입
    int nextTypeNum;        // 다음 블록 타입

    int score = 0;          // 점수
    int level = 1;          // 레벨
    int delay;              // 떨어지는 속도
    static String difficulty ;  // 난이도

    boolean confirmDraw = false;

    boolean drop = false;   // drop했는지 여부
    static boolean restart = false; // 게임이 끝난 후 재시작 여부
    int time = 0;   // 레벨 설정을 위한 타이머

    JLabel diffLabel, levelLabel, scoreLabel;
    JTextField diffTf, levelTf, scoreTf;

    Object[] endOptions={"Retry", "End"};   // 종료 선택

    //화면 깜박임 을 해결하기 위한 더블 버퍼링 필요
    Image buffImage;
    Graphics bg;

    static String selectedMap;

    public Tetris() {
    	restart = false;
    	
        //보드 초기화
        createInfo();
        initBoard();
        setDelay();
        setMap();

        // 프레임 초기화
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

        // 시간 체크 스레드 생성.
        TimeCounterThread timer = new TimeCounterThread();
        Thread t = new Thread(timer);
        t.start();

        //게임 진행
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

            //게임 오버시
            if (isEnd()) {
            	restart = true;
                String info = "난이도 : " + difficulty +
                        "\n레벨   :  " + level +
                        "\n점수   :  " + score +
                        "\n맵       :  "+ selectedMap;
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


    // 다음 블럭 타입 설정
    public void nextType(){
        Random x = new Random();
        nextTypeNum = x.nextInt(7);
    }


    ///////////////////////
    //  보드 초기화
    ///////////////////////
    public void initBoard(){    // 처음 보드 상태
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
    public void createInfo(){   // 게임 정보 생성
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
    public void setDelay(){     // 떨어지는 속도 설정
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

    // 블록 정의
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
        } else if(typeNum == 2){    // └─
            CurrentBlockX[0] = 120; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 120; CurrentBlockY[1] = 30;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 3){    // ─┘
            CurrentBlockX[0] = 180; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 120; CurrentBlockY[1] = 30;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 4){    // □
            CurrentBlockX[0] = 150; CurrentBlockY[0] = 0;
            CurrentBlockX[1] = 180; CurrentBlockY[1] = 0;
            CurrentBlockX[2] = 150; CurrentBlockY[2] = 30;
            CurrentBlockX[3] = 180; CurrentBlockY[3] = 30;
        } else if(typeNum == 5){    // ┴
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
    //   보드 및 블록 그리기
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
    public void drawMainBoard(Graphics g) { //게임보드 그리기
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 18; j++) {
                g.drawRect(LEFT_MARGIN + i * BLOCK_SIZE, TOP_MARGIN + j * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        g.setColor(Color.CYAN);
        g.drawRect(LEFT_MARGIN, TOP_MARGIN, BLOCK_SIZE * 10, BLOCK_SIZE * 18);
    }
    public void drawLine(Graphics g) {  // 세로줄 그리기
        g.setColor(Color.cyan);
        g.drawLine(30 * 12, 0, 30 * 12, 700);
    }
    public void drawNextBoard(Graphics g) {     //다음 블록 보드 그리기
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                g.drawRect(30 * 13 + i * BLOCK_SIZE, 30 * 4 + j * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        g.setColor(Color.CYAN);
        g.drawRect(30 * 13, 30 * 4, BLOCK_SIZE * 4, BLOCK_SIZE * 4);
    }
    public void drawStopBlock(Graphics g) {      // 현재 멈춰있는 블록들 그리기
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


    public void drawNextBlock(Graphics g){      // 다음 블록 그리기
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
    public void drawBlock(Graphics g){  // 현재 블록 그리기
        if(!isEnd())
            for(int i=0; i<4; i++){
                g.setColor(blockColor[nowTypeNum]);
                g.fillRect(CurrentBlockX[i], CurrentBlockY[i], BLOCK_SIZE, BLOCK_SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(CurrentBlockX[i], CurrentBlockY[i], BLOCK_SIZE, BLOCK_SIZE);
            }
    }


    // 블록 떨어짐 설정
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
    // 키 입력 Event
    ///////////////////////
    @Override
    public void keyPressed(KeyEvent e) {
        if (!drop){
            // '상'키 입력 시
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
                            else{   // 왼쪽 두칸의경우
                                    CurrentBlockRotate++;
                            }
                        }
                        else {    // 블록 회전상태가 0 또는 2인경우
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
                            else{   // 오른쪽 두칸의경우
                                CurrentBlockRotate++;
                            }
                        }
                        else {    // 블록 회전상태가 0 또는 2인경우
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
                    case 4:     // □ block.
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
                            else{   // 오른쪽 두칸의경우
                                CurrentBlockRotate++;
                            }
                        }
                        else {    // 블록 회전상태가 0 또는 2인경우
                            CurrentBlockRotate++;
                        }

                        if (CurrentBlockRotate == 4)
                            CurrentBlockRotate = 0;
                        checkTurn();
                        turnBlock6();
                        break;
                    }
                }
            // '하'키 입력 시
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                System.out.println("D");
                if (!isFloor() && !checkStop()) {
                    CurrentBlockY[0] += BLOCK_SIZE;
                    CurrentBlockY[1] += BLOCK_SIZE;
                    CurrentBlockY[2] += BLOCK_SIZE;
                    CurrentBlockY[3] += BLOCK_SIZE;
                }
            }
            // '좌'키 입력 시
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                System.out.println("L");
                if (checkLeft()) {
                    for (int i = 0; i < 4; i++) {
                        CurrentBlockX[i] -= 30;
                    }
                }
            }
            // '우'키 입력 시
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                System.out.println("R");
                if (checkRight()) {
                    for (int i = 0; i < 4; i++) {
                        CurrentBlockX[i] += 30;
                    }
                }
            }
            // '스페이스바' 입력 시
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                drop = true;
                while (true) {
                    if (!isFloor() && !checkStop()) {
                        for (int i = 0; i < 4; i++) {
                            CurrentBlockY[i] += BLOCK_SIZE;
                        }
                    }
                    if (checkStop() || isFloor()) {
                        for (int i = 0; i < 4; i++) {     // 가끔 안그려지는 오류 때문에 추가
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
    // 움직임 가능 확인
    ///////////////////////
    public boolean checkLeft(){
        // 보드의 가장 왼쪽인경우
        if(CurrentBlockX[0] == LEFT_MARGIN || CurrentBlockX[1] == LEFT_MARGIN
                || CurrentBlockX[2] == LEFT_MARGIN || CurrentBlockX[3] == LEFT_MARGIN)
            return false;

        // 움직이는 블록 바로 왼쪽에 블록이 있는경우, 보드 범위에서 벗어나지 않는한.
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
        // 보드의 가장 오른쪽이면 움직임 x
        if(CurrentBlockX[0] == BLOCK_SIZE*10 || CurrentBlockX[1] == BLOCK_SIZE*10 ||
                CurrentBlockX[2] == BLOCK_SIZE*10 ||CurrentBlockX[3] == BLOCK_SIZE*10 )
            return false;

        // 움직이는 블록 오른쪽에 블록이 그려져 있는경우
        for(int i=0; i<4; i++) {
            if(CurrentBlockX[i] != LEFT_MARGIN + BLOCK_SIZE*9 && !((CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE < 0)) {
                if (MainBoard[(CurrentBlockX[i] - LEFT_MARGIN + BLOCK_SIZE) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE] == 1) {
                    return false;
                }
            }
        }
        return true;
    }public void checkTurn(){        // 회전가능한지 확인
        switch(nowTypeNum) {
            case 0:
                if(CurrentBlockRotate == 0 || CurrentBlockRotate == 2){
                    if(CurrentBlockX[0] == LEFT_MARGIN || CurrentBlockX[1] == LEFT_MARGIN){
                        for(int i=0; i<4; i++)
                            CurrentBlockX[i] += BLOCK_SIZE;
                    }
                }   // 왼쪽벽에서 턴
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
            case 4:     // □ block.
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
    // 블록 회전
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

    // 현재 블록이 바닥인지 확인
    public boolean isFloor(){
        if(CurrentBlockY[0] == TOP_MARGIN + BLOCK_SIZE * 17 || CurrentBlockY[1] == TOP_MARGIN + BLOCK_SIZE * 17 ||
               CurrentBlockY[2] == TOP_MARGIN + BLOCK_SIZE * 17 || CurrentBlockY[3] == TOP_MARGIN + BLOCK_SIZE * 17) {
            return true;
        }
        return false;
    }
    // 다음 칸이 비어있는지 확인
    public boolean checkStop(){
        for(int i=0; i<4; i++) {
            // 바닥이 아니고, 보드의 인덱스보다 넘어가지 않는경우
            if((!isFloor()) && !((CurrentBlockY[i] - TOP_MARGIN) / BLOCK_SIZE < 0))
                if (MainBoard[(CurrentBlockX[i] - LEFT_MARGIN) / BLOCK_SIZE][(CurrentBlockY[i] - TOP_MARGIN + BLOCK_SIZE) / BLOCK_SIZE ] == 1) {
                    return true;
                }
        }
        return false;
    }

    // 맨 윗줄에 블록이 닿았는지 확인
    public boolean isEnd(){
        for(int i=0; i<10; i++){
            // 가장 윗줄 중 한칸이라도 차있는 경우
            if(MainBoard[i][0] == 1) {
                return true;
            }
        }
         return false;
    }

    // 한 라인이 가득차면 줄 삭제
    public void clearLine(){
        int bonus = 0;
        boolean isFull; // 한 줄이 가득 차있는지 여부.

        for(int i=17; i>-1; i--){
            isFull = true;

            // 한칸이라도 비어있으면 false 반복문 탈출
            for(int j=0; j<10; j++){
                if (MainBoard[j][i] == 0) {
                    isFull = false;
                    break;
                }
            }

            // 현재 i번째 줄이 가득 찬 경우.
            if(isFull) {
                bonus++;
                confirmDraw =false;
                // i번째 줄 비우기
                for(int j=0; j<10; j++) {
                    MainBoard[j][i] = 0;
                }
                // i번째 줄 위칸의 모든 줄을 한칸 내리기.
                for(int j=i; j > 0; j--){
                    for(int k=0; k<10; k++){
                        MainBoard[k][j] = MainBoard[k][j-1];
                    }
                }
                // 가장 윗 줄 비우기
                for(int j=0; j<10; j++){
                    MainBoard[j][0] = 0;
                }

                // 난이도에 따라 점수 설정
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
            while (time <= 90) {        //10단계 까지 증가후 스레드 종료
                try {
                    if(restart) {   // 게임 재시작 시 스레드 종료.
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
