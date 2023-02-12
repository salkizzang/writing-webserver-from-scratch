package com.eddicorp.application.controller;

import com.eddicorp.http.request.HttpRequest;
import com.eddicorp.http.response.HttpResponse;

import java.nio.file.Paths;

public class NotFoundController implements Controller {
    private static final String STATIC_FILE_PATH = "pages";

    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        final String pathToLoad = Paths.get(STATIC_FILE_PATH, "not-found.html").toString();
    }
}
