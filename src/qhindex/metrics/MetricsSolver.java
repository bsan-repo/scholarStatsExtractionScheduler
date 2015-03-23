/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.metrics;

import qhindex.db.EraRecordDao;
import qhindex.dataobj.AuthorWork;
import qhindex.dataobj.CitingWork;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import qhindex.util.AppException;

/**
 *
 * @author Boris Sanchez
 */
// TODO Account for any of the values required to be a NULL, at the moment causing a NULL POinter Exception at an unknown location
public class MetricsSolver {
    private String getRankForPublisher(String publisherName) throws AppException{
        String rank = "";
        if(publisherName.length() > 0){
            EraRecordDao eraRecordDao = new EraRecordDao();
            rank = eraRecordDao.searchByName(publisherName).getRank();
            if(rank == null || rank.length() <= 0){
                rank = eraRecordDao.searchByAcronym(publisherName).getRank();
            }
        }
        return rank;
    }
    
    public void findRankForAuthorWorks(ArrayList<AuthorWork> authorWorks) throws AppException{
        int errorCount = 0;
        for(int i = 0; i < authorWorks.size(); i++){
            String publisherName = authorWorks.get(i).getPublisher();
            try{
                String rank = getRankForPublisher(publisherName);
                authorWorks.get(i).setRankPublisher(rank);
            }catch(AppException aEx){
                errorCount++;
            }
        }
        if(errorCount > 0){
            throw new AppException("Could not retrieve the rank for "+errorCount+" work entries.");
        }
    }
    
    public void findRankForCitingWorks(ArrayList<CitingWork> citingWorks) throws AppException{
        int errorCount = 0;
        for(int i = 0; i < citingWorks.size(); i++){
            String publisherName = citingWorks.get(i).getPublisher();
            try{
                String rank = getRankForPublisher(publisherName);
                citingWorks.get(i).setRankPublisher(rank);
            }catch(AppException aEx){
                errorCount++;
            }
        }
        if(errorCount > 0){
            throw new AppException("Could not retrieve the rank for "+errorCount+" citation entries.");
        }
    }
    
    public void calcualteQualityCitationsForAuthorWorks(ArrayList<AuthorWork> authorWorks){
        for(int i = 0; i < authorWorks.size(); i++){
            int qualityCitations = calculateQualityCitationsForWorks(authorWorks.get(i).getCitingWorks());
            authorWorks.get(i).setQualityCitations(qualityCitations);
        }
    }
    
    public int calculateQualityCitationsForWorks(ArrayList<CitingWork> citingWorks){
        int score = 0;
        for(int i = 0; i < citingWorks.size(); i++){
            String rank = citingWorks.get(i).getRankPublisher();
            if(rank != null && rank.length() > 0 && rank.contentEquals("Not ranked") == false){
                score += 1;
            }
        }
        return score;
    }
    
    public int calculateQualityIndex(ArrayList<AuthorWork> authorWorks){
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for(int i = 0; i < authorWorks.size(); i++){
            int score = calculateQualityCitationsForWorks(authorWorks.get(i).getCitingWorks());
            scores.add(score);
        }
        Collections.sort(scores, 
                new Comparator<Integer>(){
                    @Override
                    public int compare(Integer score1, Integer score2){
                        return score2.compareTo(score1);
                    }
                }
        );
        
        int qindex = 0;
        for(int i = 0; i < scores.size(); i++){
            if(scores.get(i) <= i){
                qindex = i;
                break;
            }
        }
        return qindex;
    }
    
    public int calculateHIndex(ArrayList<AuthorWork> authorWorks){
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for(int i = 0; i < authorWorks.size(); i++){
            int score = authorWorks.get(i).getCitingWorks().size();
            scores.add(score);
        }
        Collections.sort(scores, 
                new Comparator<Integer>(){
                    @Override
                    public int compare(Integer score1, Integer score2){
                        return score2.compareTo(score1);
                    }
                }
        );
        
        int hIndex = 0;
        for(int i = 0; i < scores.size(); i++){
            if(scores.get(i) <= i){
                hIndex = i;
                break;
            }
        }
        return hIndex;
    }
    
    public int calculateCitations(ArrayList<AuthorWork> authorWorks){
        int citations = 0;
        for(int i = 0; i < authorWorks.size(); i++){
            citations += authorWorks.get(i).getCitingWorks().size();
        }
        return citations;
    }
    
    public int calculateQualityPublications(ArrayList<AuthorWork> authorWorks){
        int qualityPublications = 0;
        for(int i = 0; i < authorWorks.size(); i++){
            if(authorWorks.get(i).getRankPublisher().length() > 0){
                qualityPublications ++;
            }
        }
        return qualityPublications;
    }
    
    public int calculateQualityCitations(ArrayList<AuthorWork> authorWorks){
        int qualityCitations = 0;
        for(int i = 0; i < authorWorks.size(); i++){
            qualityCitations += calculateQualityCitationsForWorks(authorWorks.get(i).getCitingWorks());
        }
        return qualityCitations;
    }
    
    public int calculatePublications(ArrayList<AuthorWork> authorWorks){
        return authorWorks.size();
    }
}
