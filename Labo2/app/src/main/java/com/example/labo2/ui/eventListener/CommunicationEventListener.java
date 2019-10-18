package com.example.labo2.ui.eventListener;

import java.util.EventListener;

public interface CommunicationEventListener extends EventListener {
    public boolean handleServerResponse(String response);
}
