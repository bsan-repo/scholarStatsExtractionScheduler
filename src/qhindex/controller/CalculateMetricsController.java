/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.controller;

import java.util.ArrayList;
import java.util.Hashtable;
import qhindex.dataobj.AuthorWork;
import qhindex.dataobj.CitingWork;
import qhindex.metrics.MetricsSolver;
import qhindex.util.Debug;

/**
 *
 * @author Boris Sanchez
 */
public class CalculateMetricsController{
    private Hashtable<String, String> metrics = null;
    private ArrayList<AuthorWork> worksFound = null;
    private String resultsMsg = new String();
        
    protected void calculateMetrics(){
        MetricsSolver metricsSolver = new MetricsSolver();
        // Calculate the qh-index
        try{
            int qh_index = metricsSolver.calculateQualityIndex(worksFound);
            metrics.put("q-index", qh_index+"");

            int h_index = metricsSolver.calculateHIndex(worksFound);
            metrics.put("h-index", h_index+"");

            int qualityCitations = 0;
            int qualityPublications = 0;
            int publications = metricsSolver.calculatePublications(worksFound);
            int citations = metricsSolver.calculateCitations(worksFound);
            qualityCitations = metricsSolver.calculateQualityCitations(worksFound);
            qualityPublications = metricsSolver.calculateQualityPublications(worksFound);
            metrics.put("citations", citations+"");

            String qualityCitationsPercentageStr = String.format("%.1f", ((float)qualityCitations/citations)*100);
            String qualityPublicationsPercentageStr = String.format("%.1f", ((float)qualityPublications/publications)*100);
            metrics.put("qualityCitations", qualityCitations+" - "+qualityCitationsPercentageStr+"%");
            metrics.put("qualityPublications", qualityPublications+" - "+qualityPublicationsPercentageStr+"%");

            metrics.put("publications", publications+"");

            calculateDetailedEraStats(citations, publications);
        }catch(Exception ex){
            Debug.print("Exception while calculating metrics: "+ex.toString());
            resultsMsg += "Exception while calculating metrics.\n";
        }
    }

    protected void calculateDetailedEraStats(int totalCitations, int totalPublications){
        int publicationsRankAPlus = 0;
        int publicationsRankA = 0;
        int publicationsRankB = 0;
        int publicationsRankC = 0;
        int citationsRankAPlus = 0;
        int citationsRankA = 0;
        int citationsRankB = 0;
        int citationsRankC = 0;

        for(int i = 0; i<worksFound.size(); i++){
            AuthorWork authorWork = worksFound.get(i);
            if(authorWork.getRankPublisher().equals("A*")){
                publicationsRankAPlus++;
            }else if(authorWork.getRankPublisher().equals("A")){
                publicationsRankA++;
            }else if(authorWork.getRankPublisher().equals("B")){
                publicationsRankB++;
            }else if(authorWork.getRankPublisher().equals("C")){
                publicationsRankC++;
            }
            ArrayList<CitingWork> citingWorks = authorWork.getCitingWorks();
            for(int j = 0; j < citingWorks.size(); j++){
                CitingWork citingWork = citingWorks.get(j);
                if(citingWork.getRankPublisher().equals("A*")){
                    citationsRankAPlus++;
                }else if(citingWork.getRankPublisher().equals("A")){
                    citationsRankA++;
                }else if(citingWork.getRankPublisher().equals("B")){
                    citationsRankB++;
                }else if(citingWork.getRankPublisher().equals("C")){
                    citationsRankC++;
                }
            }
        }
        String publicationsRankAPlusPercentageStr = String.format("%.1f", ((float)publicationsRankAPlus/totalPublications)*100);
        String publicationsRankAPercentageStr = String.format("%.1f", ((float)publicationsRankA/totalPublications)*100);
        String publicationsRankBPercentageStr = String.format("%.1f", ((float)publicationsRankB/totalPublications)*100);
        String publicationsRankCPercentageStr = String.format("%.1f", ((float)publicationsRankC/totalPublications)*100);

        String citationsRankAPlusPercentageStr = String.format("%.1f", ((float)citationsRankAPlus/totalCitations)*100);
        String citationsRankAPercentageStr = String.format("%.1f", ((float)citationsRankA/totalCitations)*100);
        String citationsRankBPercentageStr = String.format("%.1f", ((float)citationsRankC/totalCitations)*100);
        String citationsRankCPercentageStr = String.format("%.1f", ((float)citationsRankC/totalCitations)*100);

        metrics.put("publicationsRankAPlus", publicationsRankAPlus+" -  "+publicationsRankAPlusPercentageStr+"%");
        metrics.put("publicationsRankA", publicationsRankA+" -  "+publicationsRankAPercentageStr+"%");
        metrics.put("publicationsRankB", publicationsRankB+" -  "+publicationsRankBPercentageStr+"%");
        metrics.put("publicationsRankC", publicationsRankC+" -  "+publicationsRankCPercentageStr+"%");

        metrics.put("citationsRankAPlus", citationsRankAPlus+" -  "+citationsRankAPlusPercentageStr+"%");
        metrics.put("citationsRankA", citationsRankA+" -  "+citationsRankAPercentageStr+"%");
        metrics.put("citationsRankB", citationsRankB+" -  "+citationsRankBPercentageStr+"%");
        metrics.put("citationsRankC", citationsRankC+" -  "+citationsRankCPercentageStr+"%");
    }

    public void execute(){
        resultsMsg = "";
        if(worksFound != null && metrics != null){
            String msgResults = new String();
            calculateMetrics();
        }else{
            resultsMsg += "Error accessing the data containers while calculating author metrics.\n";
        }
    }

    public Hashtable<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(Hashtable<String, String> metrics) {
        this.metrics = metrics;
    }

    public ArrayList<AuthorWork> getWorksFound() {
        return worksFound;
    }

    public void setWorksFound(ArrayList<AuthorWork> worksFound) {
        this.worksFound = worksFound;
    }

    public String getResultsMsg() {
        return resultsMsg;
    }
    
    public void resetResultsMsg(){
        resultsMsg = "";
    }
}
