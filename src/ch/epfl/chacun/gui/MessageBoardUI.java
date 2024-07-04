package ch.epfl.chacun.gui;

import ch.epfl.chacun.MessageBoard;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;

/**
 * Classe qui contient le code de création de l'interface graphique du tableau d'affichage
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public final class MessageBoardUI {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private MessageBoardUI() {}

    /**
     * Méthode qui retourne le nœud JavaFX correspondant au panneau de défilement des messages du jeu
     * @param messages0 la version observable de la liste des messages affichés sur le tableau d'affichage
     * @param tilesIdProperty JavaFX contenant l'ensemble des identités des tuiles à mettre en évidence
     * sur le plateau
     * @return le nœud JavaFX correspondant au panneau de défilement des messages du jeu
     */
    public static Node create(ObservableValue<List<MessageBoard.Message>> messages0,
                              ObjectProperty<Set<Integer>> tilesIdProperty) {

        ScrollPane messageScrollPane = new ScrollPane();
        messageScrollPane.getStylesheets().add("message-board.css");
        messageScrollPane.setId("message-board");

        VBox messageVBox = new VBox();

        messages0.addListener((o, oldMessages, newMessages) -> {
            if (newMessages != null && newMessages != oldMessages) {
                newMessages.stream()

                    // l'ancienne liste de message constitue un préfixe de la nouvelle
                    .skip(oldMessages.size())
                    .forEach(message -> {
                        Text messageText = new Text(message.text());
                        messageText.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE);

                        messageVBox.getChildren().add(messageText);

                        messageText.setOnMouseEntered(event -> tilesIdProperty.set(message.tileIds()));
                        messageText.setOnMouseExited(event -> tilesIdProperty.set(Collections.emptySet()));

                    });

                messageScrollPane.layout();
                messageScrollPane.setVvalue(1);
            }
        });

        messageScrollPane.setContent(messageVBox);
        return messageScrollPane;

    }

}

