/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.json;

import org.json.simple.JSONObject;
import qhindex.dataobj.Author;

/**
 *
 * @author Boris Sanchez
 */
public class AuthorJsonConverter {
    public JSONObject AuthorToJson(Author author){
        JSONObject obj=new JSONObject();
        obj.put("name", author.getName());
        obj.put("affiliation", author.getAffiliation());
        obj.put("url", author.getUrl());
        
        return obj;
    }
    
    public Author jsonToAuthor(JSONObject obj){
        Author author = new Author();
        author.setName((String)obj.get("name"));
        author.setAffiliation((String)obj.get("affiliation"));
        author.setUrl((String)obj.get("url"));
        
        return author;
    }
}
