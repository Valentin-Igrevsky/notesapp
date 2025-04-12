package org.client;

import org.client.http.AuthClient;
import org.client.http.NotesClient;
import org.client.models.Note;
import org.client.models.User;

import java.util.List;
import java.util.Map;

public class Main {
    static AuthClient authClient = new AuthClient();
    static NotesClient notesClient = new NotesClient();
    static User testUser = new User(13, "new", "1", "111", "111");
//    static Note testNote = new Note(testUser, "txtNote_1", "tltNote_1");

    public static void main(String[] args) throws Exception {
//        testReg();
//        testLogin();
//        testAddNote();
//        testGetNote();
//        testGetNotes();
//        testDeleteNote();
//        testPatchNote();
    }


    private static void testReg() throws Exception {
        Integer status = authClient.register(testUser);
        if (status == 201) {
            System.out.println("Успешно");
        } else {
            System.out.println("[testReg][StatusCode]: " + status);
        }
    }

    private static void testLogin() throws Exception {
        Map.Entry<Integer, User> res = authClient.login("111", "111");

        int status = res.getKey();
        User user = res.getValue();

        if (status == 200) {
            System.out.println(user);
        } else {
            System.out.println("[testLogin][StatusCode]: " + status);
        }
    }

    private static void testAddNote() throws Exception {
        Map.Entry<Integer, User> resUser = authClient.login("222", "222");
        User user = resUser.getValue();

        Note testNote = new Note(user, "txtNote_4_2", "tltNote_4_2");

        Map.Entry<Integer, Note> res = notesClient.addNote(testNote);

        int status = res.getKey();
        Note note = res.getValue();

        if (status == 200) {
            System.out.println(note);
        } else {
            System.out.println("[testAddNote][StatusCode]: " + status);
        }
    }

    private static void testGetNote() throws Exception {
        Map.Entry<Integer, List<Note>> res = notesClient.getNoteById(14, 32);

        int status = res.getKey();
        List<Note> notes = res.getValue();

        if (status == 200) {
            System.out.println(notes);
        } else {
            System.out.println("[testGetNote][StatusCode]: " + status);
        }
    }

    private static void testGetNotes() throws Exception {
        Map.Entry<Integer, List<Note>> res = notesClient.getAllNotes(15);

        int status = res.getKey();
        List<Note> notes = res.getValue();

        if (status == 200) {
            System.out.println(notes);
        } else {
            System.out.println("[testGetNotes][StatusCode]: " + status);
        }
    }

    private static void testDeleteNote() throws Exception {
        Integer status = notesClient.deleteNoteById(13, 27);
        if (status == 200) {
            System.out.println("Успешно");
        } else {
            System.out.println("[testDelete][StatusCode]: " + status);
        }
    }

    private static void testPatchNote() throws Exception {
        Map.Entry<Integer, List<Note>> res = notesClient.getNoteById(14, 29);
        List<Note> notes = res.getValue();
        Note note = notes.getFirst();
        note.setText("NEW TEXT");
        note.setTitle("NEW TITLE");

        Integer status = notesClient.patchNote(note);
        if (status == 200) {
            System.out.println("Успешно");
        } else {
            System.out.println("[testPatch][StatusCode]: " + status);
        }
    }
}