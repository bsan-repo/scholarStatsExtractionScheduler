/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.db;

import qhindex.dataobj.EraRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import qhindex.util.AppException;

/**
 *
 * @author Boris Sanchez
 */
public class EraRecordDao {
    public void createTable() throws AppException{
        Connection con = DatabaseConn.getEraDbConn();
        Statement stmt = null;
        try{
            stmt = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS era_record("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "era_id VARCHAR(6), "+
                        "name VARCHAR(100), "+
                        "acronym VARCHAR(15), "+
                        "rank VARCHAR(2),"+
                        "type VARCHAR(10));"; 
            stmt.execute(sql);
        }catch(SQLException sqlEx){
            throw new AppException("Could not create ERA table.", sqlEx);
        }finally{
            DatabaseConn.closeStatement(stmt);
        }
    }
    
    private void insert(EraRecord record) throws AppException{
        PreparedStatement stmt = null;
        try{
            String sql = "INSERT INTO era_record(id, era_id, name, acronym, rank, type) "+
                        "VALUES(NULL, ?, ?, ?, ?, ?);";
            Connection con = DatabaseConn.getEraDbConn();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, record.getEraId());
            stmt.setString(2, record.getName());
            stmt.setString(3, record.getAcronym());
            stmt.setString(4, record.getRank());
            stmt.setString(5, record.getType().name());
            stmt.executeUpdate();
        }catch(SQLException sqlEx){
            throw new AppException("Could not inserting ERA data.", sqlEx);
        }finally{
            DatabaseConn.closeStatement(stmt);
        }

    }
    
    private void update(EraRecord record) throws AppException{
        PreparedStatement stmt = null;
        try{
            String sql = "UPDATE era_record SET era_id=?, name=?, "+
                         "acronym=?, rank=?, type=? WHERE id=?;";
            Connection con = DatabaseConn.getEraDbConn();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, record.getEraId());
            stmt.setString(2, record.getName());
            stmt.setString(3, record.getAcronym());
            stmt.setString(4, record.getRank());
            stmt.setString(5, record.getType().name());
            stmt.setInt(6, record.getId());
            stmt.executeUpdate();
        }catch(SQLException sqlEx){
            throw new AppException("Could not update ERA data.", sqlEx);
        }finally{
            DatabaseConn.closeStatement(stmt);
        }
    }
    
    public void save(EraRecord record) throws AppException{
        if(record.getId() > 0){
            update(record);
        }else{
            insert(record);
        }
    }
    
    public void searchWhere(String whereStr, String whereValue, ArrayList<EraRecord> foundRecords) throws AppException{
        PreparedStatement stmt = null;
        try{
            String sql = "SELECT id, era_id, name, acronym, rank, type  FROM era_record "+whereStr+" COLLATE NOCASE;";
            Connection con = DatabaseConn.getEraDbConn();
            stmt = con.prepareStatement(sql); 
            // Warning: Not tested for case ID integer
            stmt.setString(1, whereValue); 
            ResultSet results = stmt.executeQuery();

            while(results.next()){
                EraRecord record = new EraRecord();
                record.setId(results.getInt("id"));
                record.setEraId(results.getString("era_id"));
                record.setName(results.getString("name"));
                record.setAcronym(results.getString("acronym"));
                record.setRank(results.getString("rank"));
                record.setType(EraRecord.RecordType.valueOf(results.getString("type")));
                foundRecords.add(record);
            }
        }catch(SQLException sqlEx){
            throw new AppException("Could not create ERA table.", sqlEx);
        }finally{
            DatabaseConn.closeStatement(stmt);
        }
    }
    
    public EraRecord searchByAcronym(String acronym) throws AppException{
        ArrayList<EraRecord> foundRecords = new ArrayList<EraRecord>();
        String whereStr = "where acronym = ?";
        searchWhere(whereStr, acronym, foundRecords);
        if(foundRecords.size() > 0){
            return foundRecords.get(0);
        }else{
            return new EraRecord();
        }
    }
    
    public EraRecord searchByName(String name) throws AppException{
        ArrayList<EraRecord> foundRecords = new ArrayList<EraRecord>();
        String whereStr = "where name = ?";
        searchWhere(whereStr, name, foundRecords);
        if(foundRecords.size() > 0){
            return foundRecords.get(0);
        }else{
            return new EraRecord();
        }
        
    }
}
