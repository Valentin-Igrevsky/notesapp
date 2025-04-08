package org.server.Utils;

import com.google.gson.Gson;

public class JsonUtils_server {
    private static final Gson gson = new Gson();


    public static String toJson(Object obj) {
        System.out.println(gson.toJson(obj));
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}