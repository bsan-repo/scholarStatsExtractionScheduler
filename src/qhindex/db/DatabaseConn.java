/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import qhindex.util.AppException;

// Set manually setAutoCommit(false) when block database operations are requied to be commited

/**
 *
 * @author Boris Sanchez
 */
public class DatabaseConn {
    private static DatabaseConn ref = null;
    private Connection eraDbConn = null;
    
    private DatabaseConn(){
    }
    
    private void init() throws AppException{
        
        boolean pathExist = false;
        File file = new File("db");
        if(!file.exists()){
            pathExist = file.mkdir();
        }else{
            pathExist = true;
        }
        
        if(pathExist){
            try{
                eraDbConn = DriverManager.getConnection("jdbc:sqlite:db/era.db");//jdbc:sqlite:D:\\testdb.db    "jdbc:sqlite:/home/users.sqlite"

                EraRecordDao eraRecordDao = new EraRecordDao();
                eraRecordDao.createTable();
            }catch(SQLException sqlEx){
                throw new AppException("Error accessing the database.", sqlEx);
            }
        }else{
            throw new AppException("Could not initialize or access the database folder.");
        }
    }
    
    public static void initializeDb() throws AppException{
        EraRecordDao eraRecordDao = new EraRecordDao();
        eraRecordDao.createTable();
    }
    
    public static DatabaseConn getRef() throws AppException{
        if(ref == null){
            ref = new DatabaseConn();
            ref.init();
        }
        return ref;
    }
    
    public static void deInit() throws AppException{
        try{
            getRef().eraDbConn.close();
        }catch(SQLException sqlEx){
            throw new AppException("Could not close DB resources.", sqlEx);
        }
    }
    
    public static Connection getEraDbConn() throws AppException{
        return getRef().eraDbConn;
    }
    
    public static Connection getAuthorDbConn() throws AppException{
        return getRef().eraDbConn;
    }
    
    public static void closeStatement(Statement stmt) throws AppException{
        if(stmt != null){
            try{
                stmt.close();
            }catch(SQLException sqlEx){
                throw new AppException("Could not close DB resource.", sqlEx);
            }
        }
    }
}
