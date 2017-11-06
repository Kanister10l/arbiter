/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class Grid {
    private byte[][] grid;

    public Grid(int size){
        grid = new byte[size][size];
    }

    public void setGridPoint(int x, int y, byte state){
        getGrid()[x][y] = state;
    }

    public byte[][] getGrid() {
        return grid;
    }
}
