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
    private String pointMessage;
    private String lastProgram;
    private String winner;

    public Session(String startScriptA, String startScriptB, int gridSize, double blackSpotChance){
        rt = Runtime.getRuntime();
        this.startScriptA = startScriptA;
        this.startScriptB = startScriptB;
        grid = new Grid(gridSize);
        this.gridSize = gridSize;
        configMsg = "";
        random = new Random();
        this.blackSpotChance = blackSpotChance;
        winner = null;
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
            timerTh.start();

            sendMsg(outputA, configMsg);

            listenToInitMsg(inputA, "A");
            timer.pause();
            lastProgram = "A";

            sendMsg(outputB, configMsg);

            listenToInitMsg(inputB, "B");
            timer.pause();
            lastProgram = "B";

            timerTh.interrupt();
            timer = new Timer(this, 500);
            timerTh = new Thread(timer);
            timerTh.start();

            sendMsg(outputA, "start");
            pointMessage = waitForResponse(inputA, "A");
            parseMsg(pointMessage, "A");
            lastProgram = "A";
            sendMsg(outputA, pointMessage);
            pointMessage = waitForResponse(inputA, "B");
            parseMsg(pointMessage, "B");
            lastProgram = "B";

            while(grid.getFree() > 0){
                nextTick(inputA, outputA, "A");
                if (grid.getFree() > 0){
                    nextTick(inputB, outputB, "B");
                }
            }
            setWinner(lastProgram);


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

    private void sendMsg(BufferedWriter output, String msg) throws IOException {
        output.write(msg);
        output.newLine();
        timer.reset();
        timer.resume();
    }

    private String waitForResponse(BufferedReader input, String program) throws IOException, InterruptedException {
        while(true){
            if (errorCode != 0) {
                System.out.println("Error in program " + program + ": " + errorCode); //TODO: Handle errors properly
                break;
            }
            else if (input.ready()){
                timer.pause();
                return input.readLine().toLowerCase();
            }
            Thread.sleep(30);
        }
        return "Error";
    }

    private Block parseMsg(String msg, String program){
        String[] firstSplit = msg.split("_");
        if (firstSplit.length == 2){
            String[] firstPoint = firstSplit[0].split("x");
            String[] secondPoint = firstSplit[1].split("x");
            return new Block(Integer.parseInt(firstPoint[0]), Integer.parseInt(firstPoint[1]), Integer.parseInt(secondPoint[0]), Integer.parseInt(secondPoint[0]));
        }
        else{
            errorCode = 487;
            System.out.println("Error in program " + program + ": " + errorCode); //TODO: Handle errors properly
        }
        return null;
    }

    private void nextTick(BufferedReader input, BufferedWriter output, String program) throws IOException, InterruptedException {
        sendMsg(output, pointMessage);
        pointMessage = waitForResponse(input, program);
        parseMsg(pointMessage, program);
        lastProgram = program;
    }

    private void setWinner(String winner){
        this.winner = winner;
    }

    public String getWinner(){
        return winner;
    }
}
