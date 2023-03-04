package com.eddicorp.quiz.self;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String uri;
    private final String method;
    private final Map<String, String> parameterMap = new HashMap<>();
    private final Map<String, String> cookieMap = new HashMap<>();

    public String getUri() {
        return uri;
    }
    public String getMethod() {return method;}

    public HttpRequest(InputStream inputStream) throws IOException {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        final String rawRequestLine = readLine(inputStream);
        final String[] partsOfRequestLine = rawRequestLine.split(" ");
        this.uri = partsOfRequestLine[1];
        this.method = partsOfRequestLine[0];
        String header;
        System.out.println(rawRequestLine);
        while(!"".equals(header = readLine(inputStream))){
            System.out.println("header = " + header);
            String[] findCookie = header.split(":");
            if("Cookie".equals(findCookie[0])){
                String[] cookieSplit = findCookie[1].trim().split(";");
                for (String cookies : cookieSplit) {
                    String[] idCookie = cookies.trim().split("=");
                    System.out.println(idCookie[0]);
                    System.out.println(idCookie[1]);
                    cookieMap.put(idCookie[0], idCookie[1]);
//                    if("id".equals(idCookie)){
//                        cookieMap.put("id", )
//                    }
                }
            }
        }

        final int available = inputStream.available();
        if(available>0){
            byte[] bytes = new byte[available];
            inputStream.read(bytes, 0, available);
            String s = new String(bytes);
            System.out.println("s = "+s);
            String[] splitBody = s.split("&");
            for (String param : splitBody) {
                String[] splitParam = param.split("=");
                parameterMap.put(splitParam[0], splitParam[1]);
            }
        }
    }


    public String getParameter(String parameterName) {
        return parameterMap.get(parameterName);
    }
    public String getCookie(String parameterName) {
        return cookieMap.get(parameterName);
    }
    public String deleteCookie(){
        return cookieMap.remove("id");
    }

    private static String readLine(InputStream inputStream) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        int readCharacter;
        while ((readCharacter = inputStream.read()) != -1) {
            final char currentChar = (char) readCharacter;
            if (currentChar == '\r') {
                if (((char) inputStream.read()) == '\n') {
                    return stringBuilder.toString();
                } else {
                    throw new IllegalStateException("Invalid HTTP request.");
                }
            }
            stringBuilder.append(currentChar);
        }
        throw new IllegalStateException("Unable to find CRLF");
    }

}
