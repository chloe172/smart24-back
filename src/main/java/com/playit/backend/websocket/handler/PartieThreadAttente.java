package com.playit.backend.websocket.handler;

import java.util.HashMap;
import java.util.Map;

import com.playit.backend.metier.model.Partie;

public class PartieThreadAttente {

    private static final Map<Long, Thread> threads = new HashMap<>();

    public static void addThread(Partie partie, Thread thread) {
        Long idPartie = partie.getId();
        threads.put(idPartie, thread);
    }

    public static void stopThread(Partie partie) {
        Long idPartie = partie.getId();
        Thread thread = threads.get(idPartie);
        if (thread != null) {
            thread.interrupt();
        }
        threads.remove(idPartie);
    }

}
