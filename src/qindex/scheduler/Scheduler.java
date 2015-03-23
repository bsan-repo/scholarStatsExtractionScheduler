/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qindex.scheduler;

import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.JSONObject;
import qhindex.controller.SearchAuthorWorksController;
import qhindex.dataobj.Author;
import qhindex.servercomm.ServerDataCache;
import qhindex.util.Debug;

/**
 *
 * @author 6opC4C3
 */
public class Scheduler{
    
    protected class AuthorToProcess{
        public String authorUrl;
        public long delay;
    }
    private long delay = 0;
    
    protected class ExecuteScheduledWorksTask extends TimerTask{
        
        private AuthorToProcess currentAuthorToProcess = new AuthorToProcess(); 
        private ServerDataCache serverDataCache = new ServerDataCache();
        private SearchAuthorWorksController searchAuthorWorksController = new SearchAuthorWorksController();
        private final String getAuthorToProcessFromServerUrl = Debug.getCacheServerURL()+"/serversch/index.php/client/getAuthorToProcessFromServer";
        
        public boolean retrieveAuthorToProcessFromServer(AuthorToProcess authorToProcess){
            JSONObject data = new JSONObject();
            data.put("username","client_app_user");
            data.put("password","xPK81Kan1ejtN71Z4mKw");

            JSONObject results = serverDataCache.sendRequest(data, getAuthorToProcessFromServerUrl, false);
            String resultsMsg = serverDataCache.getResultsMsg();

            if(results.containsKey("result") && ((String)results.get("result")).compareTo("ok") == 0){
                try{
                    if(results.containsKey("authorUrl")&&results.containsKey("delay")){
                        authorToProcess.authorUrl = (String)results.get("authorUrl");
                        try{
                            authorToProcess.delay = Long.parseLong((String)results.get("delay"));
                        }catch(NumberFormatException ex){
                            authorToProcess.delay = 1000*60*60*24;// Delay is in millisecs - Set to default 24 hours
                        }
                    }else{
                        return false;
                    }
                }catch(ClassCastException ccEx){
                    Debug.print("Exception while retrieving author to process from server: "+ccEx.toString()+"  Additional info: "+resultsMsg);
                }
            }else{
                return false;
            }

            return true;
        }
        
        @Override
        public void run(){
            if(retrieveAuthorToProcessFromServer(currentAuthorToProcess)){
                delay = currentAuthorToProcess.delay;
                Author author = new Author();
                author.setUrl(currentAuthorToProcess.authorUrl);
                searchAuthorWorks(author);
            }else{
                // wait half a day to query the server for any unprocessed author
                delay = 1000*60*60*12;
            }
            relaunchTimer();
        }
        
        private void searchAuthorWorks(Author authorToSearch){
            // Set the author to search and restarting the controller 
            // ensuring if something is executing is cancelled and started again
            searchAuthorWorksController.setAuthorToSearch(authorToSearch);
            searchAuthorWorksController.execute(); 
        }
    }
    
    private void relaunchTimer(){
        // TODO Compile all received messages and after certain number send to 
        // admin mail as debugging information
        
        new Timer().schedule(new ExecuteScheduledWorksTask(), delay);
    }
    
    public Scheduler(){
    }

    public void start(){
        // Start processing first author immediately
        long delay = 0;
        new Timer().schedule(new ExecuteScheduledWorksTask(), delay);
    }
}
