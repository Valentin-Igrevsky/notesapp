package org.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.server.models.Note;
import org.server.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RequestParser {
    ObjectMapper objectMapper = new ObjectMapper();

    public String toString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public Map<String, List<String>> parseQuery(HttpExchange exchange) {
        Map<String, List<String>> res = new HashMap<>();

        String queryParams = exchange.getRequestURI().getQuery();
        if (queryParams == null) {
            return null;
        }
        String[] params = queryParams.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            res.put(keyValue[0], Collections.singletonList(keyValue.length > 1 ? keyValue[1] : ""));
        }

        return res;
    }

    public Map<String, Object> parseBody(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes());

        try {
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public Map<Boolean, List<String>> validateAndExtractQueryParams(Map<String, List<String>> queryParams, List<String> requiredKeys) {
        List<String> errors = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (String key : requiredKeys) {
            if (!queryParams.containsKey(key)) {
                errors.add("Missing parameter: " + key);
                continue;
            }

            List<String> paramValues = queryParams.get(key);
            if (paramValues == null || paramValues.isEmpty() || paramValues.get(0) == null || paramValues.get(0).trim().isEmpty()) {
                errors.add("Empty or invalid value for parameter: " + key);
                continue;
            }

            values.add(paramValues.get(0).trim());
        }

        Map<Boolean, List<String>> result = new HashMap<>();
        if (errors.isEmpty()) {
            result.put(true, values);
        } else {
            result.put(false, errors);
        }

        return result;
    }

    public User formatUser(Map<String, Object> userBody) {
        int owner_id = (int) userBody.get("id");
        String name = (String) userBody.get("name");
        String surname = (String) userBody.get("surname");
        String username = (String) userBody.get("username");
        String password = (String) userBody.get("password");

        return new User(owner_id, name, surname, password, username);
    }

    public Note formatNote(Map<String, Object> noteBody) {
        long creationDate = (long) noteBody.get("createDate");
        long lastModifyDate = (long) noteBody.get("lastUpdateDate");

        int note_id = (int) noteBody.get("id");
        String title = (String) noteBody.get("title");
        String text = (String) noteBody.get("text");

        User owner = formatUser((Map<String, Object>) noteBody.get("owner"));

        return new Note(note_id, creationDate, lastModifyDate, owner, text, title);
    }
}
