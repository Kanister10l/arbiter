/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class main {

    public static void main(String[] args){
        Session session = new Session("java -jar Jarvis.jar", "java -jar Jarvis.jar", 101, 0.05);
        Thread thread = new Thread(session);
        thread.start();
    }
}
