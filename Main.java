public class Main {

    public Main() {
        while(true) {
            FirstScreen.difficulty = null;
            FirstScreen.selectedMap = "Classic";
            new FirstScreen();

            while (true) {
                try {
                    Thread.sleep(100);
                    Tetris.selectedMap = FirstScreen.selectedMap;
                    Tetris.difficulty = FirstScreen.difficulty;
                    if (FirstScreen.isStart == 0) {
                        FirstScreen.isStart = 1;
                        break;
                    }
                } catch (Exception e) {
                }
            }

            new Tetris();
        }
    }

    public static void main(String[] args){
        new Main();
    }
}
