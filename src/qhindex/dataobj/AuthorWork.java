/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.dataobj;

import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 *
 * @author Boris Sanchez
 */
public class AuthorWork {
    private StringProperty title = new SimpleStringProperty();
    private StringProperty authors = new SimpleStringProperty();
    private String publisherInGoogle = new String();
    private StringProperty publisher = new SimpleStringProperty();
    private String citationsUrl = new String();
    private StringProperty rankPublisher = new SimpleStringProperty();
    private IntegerProperty citations = new SimpleIntegerProperty();
    private IntegerProperty qualityCitations = new SimpleIntegerProperty();
    private StringProperty year = new SimpleStringProperty();
    private ArrayList<CitingWork> citingWorks = new ArrayList();
    
    public AuthorWork() {
    }

    public AuthorWork(String title, String publisher, int citations) {
        this.title.set(title);
        this.publisher.set(publisher);
        this.citations.set(citations);
    }

    public String getTitle() {
        return getTitleProperty().get();
    }

    public void setTitle(String title) {
        getTitleProperty().set(title);
    }
    
    public StringProperty getTitleProperty(){
        if(title == null)
            title = new SimpleStringProperty("");
        return title;
    }

    public String getAuthors() {
        return getAuthorsProperty().get();
    }

    public void setAuthors(String authors) {
        getAuthorsProperty().set(authors);
    }
    
    public StringProperty getAuthorsProperty(){
        if(authors == null)
            authors = new SimpleStringProperty("");
        return authors;
    }

    public String getPublisher() {
        return getPublisherProperty().get();
    }

    public void setPublisher(String publisher) {
        getPublisherProperty().set(publisher);
    }
    
    public StringProperty getPublisherProperty(){
        if(publisher == null)
            publisher = new SimpleStringProperty("");
        return publisher;
    }

    public String getCitationsUrl() {
        if(citationsUrl == null)
            citationsUrl = new String("");
        return citationsUrl;
    }

    public void setCitationsUrl(String citationsUrl) {
        this.citationsUrl = citationsUrl;
    }

    public String getRankPublisher() {
        return getRankPublisherProperty().get();
    }

    public void setRankPublisher(String rankPublisher) {
        getRankPublisherProperty().set(rankPublisher);
    }
    
    public StringProperty getRankPublisherProperty(){
        if(rankPublisher == null)
            rankPublisher = new SimpleStringProperty("");
        return rankPublisher;
    }
    
    public ObservableList<CitingWork> getCitingWorksObservableList(){
        ObservableList<CitingWork> citingWorksObservableList = FXCollections.observableArrayList();
        
        for(int i = 0; i < citingWorks.size(); i++){
            citingWorksObservableList.add(citingWorks.get(i));
        }
        return citingWorksObservableList;
    }
    
    public ArrayList<CitingWork> getCitingWorks(){
        return citingWorks;
    }

    public void setCitingWorks(ArrayList<CitingWork> citingWorks) {
        this.citingWorks = citingWorks;
    }

    public Integer getQualityCitations() {
        return qualityCitations.get();
    }

    public void setQualityCitations(int qualityCitations) {
        this.qualityCitations.set(qualityCitations);
    }

    public IntegerProperty getQualityCitationsProperty() {
        if(this.qualityCitations == null)
            this.qualityCitations = new SimpleIntegerProperty(0);
        return this.qualityCitations;
    }

    public Integer getCitations() {
        return this.getCitationsProperty().get();
    }

    public void setCitations(int citations) {
        this.getCitationsProperty().set(citations);
    }

    public IntegerProperty getCitationsProperty() {
        if(this.citations == null)
            this.citations = new SimpleIntegerProperty(0);
        return this.citations;
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
}

