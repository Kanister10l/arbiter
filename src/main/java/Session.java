import Exceptions.MoveNotValidException;
import Exceptions.ProcessTimedOutException;
import Exceptions.UnknownException;
import Exceptions.WrongProcessResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Random;

/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class Session implements Runnable, CustomEvent{
    private Runtime rt;
    private Process processA;
    private Process processB;
    private String playerNameA;
    private String playerNameB;
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
    private int lastProgram;
    private int winner;
    private Block block;
    private GameDump dump;
    private ObjectMapper mapper;
    private BufferedWriter errorWriter;
    private int[][] begining;

    public Session(String playerNameA, String playerNameB,String startScriptA, String startScriptB, int gridSize, double blackSpotChance){
        rt = Runtime.getRuntime();
        this.playerNameA = playerNameA;
        this.playerNameB = playerNameB;
        this.startScriptA = startScriptA;
        this.startScriptB = startScriptB;
        grid = new Grid(gridSize);
        this.gridSize = gridSize;
        configMsg = "";
        random = new Random();
        this.blackSpotChance = blackSpotChance;
        winner = 0;
        dump = new GameDump(playerNameA, playerNameB, gridSize);
        mapper = new ObjectMapper();
        begining = new int[gridSize][gridSize];
    }

    public void run() {
        try {
            prepareGrid();
            processA = rt.exec(startScriptA);
            processB = rt.exec(startScriptB);

            inputA = new BufferedReader(new InputStreamReader(processA.getInputStream()));
            inputB = new BufferedReader(new InputStreamReader(processB.getInputStream()));
            outputA = new BufferedWriter(new OutputStreamWriter(processA.getOutputStream()));
            outputB = new BufferedWriter(new OutputStreamWriter(processB.getOutputStream()));

            for (int i = 0; i < grid.getGrid().length; i++) {
                System.arraycopy(grid.getGrid()[i], 0, begining[i], 0, grid.getGrid().length);
            }
            dump.setBegining(begining);

            timer = new Timer(this, 1000);
            timerTh = new Thread(timer);
            timerTh.start();

            sendMsg(outputA, configMsg);

            listenToInitMsg(inputA, 2);
            timer.pause();
            lastProgram = 2;

            sendMsg(outputB, configMsg);

            listenToInitMsg(inputB, 3);
            timer.pause();
            lastProgram = 3;

            timerTh.interrupt();
            timer = new Timer(this, 500);
            timerTh = new Thread(timer);
            timerTh.start();

            sendMsg(outputA, "start");
            pointMessage = waitForResponse(inputA, 2);
            block = parseMsg(pointMessage, 2);
            grid.setGridBlock(block, 2);
            dump.addMove(new PlayerMove(block, 2));
            lastProgram = 2;

            sendMsg(outputB, pointMessage);
            pointMessage = waitForResponse(inputB, 3);
            block = parseMsg(pointMessage, 3);
            grid.setGridBlock(block, 3);
            dump.addMove(new PlayerMove(block, 3));
            lastProgram = 3;

            while(grid.getFree() > 0){
                nextTick(inputA, outputA, 2);
                if (grid.getFree() > 0){
                    nextTick(inputB, outputB, 3);
                }
            }
            setWinner(lastProgram);
            dump.setEnd(grid.getGrid());


            endWork();
            mapper.writeValue(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(System.currentTimeMillis()) + ".json"), dump);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProcessTimedOutException e) {
            try {
                long timeStamp = System.currentTimeMillis();
                errorWriter = new BufferedWriter(new FileWriter(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + " Error.log")));
                if (e.getMessage().equals("2"))
                    errorWriter.write("Program " + playerNameA + " failed to respond in a given time. Aborting!!! Don't be sad, have a hug <3");
                else
                    errorWriter.write("Program " + playerNameB + " failed to respond in a given time. Aborting!!! Don't be sad, have a hug <3");
                mapper.writeValue(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + ".json"), dump);
                errorWriter.close();
                endWork();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (WrongProcessResponseException e) {
            try {
                long timeStamp = System.currentTimeMillis();
                errorWriter = new BufferedWriter(new FileWriter(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + " Error.log")));
                if (e.getMessage().equals("2"))
                    errorWriter.write("Program " + playerNameA + " responded with an unexpected message. Aborting!!! Don't be sad, have a hug <3");
                else
                    errorWriter.write("Program " + playerNameB + " responded with an unexpected message. Aborting!!! Don't be sad, have a hug <3");
                mapper.writeValue(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + ".json"), dump);
                errorWriter.close();
                endWork();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (UnknownException e) {
            try {
                long timeStamp = System.currentTimeMillis();
                errorWriter = new BufferedWriter(new FileWriter(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + " Error.log")));
                if (e.getMessage().equals("2"))
                    errorWriter.write("Program " + playerNameA + " failed with an unknown error. Aborting!!! Don't be sad, have a hug <3");
                else
                    errorWriter.write("Program " + playerNameB + " failed with an unknown error. Aborting!!! Don't be sad, have a hug <3");
                mapper.writeValue(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + ".json"), dump);
                errorWriter.close();
                endWork();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (MoveNotValidException e) {
            try {
                long timeStamp = System.currentTimeMillis();
                errorWriter = new BufferedWriter(new FileWriter(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + " Error.log")));
                if (e.getMessage().equals("2"))
                    errorWriter.write("Program " + playerNameA + " made non-valid move. Aborting!!! Don't be sad, have a hug <3");
                else
                    errorWriter.write("Program " + playerNameB + " made non-valid move. Aborting!!! Don't be sad, have a hug <3");
                mapper.writeValue(new File("./Results/" + playerNameA + " vs " + playerNameB + " " + String.valueOf(timeStamp) + ".json"), dump);
                errorWriter.close();
                endWork();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void event(int errorCode) {
        this.errorCode = errorCode;
    }

    private void prepareGrid(){
        StringBuilder output = new StringBuilder();
        StringBuilder segment = new StringBuilder();
        output.append(gridSize);
        if (blackSpotChance != 0) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (random.nextDouble() < blackSpotChance){
                        grid.setGridPoint(i, j, 1);
                        segment.setLength(0);
                        segment.append("_");
                        segment.append(i);
                        segment.append("x");
                        segment.append(j);
                        output.append(segment.toString());
                    }
                }
            }
        }
        configMsg = output.toString();
        System.out.println(configMsg);
    }

    private void listenToInitMsg(BufferedReader input, int program) throws IOException, InterruptedException, ProcessTimedOutException, WrongProcessResponseException, UnknownException {
        while(true){
            if (errorCode != 0) {
                if (errorCode == 504)
                    throw new ProcessTimedOutException(String.valueOf(program));
                else
                    throw new UnknownException(String.valueOf(program));
            }
            else if (input.ready()){
                if (input.readLine().toLowerCase().equals("ok"))
                    break;
                else{
                    errorCode = 501;
                    throw new WrongProcessResponseException(String.valueOf(program));
                }
            }
            Thread.sleep(30);
        }
    }

    private void sendMsg(BufferedWriter output, String msg) throws IOException {
        output.write(msg);
        output.newLine();
        output.flush();
        timer.reset();
        timer.resume();
    }

    private String waitForResponse(BufferedReader input, int program) throws IOException, InterruptedException, ProcessTimedOutException, UnknownException {
        while(true){
            if (errorCode != 0) {
                if (errorCode == 504)
                    throw new ProcessTimedOutException(String.valueOf(program));
                else
                    throw new UnknownException(String.valueOf(program));
            }
            else if (input.ready()){
                timer.pause();
                return input.readLine().toLowerCase();
            }
            Thread.sleep(30);
        }
    }

    private Block parseMsg(String ms, int program) throws WrongProcessResponseException {
        String msg = ms.toLowerCase();
        String[] firstSplit = msg.split("_");
        if (firstSplit.length == 2){
            String[] firstPoint = firstSplit[0].split("x");
            String[] secondPoint = firstSplit[1].split("x");
            return new Block(Integer.parseInt(firstPoint[0]), Integer.parseInt(firstPoint[1]), Integer.parseInt(secondPoint[0]), Integer.parseInt(secondPoint[1]));
        }
        else{
            errorCode = 501;
            throw new WrongProcessResponseException(String.valueOf(program));
        }
    }

    private void nextTick(BufferedReader input, BufferedWriter output, int program) throws IOException, InterruptedException, ProcessTimedOutException, UnknownException, WrongProcessResponseException, MoveNotValidException {
        sendMsg(output, pointMessage);
        pointMessage = waitForResponse(input, program);
        block = parseMsg(pointMessage, program);
        grid.setGridBlock(block, program);
        dump.addMove(new PlayerMove(block, program));
        lastProgram = program;
    }

    private void setWinner(int winner){
        this.winner = winner;
        dump.setWinner(winner);
    }

    private void endWork(){
        timerTh.interrupt();
        processA.destroy();
        processB.destroy();
    }
}
