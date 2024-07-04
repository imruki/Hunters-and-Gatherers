package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.epfl.chacun.GameState.Action.*;

/**
 * Classe principale qui contient le code de création de l'application
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //GET PLAYERS_NAMES
        List<String> playerNames = getParameters().getUnnamed();
        int numberOfPlayers = playerNames.size();

        Preconditions.checkArgument((numberOfPlayers >= 2) && (numberOfPlayers <= 5));

        Map<String, String> namedParams = getParameters().getNamed();

        //SETUP PLAYERS LISTE ET MAP
        List<PlayerColor> playerColors = PlayerColor.ALL.stream()
                .limit(numberOfPlayers)
                .toList();

        Map<PlayerColor, String> playersMap = new HashMap<>();
        for (int i = 0; i < numberOfPlayers; i++)
            playersMap.put(playerColors.get(i), playerNames.get(i));

        //MELANGER LE DECK
        RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.getDefault();
        RandomGenerator randomGenerator;

        if (namedParams.containsKey("seed"))
            randomGenerator = factory.create(Long.parseUnsignedLong(namedParams.get("seed")));
        else
            randomGenerator = factory.create();

        List<Tile> shuffledTiles = new ArrayList<>(Tiles.TILES);
        Collections.shuffle(shuffledTiles, randomGenerator);

        Map<Tile.Kind, List<Tile>> tilesByKind = shuffledTiles.stream()
                .collect(Collectors.groupingBy(Tile::kind));
        TileDecks tileDecks =
                new TileDecks(tilesByKind.get(Tile.Kind.START),
                        tilesByKind.get(Tile.Kind.NORMAL),
                        tilesByKind.getOrDefault(Tile.Kind.MENHIR, List.of()));

        TextMaker textMaker = new TextMakerFr(playersMap);

        GameState initialGameState = GameState.initial(playerColors, tileDecks, textMaker);

        SimpleObjectProperty<GameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);
        SimpleObjectProperty<Rotation> tileToPlaceRotationP =
                new SimpleObjectProperty<>(Rotation.NONE);
        SimpleObjectProperty<Set<Occupant>> visibleOccupantsP =
                new SimpleObjectProperty<>(Set.<Occupant>of());
        SimpleObjectProperty<Set<Integer>> highlightedTilesP =
                new SimpleObjectProperty<>(Set.<Integer>of());
        SimpleObjectProperty<String> textOnTileToPlaceP =
                new SimpleObjectProperty<>("");
        SimpleObjectProperty<List<String>> allActionsP =
                new SimpleObjectProperty<>(List.<String>of());

        ObservableValue<List<MessageBoard.Message>> messagesO = gameStateP.map(g -> g.messageBoard().messages());
        ObservableValue<Tile> tileToPlaceO = gameStateP.map(GameState::tileToPlace);
        ObservableValue<Integer> normalTilesCountO = gameStateP.map(g -> g.tileDecks().normalTiles().size());
        ObservableValue<Integer> menhirTilesCountO = gameStateP.map(g -> g.tileDecks().menhirTiles().size());
        ObservableValue<PlayerColor> currentPlayerO = gameStateP.map(GameState::currentPlayer);
        ObservableValue<GameState.Action> nextActionO = gameStateP.map(GameState::nextAction);

        visibleOccupantsP.bind(gameStateP.map(g -> g.nextAction() != OCCUPY_TILE ? g.board().occupants()
                : Stream.concat(g.board().occupants().stream(), g.lastTilePotentialOccupants().stream())
                        .collect(Collectors.toSet())));

        textOnTileToPlaceP.bind(nextActionO.map(newAction -> switch (newAction) {
            case OCCUPY_TILE -> textMaker.clickToOccupy();
            case RETAKE_PAWN -> textMaker.clickToUnoccupy();
            default -> "";
        }));

        //PLAYERSUI NODE
        Node playersNode = PlayersUI.create(gameStateP, textMaker);

        //DECKSUI NODE
        Node decksNode = DecksUI
                .create(tileToPlaceO,
                        normalTilesCountO,
                        menhirTilesCountO,
                        textOnTileToPlaceP,
                        occupant -> {
                            ActionEncoder.StateAction stateAction = null ;

                            //DETERMINER L'ACTION A EFFECTUER
                            if (nextActionO.getValue() == OCCUPY_TILE) {
                                stateAction = ActionEncoder.withNewOccupant(gameStateP.get(), null);
                            } else if (nextActionO.getValue() == GameState.Action.RETAKE_PAWN) {
                                stateAction = ActionEncoder.withOccupantRemoved(gameStateP.get(), null);
                            }

                            //METTRE A JOUR LE JEU
                            update(stateAction, gameStateP, allActionsP);
                        });

        //MESSAGEBOARD NODE
        Node messageBoardNode = MessageBoardUI
                .create(messagesO, highlightedTilesP);

        //BOARDUI NODE
        Node boardNode = BoardUI
                .create(12,
                        gameStateP,
                        tileToPlaceRotationP,
                        visibleOccupantsP,
                        highlightedTilesP,
                        rotation -> {
                            tileToPlaceRotationP.set(tileToPlaceRotationP.getValue().add(rotation));
                        },

                        selectedPosition -> {
                            if (nextActionO.getValue() == PLACE_TILE) {

                                PlacedTile newPlacedTile = new PlacedTile(tileToPlaceO.getValue(), currentPlayerO.getValue(),
                                        tileToPlaceRotationP.getValue(), selectedPosition);

                                boolean canAddTile = gameStateP.getValue().board().canAddTile(newPlacedTile);
                                ActionEncoder.StateAction stateAction = null;

                                if (canAddTile)
                                    stateAction = ActionEncoder.withPlacedTile(gameStateP.getValue(),
                                        newPlacedTile);

                                //METTRE A JOUR LE JEU
                                update(stateAction, gameStateP, allActionsP);
                            }
                        },

                        selectedOccupant -> {

                            ActionEncoder.StateAction stateAction = null;
                            Set<Occupant> lastTilePotentialOccupants = gameStateP.getValue().lastTilePotentialOccupants();
                            int occupantTileId = Zone.tileId(selectedOccupant.zoneId());
                            PlacedTile occupantTile = gameStateP.getValue().board().tileWithId(occupantTileId);

                            //DETERMINER L'ACTION A EFFECTUER
                            if (nextActionO.getValue() == OCCUPY_TILE
                                    && lastTilePotentialOccupants.contains(selectedOccupant)) {
                                stateAction = ActionEncoder.withNewOccupant(gameStateP.getValue(), selectedOccupant);

                            } else if (nextActionO.getValue() == GameState.Action.RETAKE_PAWN
                                    && selectedOccupant.kind() == Occupant.Kind.PAWN
                                    && occupantTile.placer() == currentPlayerO.getValue()) {
                                stateAction = ActionEncoder.withOccupantRemoved(gameStateP.getValue(), selectedOccupant);
                            }
                            update(stateAction, gameStateP, allActionsP);

                        });

        //ACTIONSUI NODE
        Node actionNode = ActionUI
                .create(allActionsP,
                        actionString -> {
                            ActionEncoder.StateAction stateAction = ActionEncoder.decodeAndApply(gameStateP.getValue(),
                                    actionString);
                            update(stateAction, gameStateP, allActionsP);
                        });


        //SETUP LE JEU
        gameStateP.set(gameStateP.getValue().withStartingTilePlaced());

        //SETUP LA SCENE DU JEU
        VBox vBox = new VBox(actionNode, decksNode);
        BorderPane menuNode = new BorderPane();
        menuNode.setTop(playersNode);
        menuNode.setCenter(messageBoardNode);
        menuNode.setBottom(vBox);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(boardNode);
        rootNode.setRight(menuNode);

        Scene scene = new Scene(rootNode, 1440, 1080);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ChaCuN");

        primaryStage.show();

    }

    /**
     * Methode qui met à jour l'état de jeu ainsi que les actions affichées
     * @param stateAction l'action à effectuer
     * @param gameStateP l'état de jeu
     * @param allActions la liste des actions déja affichées
     */
    private void update(ActionEncoder.StateAction stateAction,SimpleObjectProperty<GameState> gameStateP,
                        SimpleObjectProperty<List<String>> allActions){

        if (stateAction != null && stateAction.gameState() != null && stateAction.actionString() != null) {

            gameStateP.set(stateAction.gameState());

            //METTRE A JOUR ACTIONSUI
            List<String> newActions = new ArrayList<>(allActions.getValue());
            newActions.add(stateAction.actionString());
            allActions.set(newActions);
        }

    }

}
