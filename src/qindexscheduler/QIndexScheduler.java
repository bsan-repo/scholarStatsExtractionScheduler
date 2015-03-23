/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qindexscheduler;

import qindex.scheduler.Scheduler;

/**
 *
 * @author 6opC4C3
 */
public class QIndexScheduler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scheduler scheduler = new Scheduler();
        scheduler.start();
    }
    
}
