import java.io.*;
import java.util.ArrayList;

/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class main {

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
                    threads.add(new Thread(new Session(dirs[i], dirs[j], startScripts[i], startScripts[j], 5, 0.0)));
                    threads.get(j - i - 1).start();
                }
                do {
                    alive = false;
                    for (int j = 0; j < threads.size(); j++) {
                        if (threads.get(i).isAlive()) {
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
}
