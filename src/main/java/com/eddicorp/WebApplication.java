package com.eddicorp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class WebApplication {

    public static void main(String[] args) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket;
        while((clientSocket = serverSocket.accept()) != null){
            try(
                    final InputStream inputStream = clientSocket.getInputStream();
                    final OutputStream outputStream = clientSocket.getOutputStream();
            ){

//                StringBuilder sb = new StringBuilder();
//                int readCharacter;
//                while((readCharacter = inputStream.read())!=-1){
//                    char tmp = (char) readCharacter;
//                    if(tmp=='\r'){
//                        char isEndCheck = (char) inputStream.read();
//                        if(isEndCheck=='\n'){
//                            sb.append(isEndCheck);
//                        }
//                    }
//                    sb.append(tmp);
//                }

//                final StringBuilder stringBuilder = new StringBuilder();
//                int readCharacter;
//                while ((readCharacter = inputStream.read()) != -1) {
//                    final char currentChar = (char) readCharacter;
//                    if (currentChar == '\r') {
//                        if (((char) inputStream.read()) == '\n') {
//                            System.out.println(stringBuilder.toString());
//                        } else {
//                            throw new IllegalStateException("Invalid HTTP request.");
//                        }
//                    }
//                    stringBuilder.append(currentChar);
//                }

                final String filePath = Paths.get("pages", "index.html").toString();
                final InputStream staticFileInputStream = WebApplication.class.getClassLoader().getResourceAsStream(filePath);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int readCount = 0;
                final byte[] readBuffer = new byte[8192];
                while((readCount = staticFileInputStream.read(readBuffer))!=-1){
                    baos.write(readBuffer, 0, readCount);
                }
                byte[] rawFile = baos.toByteArray();
                final String statusLine = "HTTP/1.1 200 OK\r\n";
                final String contentLength = "Content-Length: "+rawFile.length+"\r\n";
                final String contentHeader = "Content-Type: text/html;\r\n\r\n";
                outputStream.write(statusLine.getBytes(StandardCharsets.UTF_8));
                outputStream.write(contentLength.getBytes(StandardCharsets.UTF_8));
                outputStream.write(contentHeader.getBytes(StandardCharsets.UTF_8));
                outputStream.write(rawFile);
                outputStream.flush();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }
}
