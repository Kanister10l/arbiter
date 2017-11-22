package Exceptions;

/**
 * Created on 22.11.2017 by Kamil Samul for usage in arbiter.
 */
public class WrongProcessResponseException extends Exception {
    public WrongProcessResponseException(String message){
        super(message);
    }
}
