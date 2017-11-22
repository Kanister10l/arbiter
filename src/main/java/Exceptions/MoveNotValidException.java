package Exceptions;

/**
 * Created on 22.11.2017 by Kamil Samul for usage in arbiter.
 */
public class MoveNotValidException extends Exception{
    public MoveNotValidException(String message){
        super(message);
    }
}
