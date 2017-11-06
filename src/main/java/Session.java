import java.io.*;
import java.util.Random;

/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class Session implements Runnable, CustomEvent{
    private Runtime rt;
    private Process processA;
    private Process processB;
    private String startScriptA;
    private String startScriptB;
    private BufferedReader inputA;
    private BufferedReader inputB;
    private BufferedWriter outputA;
    private BufferedWriter outputB;
    private int errorCode;
    private Grid grid;
    private int gridSize;
    private String configMsg;
    private Random random;
    private double blackSpotChance;
    private Timer timer;
    private Thread timerTh;

    public Session(String startScriptA, String startScriptB, int gridSize, double blackSpotChance){
        rt = Runtime.getRuntime();
        this.startScriptA = startScriptA;
        this.startScriptB = startScriptB;
        grid = new Grid(gridSize);
        this.gridSize = gridSize;
        configMsg = "";
        random = new Random();
        this.blackSpotChance = blackSpotChance;
    }

    public void run() {
        try {
            processA = rt.exec(startScriptA);
            processB = rt.exec(startScriptB);

            inputA = new BufferedReader(new InputStreamReader(processA.getInputStream()));
            inputB = new BufferedReader(new InputStreamReader(processB.getInputStream()));
            outputA = new BufferedWriter(new OutputStreamWriter(processA.getOutputStream()));
            outputB = new BufferedWriter(new OutputStreamWriter(processB.getOutputStream()));

            prepareGrid();

            timer = new Timer(this, 1000);
            timerTh = new Thread(timer);

            outputA.write(configMsg);
            outputA.newLine();

            timer.resume();
            timerTh.start();

            listenToInitMsg(inputA, "A");

            timer.pause();

            outputB.write(configMsg);
            outputB.newLine();

            timer.reset();
            timer.resume();

            listenToInitMsg(inputB, "B");

            timerTh.interrupt();
            processA.destroy();
            processB.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void event(int errorCode) {
        this.errorCode = errorCode;
    }

    private void prepareGrid(){
        configMsg += gridSize;
        if (blackSpotChance != 0) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (random.nextDouble() < blackSpotChance){
                        grid.setGridPoint(i, j, (byte) 1);
                        configMsg += "_" + i + j;
                    }
                }
            }
        }
    }

    private void listenToInitMsg(BufferedReader input, String program) throws IOException, InterruptedException {
        while(true){
            if (errorCode != 0) {
                System.out.println("Error in program " + program + ": " + errorCode); //TODO: Handle errors properly
                break;
            }
            else if (input.ready()){
                if (input.readLine().toLowerCase().equals("ok"))
                    break;
                else{
                    errorCode = 501;
                    System.out.println("Error in program " + program + ": " + errorCode); //TODO: Handle errors properly
                    break;
                }
            }
            Thread.sleep(30);
        }
    }
}
