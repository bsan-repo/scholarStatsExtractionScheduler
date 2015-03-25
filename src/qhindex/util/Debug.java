/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.util;

/**
 *
 * @author Boris Sanchez
 */
public class Debug {
    private static final boolean debugMode = true;
    public static void print(String msg){
        if(debugMode == true){
            System.out.println(msg);
        }
    }
    
    public static void info(String msg){
        if(debugMode == true){
            System.out.println(msg);
        }
    }
    
    public static String getCacheServerURL(){
        //if(debugMode == true){
        //    return "http://localhost:8888";
        //}else{
            return "http://ec2-52-0-126-95.compute-1.amazonaws.com";
        //}
    }
}
