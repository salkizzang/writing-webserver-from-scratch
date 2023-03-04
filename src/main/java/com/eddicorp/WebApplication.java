package com.eddicorp;

import com.eddicorp.quiz.self.HttpRequest;
import com.eddicorp.quiz.week1.application.repository.posts.PostRepository;
import com.eddicorp.quiz.week1.application.repository.posts.PostRepositoryImpl;
import com.eddicorp.quiz.week1.application.repository.users.UserRepository;
import com.eddicorp.quiz.week1.application.repository.users.UserRepositoryImpl;
import com.eddicorp.quiz.week1.application.service.posts.Post;
import com.eddicorp.quiz.week1.application.service.posts.PostService;
import com.eddicorp.quiz.week1.application.service.posts.PostServiceImpl;
import com.eddicorp.quiz.week1.application.service.users.User;
import com.eddicorp.quiz.week1.http.response.ResponseCookie;
import com.eddicorp.quiz.week1.http.session.HttpSession;
import com.eddicorp.quiz.week1.http.session.SessionManager;
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
        SessionManager sessionManager = new SessionManager();
        HttpSession httpSession = null;
        String sessionId = "";
        UserRepository userRepository = UserRepositoryImpl.getInstance();
//        PostRepository postRepository = PostRepositoryImpl.getInstance();
        PostService postService = new PostServiceImpl();
        while((clientSocket = serverSocket.accept()) != null){
            try(
                    final InputStream inputStream = clientSocket.getInputStream();
                    final OutputStream outputStream = clientSocket.getOutputStream();
            ){
                HttpRequest httpRequest = new HttpRequest(inputStream);
                String uri = httpRequest.getUri();
                String method = httpRequest.getMethod();
                final Map<String, Object> context = new HashMap<>();
                ResponseCookie cookie = null;

                // 로그인
                if("/login".equals(uri)){
                    String usrNm = httpRequest.getParameter("username");
                    String pwd = httpRequest.getParameter("password");
                    User loginUser = userRepository.findByUsername(usrNm);
                    method = "GET";
                    uri = "/";
                    if(loginUser!=null&&loginUser.getPassword().equals(pwd)){
                        sessionId = sessionManager.createNewSession();
                        System.out.println("SessionId = " + sessionId);
                        httpSession = sessionManager.getSession(sessionId);
                        httpSession.setAttribute("USER", loginUser);
                        cookie = new ResponseCookie("id", loginUser.getUsername(), "/","localhost",9999);
                    }

                }
                // 회원가입
                if("/users".equals(uri)){
                    String usrNm = httpRequest.getParameter("username");
                    String pwd = httpRequest.getParameter("password");
                    userRepository.signUp(new User(usrNm, pwd));
                    method = "GET";
                    uri = "/";
                }

                //글쓰기
                if("/post".equals(uri)){
                    String usrId = httpRequest.getCookie("id");
                    String title = httpRequest.getParameter("title");
                    String content = httpRequest.getParameter("content");
                    if(usrId!=null) postService.write(usrId, title, content);
                    else postService.write("anonymous", title, content);
                    System.out.println(usrId);
                    uri = "/";
                    method = "GET";
                }
                //로그아웃
                if("/logout".equals(uri)){
                    httpSession = sessionManager.getSession(sessionId);
                    httpSession.removeAttribute("USER");
                    sessionManager.removeSession(sessionId);
                    httpRequest.deleteCookie();
                    cookie = null;
                    uri = "/";
                }

                String fileName;
                if("/".equals(uri)){
                    fileName = "index.html";
                    List<Post> posts = postService.getAllPosts();
                    context.put("posts", posts);
                    for (Post post : posts) {
                        System.out.println(post.toString());
                        context.put("title", post.getTitle());
                        context.put("author", post.getAuthor());
                        context.put("content", post.getContent());
                    }
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
                final Mustache.Compiler compiler = Mustache.compiler();
                System.out.println(new String(rawFileToServe));
                final Template template = compiler.compile(new String(rawFileToServe));

                if(httpSession != null && httpSession.getAttribute("USER") != null){
                    context.put("isLoggedIn", true);
                }else{
                    context.put("isLoggedIn", false);
                }
                String renderedPage = template.execute(context);
                System.out.println("@@@@@@@@@@@@");
                System.out.println(renderedPage);
                System.out.println(uri);
                final String CRLF = "\r\n";
                if(method.equals("GET")) {
                    String contentType = "Content-Type: " + mimeType + CRLF;
                    String contentLength = "Content-Length: " + renderedPage.getBytes(StandardCharsets.UTF_8).length + CRLF;
                    String statusLine = "HTTP/1.1 200 OK" + CRLF;
                    outputStream.write(statusLine.getBytes(StandardCharsets.UTF_8));
                    if(cookie != null) outputStream.write(("Set-Cookie : "+cookie.build()+CRLF).getBytes(StandardCharsets.UTF_8));
                    if(cookie == null) outputStream.write(("Set-Cookie : "+CRLF).getBytes(StandardCharsets.UTF_8));
                    outputStream.write(contentLength.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(contentType.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(CRLF.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(renderedPage.getBytes(StandardCharsets.UTF_8));
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
