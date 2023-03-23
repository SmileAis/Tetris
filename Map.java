public class Map {
    static int[][] classic = new int[10][18];
    static int[][] diagonal = new int [10][18];
    static int[][] stairs = new int[10][18];
    static int[][] cliff = new int[10][18];

    public Map(){
        setClassic();
        setDiagonal();
        setStairs();
        setCliff();
    }

    public void initMap(int[][] a){
        for(int i=0; i<10; i++){
            for(int j=0; j<18; j++){
                a[i][j] = 0;
            }
        }
    }

    public void setClassic(){
        initMap(classic);
    }
    public void setDiagonal(){
        initMap(diagonal);
        int blank = 9;
        boolean turn = false;

        for(int i=17; i>7; i--)
            for(int j=0; j<10; j++)
                diagonal[j][i] = 1;

        for(int i=17; i>7; i--) {
            for (int j = 0; j < 10; j++) {
                if (blank == j) {
                    diagonal[blank][i] = 0;
                    blank--;
                }
            }
        }
    }
    public void setStairs(){
        initMap(stairs);
        int blank = 9;
        for(int i=17; i > 7; i--) {
            for (int j = 0; j < 10; j++) {
                if (j < blank)
                    stairs[j][i] = 1;
                else
                    stairs[j][i] = 0;
            }
            blank--;
        }
    }
    public void setCliff(){
        initMap(cliff);
        for(int i=17; i>9; i--){
            for(int j=0; j<9; j++){
                cliff[j][i] = 1;
            }
        }
    }

}
