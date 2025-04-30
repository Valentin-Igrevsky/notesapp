package org.client.controller;

import org.client.http.httpClient;
import org.client.models.Note;
import org.client.models.User;
import org.client.models.httpPacket;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Controller {
    private final httpClient client = new httpClient();
    private User currentUser;
    private List<Note> localNotes = new ArrayList<>();
    private List<Note> serverNotes = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File storage = new File("session.json");
    private boolean autoSync = true;

    public Controller() {
        loadFromFile();
    }

    public boolean login(String username, String password) {
        try {
            httpPacket response = client.login(username, password);
            if (response.isCorrect()) {
                currentUser = response.getUser();
                syncReplaceLocalWithServer();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean register(String name, String surname, String username, String password) {
        User user = new User(name, surname, username, password);
        try {
            httpPacket response = client.register(user);
            return response.isCorrect();
        } catch (Exception e) {
            return false;
        }
    }

    public List<Note> getLocalNotes() {
        return new ArrayList<>(localNotes);
    }

    public void createNote(String title, String content) {
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        Note newNote = new Note(currentUser, content, title);
        if (autoSync) {
            try {
                httpPacket response = client.addNote(newNote);
                if (response.isCorrect()) {
                    localNotes.add(response.getNote());
                }
            } catch (Exception e) {
                // Обработка ошибки
            }
        } else {
            localNotes.add(newNote);
        }
    }

    public void deleteNote(int id) {
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        try {
            httpPacket response = client.deleteNoteById(currentUser.getId(), id);
            if (response.isCorrect()) {
                localNotes.removeIf(note -> note.getId() == id);
            }
        } catch (Exception e) {
            // Обработка ошибки
        }
    }

    public void updateNote(int id, String title, String content) {
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        Note noteToUpdate = null;
        for (Note note : localNotes) {
            if (note.getId() == id) {
                noteToUpdate = note;
                break;
            }
        }
        if (noteToUpdate == null) {
            throw new IllegalArgumentException("Note not found");
        }
        noteToUpdate.setTitle(title);
        noteToUpdate.setText(content);
        noteToUpdate.setLastUpdateDate();
        if (autoSync) {
            try {
                httpPacket response = client.patchNote(noteToUpdate);
            } catch (Exception e) {
                // Обработка ошибки
            }
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
        autoSync = false;
        localNotes.clear();
        serverNotes.clear();
        saveToFile();
    }

    public void saveToFile() {
        Map<String, Object> data = new HashMap<>();
        data.put("user", currentUser);
        data.put("notes", localNotes);
        data.put("autoSync", autoSync);
        try {
            mapper.writeValue(storage, data);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения данных.");
        }
    }

    public boolean isAutoSync() {
        return autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
    }

    public void syncReplaceLocalWithServer() {
        loadNotesFromServer();
        localNotes = new ArrayList<>(serverNotes);
    }

    public void syncUploadLocalToServer() {
        loadNotesFromServer();
        Map<Note, String> plan = prepareSyncPlan();
        syncToServer(plan);
        loadNotesFromServer();
        localNotes = new ArrayList<>(serverNotes);
    }

    private void loadFromFile() {
        if (!storage.exists()) return;
        try {
            Map<String, Object> data = mapper.readValue(storage, new TypeReference<>() {});
            currentUser = mapper.convertValue(data.get("user"), User.class);
            localNotes = mapper.convertValue(data.get("notes"), new TypeReference<>() {});
            autoSync = mapper.convertValue(data.get("autoSync"), Boolean.class);
        } catch (IOException e) {
            System.out.println("Ошибка загрузки данных.");
        }
    }

    private void loadNotesFromServer() {
        try {
            httpPacket response = client.getAllNotes(currentUser.getId());
            if (response.isCorrect()) {
                serverNotes = response.getNotes();
            } else {
                serverNotes = new ArrayList<>();
            }
        } catch (Exception e) {
            serverNotes = new ArrayList<>();
        }
    }

    private Map<Note, String> prepareSyncPlan() {
        Map<Note, String> plan = new HashMap<>();
        Map<Integer, Note> serverMap = serverNotes.stream()
                .collect(Collectors.toMap(Note::getId, note -> note));

        for (Note note : localNotes) {
            int id = note.getId();
            if (id == 0) {
                plan.put(note, "add");
            } else {
                Note serverNote = serverMap.get(id);
                if (serverNote != null && (!note.getText().equals(serverNote.getText()) || !note.getTitle().equals(serverNote.getTitle()))) {
                    plan.put(note, "patch");
                    serverMap.remove(id);
                }
            }
        }
        return plan;
    }

    private void syncToServer(Map<Note, String> plan) {
        for (Map.Entry<Note, String> entry : plan.entrySet()) {
            try {
                switch (entry.getValue()) {
                    case "add" -> client.addNote(entry.getKey());
                    case "patch" -> client.patchNote(entry.getKey());
                }
            } catch (Exception e) {
                System.out.println("Ошибка синхронизации: " + e.getMessage());
            }
        }
    }
}