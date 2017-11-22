import java.util.ArrayList;

/**
 * Created on 22.11.2017 by Kamil Samul for usage in arbiter.
 */
public class GameDump {
    private int gridSize;
    private String player1;
    private String player2;
    private int[][] begining;
    private ArrayList<PlayerMove> moves;
    private int[][] end;
    private int winner;

    public GameDump(String player1, String player2, int gridSize){
        this.setPlayer1(player1);
        this.setPlayer2(player2);
        this.setGridSize(gridSize);
        setMoves(new ArrayList<PlayerMove>(50));
        winner = 0;
    }

    public int[][] getBegining() {
        return begining;
    }

    public void setBegining(int[][] begining) {
        this.begining = begining;
    }

    public ArrayList<PlayerMove> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<PlayerMove> moves) {
        this.moves = moves;
    }

    public int[][] getEnd() {
        return end;
    }

    public void setEnd(int[][] end) {
        this.end = end;
    }

    public void addMove(PlayerMove move){
        moves.add(move);
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}
