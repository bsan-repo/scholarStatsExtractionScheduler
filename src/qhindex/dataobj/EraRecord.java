/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.dataobj;


/**
 *
 * @author Boris Sanchez
 */
public class EraRecord {
    
    public enum RecordType{
        JOURNAL,
        CONF
    };
    
    private int id;
    private String eraId;
    private String name;
    private String rank;
    private String acronym;
    private RecordType type;
    
    public EraRecord(){
        this.id = -1;
    }

    public EraRecord(String eraId, String name, String acronym, String rank, RecordType type) {
        this.id = -1;
        this.eraId = eraId;
        this.name = name;
        this.acronym = acronym;
        this.type = type;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEraId() {
        return eraId;
    }

    public void setEraId(String eraId) {
        this.eraId = eraId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        this.type = type;
    }
}
