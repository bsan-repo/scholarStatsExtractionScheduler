package qhindex.json;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import qhindex.dataobj.AuthorWork;
import qhindex.dataobj.CitingWork;
import qhindex.util.AppException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Boris Sanchez
 */
public class AuthorWorkJsonConverter {
    public JSONObject authorWorkToJson(AuthorWork authorWork){
        JSONObject obj=new JSONObject();
        obj.put("title", authorWork.getTitle());
        obj.put("authors", authorWork.getAuthors());
        obj.put("publisher", authorWork.getPublisher());
        obj.put("publisherInGoogle", authorWork.getPublisherInGoogle());
        obj.put("citationsUrl", authorWork.getCitationsUrl());
        obj.put("rankPublisher", authorWork.getRankPublisher());
        obj.put("citations", authorWork.getCitations());
        String qCitations = authorWork.getQualityCitations()+"";
        if(qCitations == null ||  qCitations.length() < 1){
            qCitations = "0";
        }
        obj.put("qualityCitations", qCitations);
        String year = authorWork.getYear();
        if(year == null ||  year.length() < 1){
            year = "0";
        }
        obj.put("year", year);
        JSONArray citingWorksJsonArray = new JSONArray();
        CitingWorkJsonConverter citingWorkJsonConverter = new CitingWorkJsonConverter();
        for(int i = 0; i < authorWork.getCitingWorks().size(); i++){
            CitingWork citingWork = authorWork.getCitingWorks().get(i);
            JSONObject citingWorkJsonObj = citingWorkJsonConverter.citingWorkToJson(citingWork);
            citingWorksJsonArray.add(citingWorkJsonObj);
        }
        obj.put("citingWorks", citingWorksJsonArray);
        
        return obj;
    }
    
    public AuthorWork jsonToAuthorWork(JSONObject obj) throws AppException{
        AuthorWork authorWork = new AuthorWork();
        authorWork.setTitle((String)obj.get("title"));
        authorWork.setAuthors((String)obj.get("authors"));
        authorWork.setPublisher((String)obj.get("publisher"));
        authorWork.setPublisherInGoogle((String)obj.get("publisher_in_google"));
        authorWork.setQualityCitations(Integer.parseInt((String)obj.get("quality_citations")));
        authorWork.setRankPublisher((String)obj.get("rank_publisher"));
        authorWork.setCitations(Integer.parseInt((String)obj.get("citations")));
        authorWork.setCitationsUrl((String)obj.get("citations_url"));
        authorWork.setYear((String)obj.get("year"));
        ArrayList<CitingWork> citingWorks = new ArrayList<CitingWork>();
        JSONArray citingWorksJsonArray = null;
        if(obj.get("citing_works") instanceof String){
            try{
                String citingWorksStr = (String)obj.get("citing_works");
                citingWorksJsonArray = (JSONArray)new JSONParser().parse(citingWorksStr);
            }catch(ParseException pEx){
                throw new AppException("Could not parse response.", pEx);
            }
        }else if(obj.get("citing_works") instanceof JSONArray){
            citingWorksJsonArray = (JSONArray)obj.get("citing_works");
        }
        
        if(citingWorksJsonArray != null){
            CitingWorkJsonConverter citingWorkJsonConverter = new CitingWorkJsonConverter();
            for(int i = 0; i < citingWorksJsonArray.size(); i++){
                JSONObject citingWorkJsonObj = (JSONObject)citingWorksJsonArray.get(i);
                CitingWork citingWork = citingWorkJsonConverter.jsonToCitingWork(citingWorkJsonObj);
                citingWorks.add(citingWork);
            }
            authorWork.setCitingWorks(citingWorks);
        }else{
            authorWork.setCitingWorks(new ArrayList<CitingWork>());
        }
        
        return authorWork;
    }
}
