package Exceptions;

/**
 * Created on 22.11.2017 by Kamil Samul for usage in arbiter.
 */
public class ProcessTimedOutException extends Exception{

    public ProcessTimedOutException(String message){
        super(message);
    }
}
