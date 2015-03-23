/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.controller;

import java.util.ArrayList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import qhindex.dataobj.Author;
import qhindex.util.AppHelper;
import qhindex.util.Debug;

/**
 *
 * @author Boris Sanchez
 */
public class SearchAuthorController extends Service<String>{
    
    private ArrayList<Author> authorsFound = new ArrayList<Author>();
    private String authorName = null;
    private String resultsMsg = new String();
        
    private final static String userAgentName = AppHelper.userAgent;
    
    protected class SearchAuthorTask extends Task<String>{

        protected Author extractAuthorData(Element e){
            Author a = new Author();
            a.setName(e.select("span.gs_hlt").text());
            String[] affiliationData = e.select("div.gsc_1usr_aff").text().split(",");
            // Gets the last part separated by a comma or all if not commas are present
            String affiliation = affiliationData[affiliationData.length-1].trim();
            a.setAffiliation(affiliation);
            a.setUrl(e.select("h3.gsc_1usr_name > a").get(0).attr("href"));
            return a;
        }


        // Returns zero to multiple authors found with the given name.
        protected void searchAuthorByName(String authorName, ArrayList<Author> results){
            try{
                updateProgress(0, 100);
                updateMessage("Search author - Step 1 of 5.");
                authorName.replace(" ", "+");

                Document searchAuthorDoc = Jsoup.connect("https://scholar.google.com.au/scholar?hl=en&q="+authorName)
                                .userAgent(userAgentName)
                                .get();
                AppHelper.waitBeforeNewRequest();
                
                updateProgress(50, 100);
                if(isCancelled()) return;
                
                Elements userProfilesUrl = searchAuthorDoc.select("h3.gs_rt > a");
                // Ensure the element contains the data from the user profiles
                if(userProfilesUrl.size() > 0 && userProfilesUrl.get(0).text().contains("User profiles for")){
                    String authorsProfileUrl = "https://scholar.google.com.au"+userProfilesUrl.get(0).attr("href");
                    Document authorsProfilesDoc = Jsoup.connect(authorsProfileUrl)
                                .userAgent(userAgentName)
                                .get();
                    AppHelper.waitBeforeNewRequest();
                    
                    if(isCancelled()) return;
                    
                    Elements authorElems = authorsProfilesDoc.select("div.gsc_1usr.gs_scl");
                    if(authorElems.size() > 1){
                        for(Element e : authorElems){
                            Author a = extractAuthorData(e);
                            results.add(a);
                            
                            if(isCancelled()) return;
                        }

                    }else if(authorElems.size() == 1){
                        Author a = extractAuthorData(authorElems.get(0));
                        results.add(a);
                    }else{
                        updateMessage("No authors were found.");
                    }
                    updateProgress(100, 100);
                }
            }catch(Exception ex){
                Debug.print("Exception while searching author by name: "+ex.toString());
                resultsMsg += "Exception while searching author by name.\n";
            }
        }
        
        @Override
        protected String call(){
            resultsMsg = "";
            try{
                if(authorName != null){
                    authorsFound.clear();
                    searchAuthorByName(authorName, authorsFound);
                }else{
                    resultsMsg += "Error getting the author name while searching for author.\n";
                }
            }catch(Exception ex){
                // Catch any exception in order to terminate the call and not allowing the 
                // default behaviour of continuing the thread on stand by
                Debug.print("Exception while running the search for the given author: "+ex.toString());
                resultsMsg += "Exception while running the search for the given author.\n";
            }
            return resultsMsg;
        }
    }

    protected Task createTask(){
        return new SearchAuthorTask();
    }

    public ArrayList<Author> getAuthorsFound() {
        return authorsFound;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getResultsMsg() {
        return resultsMsg;
    }
}
