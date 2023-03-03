package com.eddicorp;

import com.eddicorp.quiz.self.HttpRequest;
import com.eddicorp.quiz.week1.application.repository.users.UserRepository;
import com.eddicorp.quiz.week1.application.repository.users.UserRepositoryImpl;
import com.eddicorp.quiz.week1.application.service.posts.Post;
import com.eddicorp.quiz.week1.application.service.users.User;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebApplication {

    public static void main(String[] args) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket;
        while((clientSocket = serverSocket.accept()) != null){
            try(
                    final InputStream inputStream = clientSocket.getInputStream();
                    final OutputStream outputStream = clientSocket.getOutputStream();
            ){
                HttpRequest httpRequest = new HttpRequest(inputStream);
                String uri = httpRequest.getUri();
                String method = httpRequest.getMethod();



                if("/login".equals(uri)){
                    UserRepository userRepository = UserRepositoryImpl.getInstance();
                    String usrNm = httpRequest.getParameter("username");
                    String pwd = httpRequest.getParameter("password");
                    userRepository.signUp(new User(usrNm, pwd));
                    System.out.println(userRepository.findByUsername("test"));
                    method = "GET";
                    uri = "/";
                }

                String fileName;
                if("/".equals(uri)){
                    fileName = "index.html";
                }else{
                    fileName = uri;
                }

                String extension = null;
                final int indexOfPeriod = uri.lastIndexOf(".");
                if (indexOfPeriod != -1) {
                    extension = uri.substring(indexOfPeriod + 1);
                }

                String mimeType = "text/html; charset=utf-8";
                if(extension==null){
                    mimeType = "text/html; charset=utf-8";
                }
                if ("html".equals(extension)) {
                    mimeType = "text/html; charset=utf-8";
                }
                if ("css".equals(extension)) {
                    mimeType = "text/css; charset=utf-8";
                }
                if ("svg".equals(extension)) {
                    mimeType = "image/svg+xml";
                }
                if ("ico".equals(extension)) {
                    mimeType = "image/x-icon";
                }

                final byte[] rawFileToServe = readFileFromResourceStream(fileName);
                String renderedPage="";
                System.out.println(uri);

                final String CRLF = "\r\n";
                if(method.equals("GET")) {
                    String contentType = "Content-Type: " + mimeType + "\r\n";
                    String contentLength = "Content-Length: " + rawFileToServe.length + "\r\n";
                    String statusLine = "HTTP/1.1 200 OK" + CRLF;
                    outputStream.write(statusLine.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(contentLength.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(contentType.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(CRLF.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(rawFileToServe);
                    outputStream.flush();
                }
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    private static byte[] readFileFromResourceStream(String fileName) throws IOException {
        final String filePath = Paths.get("pages", fileName).toString();
        try (
                final InputStream resourceInputStream = WebApplication.class
                        .getClassLoader()
                        .getResourceAsStream(filePath);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            if (resourceInputStream == null) {
                return null;
            }
            int readCount = 0;
            final byte[] readBuffer = new byte[8192];
            while ((readCount = resourceInputStream.read(readBuffer)) != -1) {
                baos.write(readBuffer, 0, readCount);
            }
            return baos.toByteArray();
        }
    }
}
