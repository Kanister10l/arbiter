/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class PlayerMove {
    private Block block;
    private int playerNumber;

    public PlayerMove(Block block, int playerNumber){
        this.block = block;
        this.playerNumber = playerNumber;
    }

    public Block getBlock() {
        return block;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
}
