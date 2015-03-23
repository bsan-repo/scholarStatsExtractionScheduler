/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.controller;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import qhindex.dataobj.Author;
import qhindex.dataobj.AuthorWork;
import qhindex.json.AuthorJsonConverter;
import qhindex.json.AuthorWorkJsonConverter;
import qhindex.servercomm.ServerDataCache;
import qhindex.util.AppException;
import qhindex.util.Debug;

/**
 *
 * @author Boris Sanchez
 */
public class ServerDataCacheController {
    private ServerDataCache serverDataCache = new ServerDataCache();
    private AuthorJsonConverter authorJsonConverter = new AuthorJsonConverter();
    private AuthorWorkJsonConverter authorWorkJsonConverter = new AuthorWorkJsonConverter();
    private String resultsMsg = new String();
    
    // local host
    //private final String cacheAuthorWorksFromServerUrl = "http://localhost:8888/serversch/public/index.php/client/authorCache";
    //private final String saveAuthorWorksToServerurl = "http://localhost:8888/serversch/public/index.php/client/authorSave";
    // aws 
    private final String cacheAuthorWorksFromServerUrl = Debug.getCacheServerURL()+"/serversch/index.php/client/authorCache";
    private final String saveAuthorWorksToServerurl = Debug.getCacheServerURL()+"/serversch/index.php/client/authorSave";
    
    public boolean cacheAuthorWorksFromServer(String authorUrl, Author author, ArrayList<AuthorWork> authorWorks) throws AppException{
        // Set results to null
        authorWorks.clear();
        
        JSONObject data = new JSONObject();
        data.put("authorUrl",authorUrl);
        data.put("username","client_app_user");
        data.put("password","xPK81Kan1ejtN71Z4mKw");
        
        JSONObject results = serverDataCache.sendRequest(data, cacheAuthorWorksFromServerUrl, false);
        resultsMsg += serverDataCache.getResultsMsg();
        
        if(results.size() > 0 && results.containsKey("result") && results.get("result").toString().contains("ok")){
            try{
            // Ensure it is a valid array of author works
            if(results.containsKey("authorWorks")){
                // Extract author data
                if(results.containsKey("authorSelected")){
                    String authorStr = (String)results.get("authorSelected");
                    JSONObject authorJsonObj = null;
                    try{
                        authorJsonObj = (JSONObject)new JSONParser().parse(authorStr);
                    }catch(ParseException pEx){
                        throw new AppException("Could not parse response.", pEx);
                    }
                    if(authorJsonObj!= null){
                        AuthorJsonConverter authorJsonConverter = new AuthorJsonConverter();
                        Author authorFromJson = authorJsonConverter.jsonToAuthor(authorJsonObj);
                        author.setName(authorFromJson.getName());
                        author.setAffiliation(authorFromJson.getAffiliation());
                        author.setUrl(authorFromJson.getUrl());
                    }
                }
                
                // Extract author works and citing works
                String authorWorksStr = (String)results.get("authorWorks");
                
                JSONArray authorWorksJsonArray = null;
                try{
                    authorWorksJsonArray = (JSONArray)new JSONParser().parse(authorWorksStr);
                }catch(ParseException pEx){
                    throw new AppException("Could not parse response.", pEx);
                }
                if(authorWorksJsonArray!= null && authorWorksJsonArray.size() > 0){
                    for(int i = 0; i < authorWorksJsonArray.size(); i++){
                        JSONObject authorWorkJsonObj = (JSONObject)authorWorksJsonArray.get(i);
                        AuthorWork authorWork = authorWorkJsonConverter.jsonToAuthorWork(authorWorkJsonObj);
                        authorWorks.add(authorWork);
                    }
                }
            }else{
                return false;
            }
            }catch(ClassCastException ccEx){
                throw new AppException("Wrong formatted data recived.", ccEx);
            }
        }else{
            return false;
        }
        
        return true;
    }
    
    public boolean saveAuthorWorksToServer(Author author, ArrayList<AuthorWork> authorWorks, String msg) throws AppException{
        
        JSONArray authorWorksJsonArray = new JSONArray();
        AuthorWorkJsonConverter authorWorkJson = new AuthorWorkJsonConverter();
        for(int i = 0; i < authorWorks.size(); i++){
            AuthorWork authorWork = authorWorks.get(i);
            JSONObject citingWorkJsonObj = authorWorkJson.authorWorkToJson(authorWork);
            authorWorksJsonArray.add(citingWorkJsonObj);
        }
        
        JSONObject data = new JSONObject();
        data.put("username","client_app_user");
        data.put("password","xPK81Kan1ejtN71Z4mKw");
        data.put("author", authorJsonConverter.AuthorToJson(author));
        data.put("authorWorks", authorWorksJsonArray);
        
        JSONObject results = serverDataCache.sendRequest(data, saveAuthorWorksToServerurl, true);
        resultsMsg += serverDataCache.getResultsMsg();
        
        if(results.containsKey("result")){
            try{
                String resultStr = (String)results.get("result");

                if(results.containsKey("msg")){
                    msg = (String)results.get("msg");

                    if(resultStr == "error"){
                        return false;
                    }
                }else{
                    return false;
                }
            }catch(ClassCastException ccEx){
                throw new AppException("Wrong formatted data recived.", ccEx);
            }
        }else{
            return false;
        }
        
        return true;
    }

    public String getResultsMsg() {
        return resultsMsg;
    }
    
    public void resetResultsMsg(){
        serverDataCache.resetResultsMsg();
        resultsMsg = "";
    }
}
