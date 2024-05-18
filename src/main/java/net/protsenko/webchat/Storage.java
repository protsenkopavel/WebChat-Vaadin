package net.protsenko.webchat;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Storage {
    @Getter
    private Queue<ChatMessage> chatMessages = new ConcurrentLinkedQueue<>();

    private ComponentEventBus eventBus = new ComponentEventBus(new Div());

    @Getter
    @AllArgsConstructor
    public static class ChatMessage {
        public String name;
        public String message;
    }

    public static class ChatEvent extends ComponentEvent<Div> {

        public ChatEvent() {
            super(new Div(), false);
        }
    }

    public void addRecord(String user, String message) {
        chatMessages.add(new ChatMessage(user, message));
        eventBus.fireEvent(new ChatEvent());
    }

    public void addRecordJoined(String user) {
        chatMessages.add(new ChatMessage("", user));
        eventBus.fireEvent(new ChatEvent());
    }

    public Registration attachListener(ComponentEventListener<ChatEvent> listener) {
        return eventBus.addListener(ChatEvent.class, listener);
    }

    public int size() {
        return chatMessages.size();
    }
}
