package com.eddicorp.controller;

import com.eddicorp.http.HttpRequest;
import com.eddicorp.http.HttpResponse;

import java.nio.file.Paths;

public class StaticFileController implements Controller{

    private static String STATIC_FILE_PATH = "pages";
    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        String filePath = determineFilePathFromUri(uri);
        String pathToLoad = Paths.get(STATIC_FILE_PATH, filePath).toString();
    }

    private String determineFilePathFromUri(String uri) {
        if("/".equals(uri)){
            return "index.html";
        }else{
            return uri;
        }
    }
}
