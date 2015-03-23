/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.dataobj;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Boris Sanchez
 */
public class CitingWork {
    private StringProperty name = new SimpleStringProperty();
    private StringProperty publisher = new SimpleStringProperty();
    private StringProperty rankPublisher = new SimpleStringProperty();
    // Tbe following attributes are not used at the moment
    private StringProperty year = new SimpleStringProperty();
    private StringProperty authors = new SimpleStringProperty();
    private String url = new String();
    private String citationsNumber = new String();
    // Collected in case a better method to extract the name can be implemented later
    private String publisherInGoogle = new String();
    private String publisherInExternalWeb = new String();

    public CitingWork() {
    }

    public CitingWork(String name, String authors, String publisher, String rankPublisher, String year) {
        this.name.set(name);
        this.authors.set(authors);
        this.publisher.set(publisher);
        this.rankPublisher.set(rankPublisher);
        this.year.set(year);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty getNameProperty(){
        return name;
    }

    public String getAuthors() {
        return authors.get();
    }

    public void setAuthors(String authors) {
        this.authors.set(authors);
    }
    
    public StringProperty getAuthorsProperty(){
        return authors;
    }

    public String getPublisher() {
        return publisher.get();
    }

    public void setPublisher(String publisher) {
        this.publisher.set(publisher);
    }
    
    public StringProperty getPublisherProperty(){
        return publisher;
    }

    public String getRankPublisher() {
        return rankPublisher.get();
    }

    public void setRankPublisher(String rankPublisher) {
        this.rankPublisher.set(rankPublisher);
    }
    
    public StringProperty getRankPublisherProperty(){
        return rankPublisher;
    }

    public String getYear() {
        return year.get();
    }

    public void setYear(String year) {
        this.year.set(year);
    }
    
    public StringProperty getYearProperty(){
        return year;
    }

    public String getPublisherInGoogle() {
        return publisherInGoogle;
    }

    public void setPublisherInGoogle(String publisherInGoogle) {
        this.publisherInGoogle = publisherInGoogle;
    }

    public String getPublisherInExternalWeb() {
        return publisherInExternalWeb;
    }

    public void setPublisherInExternalWeb(String publisherInExternalWeb) {
        this.publisherInExternalWeb = publisherInExternalWeb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCitationsNumber() {
        return citationsNumber;
    }

    public void setCitationsNumber(String citationsNumber) {
        this.citationsNumber = citationsNumber;
    }
}
