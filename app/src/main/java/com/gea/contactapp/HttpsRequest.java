package com.gea.contactapp;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class HttpsRequest {
    //Supported HttpRequest methods
    public enum Method{
        POST,PUT,DELETE,GET
    }

    private HttpsURLConnection con;
    private OutputStream os;
    //After instantiation, when opening connection - IOException can occur
    private HttpsRequest(URL url)throws IOException{
        con = (HttpsURLConnection) url.openConnection();
    }
    //Can be instantiated with String representation of url, force caller to check for IOException which can be thrown
    public HttpsRequest(String url)throws IOException{
        this(new URL(url));
    }
    /**
     * Sending connection and opening an output stream to server by pre-defined instance variable url
     */
    private void prepareAll(Method method)throws IOException{
        con.setDoInput(true);
        con.setRequestMethod(method.name());
        if(method==Method.POST||method==Method.PUT){
            con.setDoOutput(true);
            os = con.getOutputStream();
        }
    }
    /**
     * Prepares HttpRequest method with for given method, possible values: HttpRequest.Method.POST,
     * HttpRequest.Method.PUT, HttpRequest.Method.GET & HttpRequest.Method.DELETE*/
    public HttpsRequest prepare(Method method)throws IOException{
        prepareAll(method);
        return this;
    }
    /**
     * Adding request headers (standard format "Key":"Value")*/
    public HttpsRequest withHeaders(String... headers){
        for (String header : headers) {
            String[] h = header.split("[:]");
            con.setRequestProperty(h[0], h[1]);
        }
        return this;
    }

    /**
     * Writes query to open stream to server
     */
    private void withData(String query) throws IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.close();
    }

    /**
     * Builds query on format of key1=v1&key2=v2 from given hashMap structure */
    public HttpsRequest withData(HashMap<String,String> params) throws IOException{
        StringBuilder result=new StringBuilder();
        for(Map.Entry<String,String>entry : params.entrySet()){
            result.append(result.length() > 0 ? "&" : "").append(entry.getKey()).append("=").append(entry.getValue());//appends: key=value (for first param) OR &key=value(second and more)
        }
        withData(result.toString());
        return this;
    }
    /**
     * Sending request to the server and pass to caller String as it received in response from server
     * */
    public String sendAndReadString() throws IOException{
        BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response=new StringBuilder();
        for(String line;(line=br.readLine())!=null;) response.append(line).append("\n");
        return response.toString();
    }

}