/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import qhindex.dataobj.Author;
import qhindex.dataobj.AuthorWork;
import qhindex.dataobj.CitingWork;
import qhindex.metrics.MetricsSolver;
import qhindex.util.AppException;
import qhindex.util.AppHelper;
import qhindex.util.Debug;

/**
 *
 * @author Boris Sanchez
 */
public class SearchAuthorWorksController{
    
    private ArrayList<AuthorWork> worksFound = new ArrayList<AuthorWork>();
    private Author authorToSearch = null;
    private ServerDataCacheController serverDataCacheController  = new ServerDataCacheController();
    private String resultsMsg = new String();
    
    private final static String userAgentName = AppHelper.userAgent;

    private String formatRegExSpecialCharsInString(String string){
        Pattern specialCharsRegEx = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
        String scapedString =  specialCharsRegEx.matcher(string).replaceAll("\\\\$0");
        return scapedString;
    }

    private String handlePublicationNameCases(String pubName){
        String publicationMediumStr = pubName.toUpperCase();
        // Handle unwanted words inside the publication medium name
        // Remove case procedings conference addition to conference name
        // Try first case
        if(publicationMediumStr.contains("PROCEEDINGS OF THE ")){
            publicationMediumStr = publicationMediumStr.replaceFirst("PROCEEDINGS OF THE \\d*?\\w*? (ANNUAL)?", "");
            // At least remove the known part
            if(publicationMediumStr.contains("PROCEEDINGS OF THE ")){
                publicationMediumStr = publicationMediumStr.replace("PROCEEDINGS OF THE ", "");
            }
            int startOfFixedNameIndex = pubName.toUpperCase().indexOf(publicationMediumStr);
            if(startOfFixedNameIndex >= 0){
                pubName = pubName.substring(startOfFixedNameIndex);
            }
        }
        return pubName;
    }

    private String correctAuthorWorkPublisher(String publisher){
        publisher = publisher.split(",")[0];
        Pattern endingNumbersPatter = Pattern.compile("(\\((\\d|\\s|\\+|\\-)+\\)|\\d+|\\s+|,|-)+$");
        Matcher matcher = endingNumbersPatter.matcher(publisher);
        if(matcher.find(0)){
            String corrected = publisher.substring(0, matcher.start()).trim();
            publisher = corrected;
        }
        return publisher;
    }
    
    // This method tries to obtain the publisher medium name of an author work if it cannot be 
    // extracted from the author works list. It requests the work details page in Scholar.
    // It can throw an exception while obtaining the document that is not handled in this method
    private String handlePublicationMedium(String publicationMedium, String urlAuthorWork) throws IOException{
        if(publicationMedium.contains("...")){
            Document authorWorkDoc = requestWebDocFromScholar("https://scholar.google.com.au"+urlAuthorWork, 1, false);
            AppHelper.waitBeforeNewRequest();
            // TODO verify that there are enough elements in the list before accessing the element at zero 0
            if(authorWorkDoc != null){
                Elements publicationMediumElems =  authorWorkDoc.select("div[id=gsc_table] > div.gs_scl:eq(2) > div.gsc_value");
                String publicationMediumStr = "";
                if(publicationMediumElems.size() > 0){
                    publicationMediumStr = publicationMediumElems.get(0).text();
                    publicationMedium = handlePublicationNameCases(publicationMediumStr);
                }
            }
        }
        publicationMedium = publicationMedium.trim();
        publicationMedium = correctAuthorWorkPublisher(publicationMedium);
        return publicationMedium;
    }

    // Inside called method handlePublicationMedium throws exception that is not handle in this method
    private AuthorWork extractAuthorWorkData(Element authorWorkElements) throws IOException{
        AuthorWork aw = new AuthorWork();
        Element titleElem = authorWorkElements.select("td.gsc_a_t > a").get(0);
        String name = titleElem.text();
        aw.setTitle(name);
        String urlAuthorWork = titleElem.attr("href");
        Elements workData = authorWorkElements.select("td.gsc_a_t > div");
        if(workData.size() > 1){
            String publisherInGoogle = workData.get(1).text();
            aw.setPublisherInGoogle(publisherInGoogle);
            aw.setPublisher(
                handlePublicationMedium(publisherInGoogle, urlAuthorWork)
            );

            String authors = workData.get(0).text();
            aw.setAuthors(authors);
        }
        Elements citationsData = authorWorkElements.select("td.gsc_a_c > a");
        if(citationsData.size() > 0){
            aw.setCitationsUrl(citationsData.get(0).attr("href"));
            int cititationsExtractedNumber = 0;
            try{
                String citationStr = citationsData.get(0).text();
                if(citationStr.length() > 0){
                    cititationsExtractedNumber = Integer.parseInt(citationStr);
                }
            }catch(Exception ex){
                Debug.print("Exception while extracting author work data: "+ex.toString());
                resultsMsg += "Exception while extracting author work data.\n";
            }
            aw.setCitations(cititationsExtractedNumber);
        }
        return aw;
    }

    private boolean searchCachedAuthorWorksInServer(String authorUrl, Author author, ArrayList<AuthorWork> results){
        boolean resultsFound = false;
        try{
            resultsFound = serverDataCacheController.cacheAuthorWorksFromServer(authorUrl, author, results);
            resultsMsg += serverDataCacheController.getResultsMsg();
        }catch(AppException appEx){
            Debug.print("Exception while searching author in the cache server: "+appEx.toString());
            resultsMsg += "Exception while searching author in the cache server.\n";
        }
        return resultsFound;
    }

    private boolean saveAuthorWorksToServer(Author author, ArrayList<AuthorWork> results){
        boolean isDataSaved = false;
        try{
            String msg = new String();
            isDataSaved = serverDataCacheController.saveAuthorWorksToServer(author, results, msg);
            resultsMsg += serverDataCacheController.getResultsMsg();
        }catch(AppException appEx){
            Debug.print("Exception while saving the author to the cache server: "+appEx.toString());
            resultsMsg += "Exception while saving the author to the cache server.\n";
        }
        return isDataSaved;
    }
    
    // Attempt to retrieve a web document in particular set up for google scholar 
    // which is known to denied requests after 100 request have been done.
    private Document requestWebDocFromScholar(String url, int attempts, boolean wait){
        int attemptsToRetrieveResource = attempts;
        Document doc = null;
        for(int i = 0; i < attemptsToRetrieveResource; i++){
            Debug.info("("+i+")Attempting to retrieve: "+url);
            try{
                Response response = Jsoup.connect(url)
                        .ignoreHttpErrors(true) 
                        .method(Connection.Method.GET)
                        .execute();
                int statusCode = response.statusCode();

                if(statusCode == 503){
                    Debug.info("Waiting one day to continue search after a 503 response from schoolar");
                    // Wait one day before continue search
                    if(wait == true){
                        try{
                            wait(10000*60*60*24);
                        }catch(InterruptedException iex){
                            Debug.print("Exception while waiting to continue search after a 503 response: "+iex.toString());
                            resultsMsg += "Exception while waiting to continue search after a 503 response.\n";
                        }
                    }
                }else if(statusCode == 200){
                    doc = response.parse();
                    break;
                }
            }catch(IOException ioex){
                Debug.print("Exception while retrieving scholar web page: "+ioex.toString());
                resultsMsg += "Exception while retrieving scholar web page.\n";
            }
        }
        return doc;
    }
    
    
    private Document requestWebDocFromScholar(String url){
        return requestWebDocFromScholar(url, 2, true);
    }
 
    private boolean searchWebAuthorWorks(String authorUrl, ArrayList<AuthorWork> results){
            Debug.info("Searching author works");
            int page = 0;
            boolean continueSearch = true;
            while(continueSearch){
                int maxRecordsPerPage = 100; // Max number of records allow to be retrieved at a time by google scholar
                String resultIndex = "&cstart="+(page*maxRecordsPerPage)+"&pagesize="+(page*maxRecordsPerPage+maxRecordsPerPage);

                Document authorDoc = requestWebDocFromScholar("https://scholar.google.com.au"+authorUrl+resultIndex);
                
                AppHelper.waitBeforeNewRequest();

                if(authorDoc != null){
                    Elements authorWorksElems = authorDoc.select("tr.gsc_a_tr");

                    for(Element aWorkElems : authorWorksElems){
                        try{
                        results.add(extractAuthorWorkData(aWorkElems));
                        }catch(IOException ioe){
                            Debug.print("Exception while processing author works: "+ioe.toString());
                            resultsMsg += "Exception while processing author works.\n";
                        }
                    }
                }
                page += 1;
                continueSearch = false;
            }

        return true;
    }

    public boolean searchWebAuthorData(String authorUrl, ArrayList<AuthorWork> results){
        boolean searchCompletedOk = true;

        
        searchCompletedOk = searchWebAuthorWorks(authorUrl, results);

        MetricsSolver metricsSolver = new MetricsSolver();
        String msg = "";

        int numberOfWorks = worksFound.size();
        int numberOfWorksProcessed = 0;

        Debug.info("Searching citations for "+worksFound.size()+" author works.");
        int counter = 1;
        for(AuthorWork authorwork : worksFound){
            Debug.info("Searching author "+(counter++)+" of "+worksFound.size());
            ArrayList<CitingWork> citingWorks = new ArrayList<CitingWork>();
            try{
                searchCitationsForWork(authorwork, citingWorks);
            }catch(IOException ioex){
                Debug.print("Exception while searching citations for author work: "+ioex.toString());
                resultsMsg += "Exception while searching citations for author work.\n";
            }
            // Find the rank for the publishers of the current author work's citing works
            try{
                metricsSolver.findRankForCitingWorks(citingWorks);
            }catch(Exception ex){
                Debug.print("Exception while finding rank for citing works: "+ex.toString());
                resultsMsg += "Exception while finding rank for citing works.\n";
            }

            authorwork.setCitingWorks(citingWorks);

            // Progress bar
            numberOfWorksProcessed += 1;
        }

        try{
            metricsSolver.findRankForAuthorWorks(worksFound);
            metricsSolver.calcualteQualityCitationsForAuthorWorks(worksFound);
        }catch(AppException aEx){
            Debug.print("Exception while calculating the number of quality citations for author works: "+aEx.toString());
            resultsMsg += "Exception while calculating the number of quality citations for author works.\n";
        }

        return searchCompletedOk;
    }

    public void searchAuthorWorks(Author author, ArrayList<AuthorWork> results){
        boolean cachedResultsFound = searchCachedAuthorWorksInServer(author.getUrl(), author, results);

        if(!cachedResultsFound){
            Debug.info("No cached data found - performing web search");
            boolean webResultsFound = searchWebAuthorData(author.getUrl(), results);
            if(webResultsFound == true){
                Debug.info("Saving data to cache server");
                boolean isDataSaved = saveAuthorWorksToServer(author, results);
                if(isDataSaved == true){
                    Debug.info("   Data saved ok");
                }else{
                    Debug.info("   Could not save data");
                }
            }
        }
        /*else{ 
            // TODO Fix for current data stored in cache server and remove next segment of code
            // Correct problems in data stored with number of quality citations
            MetricsSolver metrics = new MetricsSolver();
            try{
                metrics.findRankForAuthorWorks(results);
                metrics.calcualteQualityCitationsForAuthorWorks(worksFound);
            }catch(Exception ex){
                Debug.print("Exception while calculating the number of quality citations for author works: "+ex.toString());
                resultsMsg += "Exception while calculating the number of quality citations for author works.\n";

            }
            saveAuthorWorksToServer(author, results);
        }*/
    }

    private String divideStringAt(String string, String separator, boolean getFirstPart){
        int indexOfOccurrence = string.indexOf(separator);
        String subString = "";
        if(indexOfOccurrence != -1){
            if(getFirstPart == true){
                // separator is not included by default
                subString = string.substring(0, indexOfOccurrence);
            }else{
                // Remove the separator from the returned string
                subString = string.substring(indexOfOccurrence+separator.length());
            }
        }else{
            subString = string;
        }
        return subString;
    }

    private String resolvePublisher(String urlCitationWork, String publisherNameIncomplete) throws IOException{
        String publisher = publisherNameIncomplete;
        if(urlCitationWork.contains(".pdf") == false){
            // Get the header and determine if the resource is in text format (html or plain)
            // to be able to extract the publisher name
            final RequestConfig requestConfig = RequestConfig.custom()
                                                    .setConnectTimeout(AppHelper.connectionTimeOut)
                                                    .setConnectionRequestTimeout(AppHelper.connectionTimeOut)
                                                    .setSocketTimeout(AppHelper.connectionTimeOut)
                                                    .setStaleConnectionCheckEnabled(true)
                                                    .build();
            final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

            HttpHead httpHead = new HttpHead(urlCitationWork); 
            try{
                CloseableHttpResponse responseHead = httpclient.execute(httpHead);
                StatusLine statusLineHead = responseHead.getStatusLine();
                responseHead.close();
                String contentType = responseHead.getFirstHeader("Content-Type").toString().toLowerCase();

                if(statusLineHead.getStatusCode() < 300 && contentType.contains("text/html") || contentType.contains("text/plain")){
                    HttpGet httpGet = new HttpGet(urlCitationWork); 

                    CloseableHttpResponse responsePost = httpclient.execute(httpGet);
                    StatusLine statusLine = responsePost.getStatusLine();

                    if (statusLine.getStatusCode() < 300) {
                        //AppHelper.waitBeforeNewRequest();
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                (responsePost.getEntity().getContent())));
                        String content = new String();
                        String line;
                        while ((line = br.readLine()) != null) {
                            content += line;
                        }

                        int bodyStartIndex = content.indexOf("<body");
                        if(bodyStartIndex < 0)bodyStartIndex = 0;

                        try{
                            publisherNameIncomplete = formatRegExSpecialCharsInString(publisherNameIncomplete);
                            Pattern pattern = Pattern.compile(publisherNameIncomplete+"(\\w|\\d|-|\\s)+");
                            Matcher matcher = pattern.matcher(content);
                            if(matcher.find(bodyStartIndex)){
                                publisher = content.substring(matcher.start(), matcher.end());
                            }else{
                                publisher = publisherNameIncomplete;
                            }
                        }catch(Exception ex){
                            Debug.print("Exception while resolving publisher for citing work - extrating pattern from citation web resource: "+ex.toString());
                            resultsMsg += "Exception while resolving publisher for citing work - extrating pattern from citation web resource.\n";
                        }
                    }
                    responsePost.close();

                }
            }catch(IOException ioEx){
                            Debug.print("Exception while resolving publisher for citing work: "+ioEx.toString());
                            resultsMsg += "Exception while resolving publisher for citing work.\n";
            }
        }
        publisher = publisher.trim();
        return publisher;
    }

    private String removeNonLetterCharsAtBeginning(String string){
        int indexFirstLetter = 0;
        Pattern letterOrNumberCharPatter = Pattern.compile("[a-zA-Z1-9]");
        Matcher matcher = letterOrNumberCharPatter.matcher(string);
        if(matcher.find(0)){
            indexFirstLetter = matcher.start();
        }
        return string.substring(indexFirstLetter);
    }

    private CitingWork extractCitationWork(Element citationElements){
        CitingWork citingWork = new CitingWork();
        // Extract title(name) and url
        Elements nameElems = citationElements.select("h3.gs_rt > a");
        String urlCitationWork = "";
        if(nameElems.size() > 0){
            citingWork.setName(nameElems.get(0).text());
            urlCitationWork = nameElems.get(0).attr("href");
            citingWork.setUrl(urlCitationWork);
        }
        // Extract authors, publisher in google/external web/calculated
        Elements publicationElem = citationElements.select("div.gs_a");
        if(publicationElem.size() > 0){
            String citationData = publicationElem.get(0).text();
            // Get the author data
            citingWork.setAuthors(divideStringAt(citationData, " - ", true));
            // Remove Authors data to extract published in value
            String citationWithoutAuthors = divideStringAt(citationData, " - ", false);
            // Remove the year data
            String publisher = divideStringAt(citationWithoutAuthors, ", ", true);
            citingWork.setPublisherInGoogle(publisher);
            // Assumes that only one sequence "..." exist AT THE END to indicate the name is incomplete
            if(publisher.contains("…") && publisher.length() > 5 && urlCitationWork.length() > 0){
                try{
                    // Remove the " ..." string at the end
                    String publisherNameIncompleteWithoutDots = publisher.replace("…", "");
                    String resolvedPublisher = resolvePublisher(urlCitationWork, publisherNameIncompleteWithoutDots);
                    citingWork.setPublisherInExternalWeb(resolvedPublisher);
                    publisher = handlePublicationNameCases(resolvedPublisher);
                }catch(IOException ioEx){
                    Debug.print("Exception while extracting citing work: "+ioEx.toString());
                    resultsMsg += "Exception while extracting citing work.\n";
                }
            }else{
                publisher = removeNonLetterCharsAtBeginning(publisher);
                publisher = handlePublicationNameCases(publisher);
                publisher = correctAuthorWorkPublisher(publisher);
            }
            citingWork.setPublisher(publisher);
        }
        // Extract citation number
        Elements citationElems = citationElements.select("div.gs_fl > a");
        if(citationElems.size()>0){
            String citationNumberData = citationElems.get(0).text();
            citationNumberData = citationNumberData.replace("Cited by ", "");
            citingWork.setCitationsNumber(citationNumberData);
        }
        return citingWork;
    }

    public void searchCitationsForWork(AuthorWork awork, ArrayList<CitingWork> results) throws IOException{
        int maxResultsPerPage = 10;
        int indexCitationComponentStart = awork.getCitationsUrl().indexOf('?') + 1;
        String citationsUrlComponent = awork.getCitationsUrl().substring(indexCitationComponentStart);
        boolean continueSearch = true;
        int page = 0;

        while(continueSearch){
            Debug.info("Retrieving citations for work - page "+page);
            
            String citationsUrlPage = "https://scholar.google.com.au/scholar?start="+(page*maxResultsPerPage)+"&"+citationsUrlComponent;

            Document authorDoc = requestWebDocFromScholar(citationsUrlPage);
            AppHelper.waitBeforeNewRequest();

            Elements citationsElems = null;
            if(authorDoc != null){
                citationsElems = authorDoc.select("div.gs_ri");
                for(Element citationElements : citationsElems){
                    CitingWork citingWork = extractCitationWork(citationElements);
                    results.add(citingWork);
                }
            }
            page += 1;

            // End search if cannot retrieve the citations web page from scholar or it does not have 
            // any citation elements because there are no more citations.
            if(citationsElems == null || citationsElems.size() < maxResultsPerPage){
                continueSearch = false;
            }
        }
    }

    public void execute(){
        resultsMsg = "";
        serverDataCacheController.resetResultsMsg();
        try{
            if(authorToSearch != null && authorToSearch.getUrl().length() > 0){
                Debug.info("Executing search - params ok");
                searchAuthorWorks(authorToSearch, worksFound);
            }else{
                resultsMsg = "No author name given.";
            }
        }catch(Exception ex){
            // Catch any exception in order to terminate the call and not allowing the 
            // default behaviour of continuing the thread on stand by
            Debug.print("Exception while running the search for author works: "+ex.toString());
            resultsMsg += "Exception while running the search for author works.\n";
        }
    }

    public void setAuthorToSearch(Author authorToSearch) {
        this.authorToSearch = authorToSearch;
    }

    public Author getAuthorToSearch() {
        return authorToSearch;
    }

    public ArrayList<AuthorWork> getWorksFound() {
        return worksFound;
    }

    public String getResultsMsg() {
        return resultsMsg;
    }
}
