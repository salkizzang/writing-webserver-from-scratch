package com.eddicorp.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String uri;
    private HttpMethod httpMethod;
    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, String> parameterMap = new HashMap<>();
    private Map<String, Cookie> cookieMap = new HashMap<>();
    private byte[] rawBody;

    public HttpRequest(InputStream inputStream) throws IOException{
        String requestLine = readLine(inputStream);
        String[] partsOfRequestLine = requestLine.split(" ");
        this.httpMethod = HttpMethod.valueOf(partsOfRequestLine[0]);
        String[] uriAndQueryString = partsOfRequestLine[1].split("\\?");
        this.uri = uriAndQueryString[0].trim();
        parseHeaders(inputStream);
        byte[] rawBody = parseBody(inputStream);
        parseCookies();
        parseParameters(rawBody);
        this.rawBody = rawBody;
    }

    private void parseParameters(byte[] rawBody) throws UnsupportedEncodingException {
        String contentType = headerMap.get("Content-Type");
        if("application/x-www-form-urlencoded".equals(contentType) && rawBody != null){
            String urlEncodedForm = new String(rawBody);
            String decoded = URLDecoder.decode(urlEncodedForm, StandardCharsets.UTF_8.name());
            String[] keyAndValues = decoded.split("&");
            for(String keyAndValue : keyAndValues){
                String[] split = keyAndValue.split("=");
                if(split.length>1){
                    parameterMap.put(split[0].trim(), split[1].trim());
                }
            }
        }
    }

    private void parseCookies() {
        String rawCookie = headerMap.get("Cookie");
        if(rawCookie == null){
            return;
        }
        String[] rawCookies = rawCookie.split(";");
        for(String raw : rawCookies){
            String[] keyAndValue = raw.split("=");
            Cookie cookie = new Cookie(keyAndValue[0].trim(), keyAndValue[1].trim());
            cookieMap.put(cookie.getName(), cookie);
        }
    }

    private byte[] parseBody(InputStream inputStream) throws IOException {
        if(inputStream.available()>0){
            byte[] bodyBytes = new byte[inputStream.available()];
            inputStream.read(bodyBytes);
            return bodyBytes;
        }else{
            return null;
        }
    }

    private void parseHeaders(InputStream inputStream) throws IOException {
        String rawHeader;
        while(!"".equals((rawHeader = readLine(inputStream)))){
            String[] headerAndValues = rawHeader.split(":");
            String headerName = headerAndValues[0].trim();
            String headerValue = headerAndValues[1].trim();
            headerMap.put(headerName, headerValue);
        }
    }

    private String readLine(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int readCharacter;
        while((readCharacter=inputStream.read())!=-1){
            char currentChar = (char) readCharacter;
            if(currentChar=='\r'){
                if(((char)inputStream.read())=='\n'){
                    return stringBuilder.toString();
                }else{
                    throw new IllegalStateException("Unable to parse line.");
                }
            }
            stringBuilder.append(currentChar);
        }
        throw new IllegalStateException("Unable to find CRLF");
    }

    public String getUri(){return uri;}
    public HttpMethod getHttpMethod(){return httpMethod;}
    public String getParameter(String parameterName){return parameterMap.get(parameterName);}
//    public HttpSession

}
