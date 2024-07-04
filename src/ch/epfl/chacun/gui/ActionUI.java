package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import ch.epfl.chacun.Preconditions;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Classe qui contient le code de création de l'interface graphique permettant le jeu à distance
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class ActionUI {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private ActionUI() {}

    /**
     * Méthode qui retourne le nœud JavaFX correspondant à l'affichage du texte contenant les actions du jeu
     * @param allActionsList0 la liste observable de la représentation en base32 de toutes les actions effectuées
     * depuis le début de la partie
     * @param stringHandler un gestionnaire d'événement destiné à être appelé avec la représentation en base32
     * d'une action, qui doit être effectuée si elle est valide
     * @return le nœud JavaFX correspondant à l'affichage du texte contenant les actions du jeu
     */
    public static Node create(ObservableValue<List<String>> allActionsList0, Consumer<String> stringHandler) {

        HBox actionsHBox = new HBox();
        actionsHBox.getStylesheets().add("actions.css");
        actionsHBox.setId("actions");

        Text actionsText = new Text();
        actionsText.textProperty().bind(allActionsList0.map(ActionUI::lastFourActions));
        actionsHBox.getChildren().add(actionsText);

        TextField actionFieldText = new TextField();
        actionFieldText.setId("action-field");

        actionFieldText.setTextFormatter(
                new TextFormatter<> (change -> {

                    String changeText = change.getText();

                    if (change.isAdded()) {
                        String formattedChange = changeText.chars()
                                // convertit les lettres minuscules en majuscules
                                .map(c -> Character.isLowerCase(c) ? Character.toUpperCase(c) : c)
                                .mapToObj(c -> String.valueOf((char) c))
                                .collect(Collectors.joining());
                        change.setText(formattedChange);

                        try {
                            // vérifie si le texte est valide après la modification
                            Preconditions.checkArgument(Base32.isValid(formattedChange));
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    }

                    return change;
                }));

        actionFieldText.setOnAction(event -> {
            stringHandler.accept(actionFieldText.getText());
            actionFieldText.clear();
        });

        actionsHBox.getChildren().add(actionFieldText);
        return actionsHBox;
    }

    /**
     * Méthode qui retourne le texte constitué de la représentation en base32 des 4 dernières actions du jeu
     * @param list la liste de la représentation en base32 de toutes les actions effectuées depuis le début de
     * la partie
     */
    private static String lastFourActions(List<String> list) {

        int listSize = list.size();
        int startIndex = listSize < 4 ? 0 : listSize - 4;

        List<String> lastFourActionsIndexed = IntStream.range(startIndex, listSize)
            .mapToObj(i -> STR."\{i + 1}:\{list.get(i)}")
            .collect(Collectors.toList());

        return String.join(", ", lastFourActionsIndexed);
    }

}

