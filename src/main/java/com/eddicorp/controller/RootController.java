package com.eddicorp.controller;

import com.eddicorp.http.HttpRequest;
import com.eddicorp.http.HttpResponse;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RootController implements Controller{

    private static String STATIC_FILE_PATH = "pages";
    private static Map<RequestMapper, Controller> requestMap = new HashMap<>();

    private Controller staticFileController = new StaticFile


    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        RequestMapper requestMapper = new RequestMapper(uri, request.getHttpMethod());
        Controller maybeController = requestMap.get(requestMapper);
        if(maybeController != null){
            maybeController.handle(request, response);
            return;
        }

        String pathToLoad = Paths.get(STATIC_FILE_PATH, uri).toString();
        URL maybeResource = this.getClass().getClassLoader().getResource(pathToLoad);
        if(maybeResource!=null){
            static
        }

    }
}
