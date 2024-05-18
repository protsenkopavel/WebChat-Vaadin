package net.protsenko.webchat;

import com.github.rjeschke.txtmark.Processor;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route
public class MainView extends VerticalLayout {

    private final Storage storage;
    private Registration registration;
    private Grid<Storage.ChatMessage> grid;
    private VerticalLayout chat;
    private VerticalLayout login;
    private String user = "";

    public MainView(Storage storage) {
        this.storage = storage;
        buildLogin();
        buildChat();
    }

    private void buildLogin() {
        login = new VerticalLayout() {{
            TextField field = new TextField();
            field.setPlaceholder("Please enter a username");
            add(
                    field,
                    new Button("Login") {{
                        addClickListener(click -> {
                            login.setVisible(false);
                            chat.setVisible(true);
                            user = field.getValue();
                            storage.addRecordJoined(user);
                        });
                        addClickShortcut(Key.ENTER);
                    }}
            );
        }};
        add(login);
    }

    private void buildChat() {
        chat = new VerticalLayout();
        add(chat);
        chat.setVisible(false);

        grid = new Grid<>();
        grid.setItems(storage.getChatMessages());
        grid.addColumn(new ComponentRenderer<>(chatMessage -> new Html(renderRow(chatMessage))))
                .setAutoWidth(true);

        TextField textField = new TextField();
        chat.add(
                new H3("Vaadin chat"),
                grid,
                new HorizontalLayout() {{
                    add(textField,
                            new Button("Send message") {{
                                addClickListener(event -> {
                                    storage.addRecord(user, textField.getValue());
                                    textField.clear();
                                });
                                addClickShortcut(Key.ENTER);
                            }}
                    );
                }}
        );
    }

    public void onMessage(Storage.ChatEvent event) {
        if (getUI().isPresent()) {
            UI ui = getUI().get();
            ui.getSession().lock();
            ui.beforeClientResponse(grid, ctx -> grid.scrollToEnd());
            ui.access(() -> grid.getDataProvider().refreshAll());
            ui.getSession().unlock();
        }
    }

    private static String renderRow(Storage.ChatMessage chatMessage) {
        if (chatMessage.getName().isEmpty()) {
            return Processor.process(String.format("_User **%s** is just joined the chat!_", chatMessage.getMessage()));
        } else {
            return Processor.process(String.format("**%s**: %s", chatMessage.getName(), chatMessage.getMessage()));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        registration = storage.attachListener(this::onMessage);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
    }
}
