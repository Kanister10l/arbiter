import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class main {
    private static Random random = new Random();

    public static void main(String[] args){
        boolean alive;
        ArrayList<Thread> threads;
        BufferedReader bufferedReader;
        String[] dirs = new File("./Programs").list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });

        String[] startScripts = new String[dirs.length];

        for (int i = 0; i < dirs.length; i++) {
            try {
                bufferedReader = new BufferedReader(new FileReader("./Programs/" + dirs[i] + "/info.txt"));
                startScripts[i] = bufferedReader.readLine();
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int o = 0; o < 2; o++) {
            for (int i = 0; i < dirs.length; i++) {
                threads = new ArrayList<Thread>(dirs.length - i - 1);
                for (int j = i + 1; j < dirs.length; j++) {
                    if (args.length == 0)
                        threads.add(new Thread(new Session(dirs[i], dirs[j], startScripts[i], startScripts[j], getRandomNumberInt(), getRandomNumberDouble(), o)));
                    else
                        threads.add(new Thread(new Session(dirs[i], dirs[j], startScripts[i], startScripts[j], args[0], o)));
                    threads.get(j - i - 1).start();
                }
                do {
                    alive = false;
                    for (int j = 0; j < threads.size(); j++) {
                        if (threads.get(j).isAlive()) {
                            alive = true;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }while (alive);
            }
        }
    }

    public static int getRandomNumberInt(){
        int r = random.nextInt(96) + 3;
        while (r % 2 == 0)
            r = random.nextInt(96) + 3;
        return r;
    }

    public static double getRandomNumberDouble(){
        double r = random.nextDouble();
        while (r > 0.05)
            r = random.nextDouble();
        return r;
    }
}