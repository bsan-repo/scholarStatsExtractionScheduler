/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.util;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Boris Sanchez
 */
public class AppHelper {
    private static final int milliSecToWait = 250;
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";
    public static final int connectionTimeOut = 7000;
    public static void waitBeforeNewRequest(){
        // Wait a bit before doing a new request to avoid overloading the 
        // data provider server
        try{
            TimeUnit.MILLISECONDS.sleep(milliSecToWait);
        }catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
    
}
