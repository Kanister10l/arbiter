/**
 * Created on 06.11.2017 by Kamil Samul for usage in arbiter.
 */
public class Timer implements Runnable{
    private long milis;
    private long timeout;
    private CustomEvent eventTarget;
    private boolean paused;
    private long last;
    private long now;

    public Timer(CustomEvent eventTarget, long timeout){
        this.eventTarget = eventTarget;
        this.timeout = timeout;
        milis = 0;
        paused = true;
    }

    public void run() {
        while(!Thread.interrupted()){
            if (!paused){
                now = System.currentTimeMillis();
                milis += now - last;
                last = now;
                if (milis > timeout)
                    triggerEvent();
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void reset(){
        milis = 0;
    }

    public void triggerEvent(){
        eventTarget.event(504);
    }

    public void pause(){
        paused = true;
    }

    public void resume(){
        paused = false;
        last = System.currentTimeMillis();
    }
}
