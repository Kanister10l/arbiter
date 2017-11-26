import Exceptions.MoveNotValidException;

/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class Grid {
    private int[][] grid;
    private int[][] freeSpace;
    private int size;
    private int free;

    public Grid(int size){
        grid = new int[size][size];
        freeSpace = new int[size][size];
        free = 0;
        this.size = size;
        prepareFreeSpace();
    }

    public void setGridPoint(int x, int y, int state){
        grid[x][y] = state;
        if (freeSpace[x][y] > 0)
            free--;
        if (x - 1 >= 0) {
            freeSpace[x - 1][y]--;
            if (freeSpace[x - 1][y] == 0 && grid[x - 1][y] == 0)
                free--;
        }
        if (x + 1 < size) {
            freeSpace[x + 1][y]--;
            if (freeSpace[x + 1][y] == 0 && grid[x + 1][y] == 0)
                free--;
        }
        if (y - 1 >= 0) {
            freeSpace[x][y - 1]--;
            if (freeSpace[x][y - 1] == 0 && grid[x][y - 1] == 0)
                free--;
        }
        if (y + 1 < size) {
            freeSpace[x][y + 1]--;
            if (freeSpace[x][y + 1] == 0 && grid[x][y + 1] == 0)
                free--;
        }
    }

    public void setGridBlock(Block block, int player) throws MoveNotValidException {
        if(validateMove(block)){
            setGridPoint(block.getX1(), block.getY1(), player);
            setGridPoint(block.getX2(), block.getY2(), player);
            return;
        }
        throw new MoveNotValidException(String.valueOf(player));
    }

    private boolean validateMove(Block block){
        return grid[block.getX1()][block.getY1()] == 0 && grid[block.getX2()][block.getY2()] == 0;
    }

    public int[][] getGrid() {
        return grid;
    }

    private void prepareFreeSpace(){
        for (int i = 1; i < size - 1; i++) {
            freeSpace[i][0] = 3;
            freeSpace[i][size - 1] = 3;
            free += 2;
            for (int j = 1; j < size - 1; j++) {
                freeSpace[i][j] = 4;
                free++;
            }
        }
        for (int i = 1; i < size - 1; i++) {
            freeSpace[0][i] = 3;
            freeSpace[size - 1][i] = 3;
            free += 2;
        }
        freeSpace[0][0] = 2;
        freeSpace[0][size - 1] = 2;
        freeSpace[size - 1][0] = 2;
        freeSpace[size - 1][size - 1] = 2;
        free += 4;
    }

    public int getFree(){
        return free;
    }
}
