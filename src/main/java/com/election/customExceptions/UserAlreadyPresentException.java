package com.election.customExceptions;

public class UserAlreadyPresentException extends Exception {
    public UserAlreadyPresentException(String msg){
        super(msg);
    }
}
