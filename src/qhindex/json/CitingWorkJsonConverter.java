/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.json;

import org.json.simple.JSONObject;
import qhindex.dataobj.CitingWork;

/**
 *
 * @author Boris Sanchez
 */
public class CitingWorkJsonConverter {
    public JSONObject citingWorkToJson(CitingWork citigWork){
        JSONObject obj=new JSONObject();
        obj.put("name", citigWork.getName());
        obj.put("publisher", citigWork.getPublisher());
        obj.put("publisherInGoogle", citigWork.getPublisherInGoogle());
        obj.put("publisherInExternalWeb", citigWork.getPublisherInExternalWeb());
        obj.put("rankPublisher", citigWork.getRankPublisher());
        obj.put("authors", citigWork.getAuthors());
        obj.put("url", citigWork.getUrl());
        obj.put("citationsNumber", citigWork.getCitationsNumber());
        String year = citigWork.getYear();
        if(year == null ||  year.length() < 1){
            year = "0";
        }
        obj.put("year", year);
        
        return obj;
    }
    
    public CitingWork jsonToCitingWork(JSONObject obj){
        CitingWork citigWork = new CitingWork();
        citigWork.setName((String)obj.get("name"));
        citigWork.setPublisher((String)obj.get("publisher"));
        citigWork.setPublisherInGoogle((String)obj.get("publisher_in_google"));
        citigWork.setPublisherInExternalWeb((String)obj.get("publisher_in_external_web"));
        citigWork.setRankPublisher((String)obj.get("rank_publisher"));
        citigWork.setAuthors((String)obj.get("authors"));
        citigWork.setYear((String)obj.get("year"));
        if(obj.get("citationsNumber") != null){
            citigWork.setCitationsNumber((String)obj.get("citationsNumber"));
        }
        
        return citigWork;
    }
}
