package com.example.demo.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UrlUtil {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 获取URL内容
     * @param url URL
     * @return URL内容
     */
    public static  String retrieveUrl(String url) throws  Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
