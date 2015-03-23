/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qhindex.servercomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.Consts;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import qhindex.util.AdminMail;
import qhindex.util.AppException;
import qhindex.util.AppHelper;
import qhindex.util.Debug;

/**
 *
 * @author Boris Sanchez
 */
public class ServerDataCache {
    private String resultsMsg = new String();
    
    public JSONObject sendRequest(JSONObject data, String url, boolean sentAdminNotification){
        JSONObject obj = new JSONObject();
        
        final RequestConfig requestConfig = RequestConfig.custom()
                                                .setConnectTimeout(AppHelper.connectionTimeOut)
                                                .setConnectionRequestTimeout(AppHelper.connectionTimeOut)
                                                .setSocketTimeout(AppHelper.connectionTimeOut)
                                                .setStaleConnectionCheckEnabled(true)
                                                .build();
        final CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
        HttpPost httpPost = new HttpPost(url); 
            
        String dataStr = data.toJSONString();
        dataStr = dataStr.replaceAll("null", "\"\"");

        if(sentAdminNotification == true){
            try{
                AdminMail adminMail = new AdminMail();
                adminMail.sendMailToAdmin("Data to send to the server.", dataStr);
            }catch(Exception ex){ // Catch any problem during this step and continue 
                Debug.print("Could not send admin notification e-mail: "+ex);
            }
        }
        Debug.print("DATA REQUEST: "+dataStr);
        StringEntity jsonData = new StringEntity(dataStr, ContentType.create("plain/text", Consts.UTF_8));
        jsonData.setChunked(true);
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("accept", "application/json");
        httpPost.setEntity(jsonData);

        try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                Debug.print("Exception while sending http request: "+statusLine.getStatusCode() + " : " +
                                statusLine.getReasonPhrase());
                resultsMsg += "Exception while sending http request: "+statusLine.getStatusCode() + " : " +
                                statusLine.getReasonPhrase()+"\n";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));
            String output = new String();
            String line;
            while ((line = br.readLine()) != null) {
                output += line;
            }
            output = output.substring(output.indexOf('{'));
            try{
                obj = (JSONObject)new JSONParser().parse(output);
            }catch(ParseException pEx){
                Debug.print("Could not parse internet response. It is possible the cache server fail to deliver the content: "+pEx.toString());
                resultsMsg += "Could not parse internet response. It is possible the cache server fail to deliver the content.\n";
            }
        }catch(IOException ioEx){
            Debug.print("Could not handle the internet request: "+ioEx.toString());
            resultsMsg += "Could not handle the internet request.\n";
        }
        return obj;
    }

    public String getResultsMsg() {
        return resultsMsg;
    }

    public void resetResultsMsg() {
        resultsMsg = "";
    }
}
