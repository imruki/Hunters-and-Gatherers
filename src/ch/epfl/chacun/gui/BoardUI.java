package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Consumer;

import static javafx.scene.paint.Color.*;

/**
 * Classe qui contient le code de création de la partie de l'interface graphique qui affiche le plateau de jeu
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class BoardUI {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private BoardUI() {}

    /**
     * Méthode qui retourne le nœud JavaFX correspondant au plateau du jeu
     *
     * @param reach la portée du plateau à créer
     * @param gameStateO la version observable de l'état du jeu
     * @param rotationO la version observable de la rotation à appliquer à la tuile à placer
     * @param visibleOccupantsO la version observable des occupants visibles
     * @param tileIdsO la version observable des tuiles mises en évidence
     * @param rotationHandler un gestionnaire d'événement à appeler lorsque le joueur courant désire effectuer
     * une rotation de la tuile à placer, soit, quand il effectue un clic droit sur une case de la frange
     * @param positionHandler un gestionnaire d'événement à appeler lorsque le joueur courant désire poser
     * la tuile à placer, soit, quand il effectue un clic gauche sur une case de la frange
     * @param occupantHandler un gestionnaire d'événement à appeler lorsque le joueur courant sélectionne un occupant,
     * soit, quand il clique sur l'un d'entre eux
     * @return le nœud JavaFX correspondant au plateau du jeu
     */
    public static Node create(int reach, ObservableValue<GameState> gameStateO, ObservableValue<Rotation> rotationO,
                              ObservableValue<Set<Occupant>> visibleOccupantsO,
                              ObservableValue<Set<Integer>> tileIdsO, Consumer<Rotation> rotationHandler,
                              Consumer<Pos> positionHandler, Consumer<Occupant> occupantHandler) {

        Preconditions.checkArgument(reach > 0);

        ScrollPane boardScrollPane = new ScrollPane();
        boardScrollPane.getStylesheets().add("board.css");
        boardScrollPane.setId("board-scroll-pane");

        GridPane boardGrid = new GridPane();
        boardGrid.setId("board-grid");

        //VALEURS OBSERVABLES DU GAMESTATE
        ObservableValue<PlayerColor> currentPlayerO = gameStateO.map(GameState::currentPlayer);
        ObservableValue<Board> boardO = gameStateO.map(GameState::board);
        ObservableValue<Set<Animal>> cancelledAnimalsO = boardO.map(Board::cancelledAnimals);
        ObservableValue<Set<Pos>> fringeO = boardO.map(Board::insertionPositions);
        ObservableValue<Tile> tileToPlaceO = gameStateO.map(GameState::tileToPlace);

        //INITIALISER TOUTES LES CASES
        for (int x = -reach; x <= reach; x++) {
            for (int y = -reach; y <= reach; y++) {
                Pos tilePos = new Pos(x, y);
                ObservableValue<PlacedTile> tileAtPosO = boardO.map(b -> b.tileAt(tilePos));

                Group boardGroup = new Group();

                //CADRE
                ImageView tileView = new ImageView();
                tileView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                tileView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);

                boardGroup.getChildren().add(tileView);

                //AJOUTER LE VOILE AUX CASES
                ObjectProperty<ColorInput> veilInput = new SimpleObjectProperty<>();
                Blend blend = new Blend(BlendMode.SRC_OVER);
                blend.setOpacity(0.5);
                boardGroup.setEffect(blend);
                blend.topInputProperty().bind(veilInput);
                veilInput.set(new ColorInput(0, 0, ImageLoader.NORMAL_TILE_FIT_SIZE,
                        ImageLoader.NORMAL_TILE_FIT_SIZE, null));

                ObjectProperty<Color> veilColor = new SimpleObjectProperty<>(TRANSPARENT);
                ObjectProperty<Image> tileImage = new SimpleObjectProperty<>(emptyImage());

                //INITIALISATION CELLDATA DE LA CASE
                ObservableValue<CellData> cellDataO = Bindings
                    .createObjectBinding(() -> {

                        //SI LA TUILE EST POSéE
                        if (tileAtPosO.getValue() != null) {
                            int tileId = tileAtPosO.getValue().id();
                            tileImage.set(CellData.imageCache.computeIfAbsent(tileId, ImageLoader::normalImageForTile));

                            //SI LA TUILE N'EST PAS MISE EN EVIDENCE
                            if (!tileIdsO.getValue().contains(tileId) && !tileIdsO.getValue().isEmpty())
                                veilColor.set(BLACK);
                            else
                                veilColor.set(TRANSPARENT);
                        }
                        //SI LA TUILE N'EST PAS POSéE
                        else {
                            if (!fringeO.getValue().contains(tilePos)) {
                                boardGroup.setOnMouseClicked(null);
                            }
                            //SI LA TUILE FAIT PARTIE DE LA FRINGE
                            else {
                                boardGroup.setOnMouseClicked(event -> {
                                    if (event.isStillSincePress()) {
                                        //LEFT CLICK
                                        if (event.getButton() == MouseButton.PRIMARY)
                                            positionHandler.accept(tilePos);
                                            //RIGHT CLICK
                                        else if (event.getButton() == MouseButton.SECONDARY) {
                                            if (event.isAltDown())
                                                rotationHandler.accept(Rotation.RIGHT);
                                            else
                                                rotationHandler.accept(Rotation.LEFT);
                                        }
                                    }
                                });
                                if (currentPlayerO.getValue() != null && tileToPlaceO.getValue() != null){
                                    //SI LA CASE EST SURVOLéE
                                    if (boardGroup.isHover()){

                                        PlacedTile placedTile = new PlacedTile(tileToPlaceO.getValue(),
                                                currentPlayerO.getValue(), rotationO.getValue(), tilePos, null);

                                        boolean canAddTile = boardO.getValue().canAddTile(placedTile);
                                        int tileToPlaceId = tileToPlaceO.getValue().id();
                                        tileImage.set(CellData.imageCache.computeIfAbsent(tileToPlaceId,
                                                ImageLoader::normalImageForTile));

                                        if (!canAddTile)
                                            veilColor.set(WHITE);
                                        else
                                            veilColor.set(TRANSPARENT);
                                    }
                                    //SI LA CASE NEST PAS SURVOLéE
                                    else {
                                        veilColor.set(ColorMap.fillColor(currentPlayerO.getValue()));
                                        tileImage.set(emptyImage());
                                    }
                                }
                            }
                        }


                        return new CellData(tileImage.getValue(), rotationO.getValue().degreesCW(), veilColor.getValue());
                    },
                            //ON A OPTé POUR PLUS DE PARAMETRES AU LIEU DE METTRE GAMESTATEO POUR PLUS DE LISIBILITé
                            rotationO, fringeO, boardO, tileToPlaceO, currentPlayerO, tileIdsO
                            , tileAtPosO, boardGroup.hoverProperty());

                //BIND LES ATTRIBUTS DE CELLDATA AU GROUPE
                tileView.imageProperty().bind(cellDataO.map(cd -> cd.tileImage));
                veilInput.get().paintProperty().bind(cellDataO.map(cd -> cd.veilColor));

                //AUDITEUR DE LA TUILE
                tileAtPosO.addListener((o, oldTile, newTile) -> {

                    //INITIALISATION SI LA TUILE VIENT  D'ETRE PLACéE (S'EFFECTUE QU'UNE SEULE FOIS)
                    if (oldTile == null) {

                        //FIXER LA ROTATION UNE FOIS LA TUILE POSéE
                        boardGroup.rotateProperty().unbind();
                        boardGroup.rotateProperty().set(newTile.rotation().degreesCW());

                        //RECUPERER LES ANIMAUX DE LA TUILES
                        List<Animal> tileAnimals = new ArrayList<>();
                        newTile.meadowZones().forEach(zone -> tileAnimals.addAll(zone.animals()));

                        //AJOUTER LES JETONS D'ANNULATIONS POUR CHAQUE ANIMAL
                        for (Animal animal : tileAnimals) {
                            ImageView marker = new ImageView();
                            marker.setId(STR."marker_\{animal.id()}");
                            marker.getStyleClass().add("marker");

                            marker.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
                            marker.setFitHeight(ImageLoader.MARKER_FIT_SIZE);

                            marker.visibleProperty().bind(cancelledAnimalsO.map(set -> set.contains(animal)));

                            boardGroup.getChildren().add(marker);
                        }

                        //INITIALISER LES OCCUPANTS
                        for (Occupant occupant : newTile.potentialOccupants()) {
                            Node occupantPath = Icon.newFor(currentPlayerO.getValue(), occupant.kind());

                            if (occupant.kind() == Occupant.Kind.PAWN)
                                occupantPath.setId(STR."pawn_\{occupant.zoneId()}");
                            else
                                occupantPath.setId(STR."hut_\{occupant.zoneId()}");

                            occupantPath.setOnMouseClicked(event -> {
                                if (event.isStillSincePress())
                                    occupantHandler.accept(occupant);
                            });
                            occupantPath.rotateProperty().set(newTile.rotation().negated().degreesCW());

                            occupantPath.visibleProperty().bind(visibleOccupantsO.map(set -> set.contains(occupant)));

                            boardGroup.getChildren().add(occupantPath);
                        }
                    }
                });

                boardGroup.rotateProperty().bind(cellDataO.map(cd -> cd.rotation));
                boardGrid.add(boardGroup, x + reach, y + reach, 1, 1);
            }
        }

        boardScrollPane.setContent(boardGrid);

        //POUR CENTRER LE BOARD
        boardScrollPane.setHvalue(0.5);
        boardScrollPane.setVvalue(0.5);

        return boardScrollPane;

    }

    private static Image emptyImage(){
        WritableImage emptyTileImage = new WritableImage(1, 1);
        emptyTileImage
                .getPixelWriter()
                .setColor(0, 0, Color.gray(0.98));
        return emptyTileImage;
    }

    private record CellData(Image tileImage, int rotation, Color veilColor){
        private static final Map<Integer, Image> imageCache = new HashMap<>();
    }

}


