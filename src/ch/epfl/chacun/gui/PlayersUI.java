package ch.epfl.chacun.gui;

import ch.epfl.chacun.GameState;
import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.TextMaker;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;

/**
 * Classe qui contient le code de création de la partie de l'interface graphique qui affiche les informations
 * sur les joueurs
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class PlayersUI {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private PlayersUI() {}

    /**
     * Le nombre maximal de huttes que possède un joueur en début de partie
     */
    private static final int MAX_HUTS = Occupant.occupantsCount(Occupant.Kind.HUT);

    /**
     * Le nombre maximal de pions que possède un joueur en début de partie
     */
    private static final int MAX_PAWNS = Occupant.occupantsCount(Occupant.Kind.PAWN);

    /**
     * Méthode qui retourne le nœud JavaFX à la racine du graphe de scène dont elle est responsable
     * @param gameState0 la version observable de l'état actuel de la partie
     * @param textMaker un générateur de texte
     * @return le nœud JavaFX à la racine du graphe de scène dont elle est responsable
     */
    public static Node create(ObservableValue<GameState> gameState0, TextMaker textMaker) {

        VBox playersVBox = new VBox();
        playersVBox.getStylesheets().add("players.css");
        playersVBox.setId("players");

        //VALEURS OBSERVABLES
        ObservableValue<List<PlayerColor>> listOfPlayers0 = gameState0.map(GameState::players);
        ObservableValue<PlayerColor> currentPlayerO = gameState0.map(GameState::currentPlayer);
        ObservableValue <Map<PlayerColor, Integer>> pointsO = gameState0.map(g -> g.messageBoard().points());

        for (PlayerColor playerColor : listOfPlayers0.getValue()) {

            String playerName = textMaker.playerName(playerColor);

            TextFlow playerFlow = new TextFlow();
            playerFlow.getStyleClass().add("player");

            currentPlayerO.addListener( (o, oldPlayer, newPlayer) -> {
                if (newPlayer == playerColor) {
                    playerFlow.getStyleClass().add("current");
                } else {
                    playerFlow.getStyleClass().remove("current");
                }
            });

                //CIRCLE
            Circle playerCircle = new Circle(5, ColorMap.fillColor(playerColor));

                //TEXT FOR POINTS
            ObservableValue<String> pointsTextO =
                    pointsO.map(p -> STR." \{playerName} : \{textMaker.points(p.getOrDefault(playerColor,
                            0))}\n");

            Text pointsText = new Text();
            pointsText.textProperty().bind(pointsTextO);

            //OCCUPANTS
            SVGPath[] hutsTab = createOccupantNodes(gameState0, playerColor, Occupant.Kind.HUT, MAX_HUTS);
            SVGPath[] pawnsTab = createOccupantNodes(gameState0, playerColor, Occupant.Kind.PAWN, MAX_PAWNS);

            Text spaceBetweenOccupants = new Text("   ");

            playerFlow.getChildren().addAll(playerCircle, pointsText);

            for (int i = 0; i < MAX_HUTS; i++)
                playerFlow.getChildren().add(hutsTab[i]);

            playerFlow.getChildren().add(spaceBetweenOccupants);

            for (int i = 0; i < MAX_PAWNS; i++)
                playerFlow.getChildren().add(pawnsTab[i]);

            playersVBox.getChildren().add(playerFlow);

        }

        return playersVBox;
    }

    /**
     * Méthode qui retourne un tableau de nœuds JavaFX correspondants à l'affichage des occupants d'un joueur
     * @param gameState0 la version observable de l'état actuel de la partie
     * @param playerColor couleur d'un joueur
     * @param kind type d'occupant
     * @return un tableau de nœuds JavaFX correspondants à l'affichage des occupants d'un type donné d'un joueur
     */
    private static SVGPath[] createOccupantNodes(ObservableValue<GameState> gameState0, PlayerColor playerColor,
                                                 Occupant.Kind kind, int nbNodes) {

        SVGPath[] nodesTab = new SVGPath[nbNodes];

        ObservableValue<Double> opacity0;

        // pour que les icônes des pions les plus à droite de sa ligne soient quasi-transparentes en premier
        for (int i = nbNodes-1; i >=0 ; i--) {
            nodesTab[i] = (SVGPath) Icon.newFor(playerColor, kind);

            int index = i;
            opacity0 = gameState0
                    .map(g -> (g.freeOccupantsCount(playerColor, kind) > index) ? 1.0 : 0.1);

            nodesTab[index].opacityProperty().bind(opacity0);
        }

        return nodesTab;
    }

}