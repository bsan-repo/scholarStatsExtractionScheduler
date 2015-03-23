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
public class Author{
    private StringProperty name;
    private StringProperty affiliation;
    private String url;
    
    public Author(){
        this.name = new SimpleStringProperty();
        this.affiliation = new SimpleStringProperty();
    }
    
    public Author(String name, String institution){
        this.name = new SimpleStringProperty();
        this.affiliation = new SimpleStringProperty();
        this.name.set(name);
        this.affiliation.set(institution);
    }
    
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty getNameProperty() {
        return name;
    }

    public String getAffiliation() {
        return affiliation.get();
    }

    public void setAffiliation(String affiliation) {
        this.affiliation.set(affiliation);
    }

    public StringProperty getAffiliationProperty() {
        return affiliation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
