package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Tile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * Classe qui contient le code de création de la partie de l'interface graphique qui affiche les tas de tuiles
 * ainsi que la tuile à poser
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class DecksUI {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private DecksUI() {}

    /**
     * Méthode qui retourne le nœud JavaFX correspondant aux tas de tuiles et à la tuile à poser
     * @param tileToPlace0 la version observable de la tuile à placer
     * @param normalTilesCount0 la version observable du nombre de tuiles restantes dans le tas des tuiles normales
     * @param menhirTilesCount0 la version observable du nombre de tuiles restantes dans le tas des tuiles menhir
     * @param textOnTileToPlace0 la version observable du texte à afficher à la place de la tuile à placer
     * @param eventHandler un gestionnaire d'événement destiné à être appelé lorsque le joueur courant signale qu'il
     * ne désire pas poser ou reprendre un occupant (en cliquant sur le texte affiché à la place de la prochaine tuile)
     * @return le nœud JavaFX correspondant aux tas de tuiles et à la tuile à poser
     */
    public static Node create(ObservableValue<Tile> tileToPlace0, ObservableValue<Integer> normalTilesCount0,
                              ObservableValue<Integer> menhirTilesCount0, ObservableValue<String> textOnTileToPlace0,
                              Consumer<Occupant> eventHandler) {

        VBox decksVBox = new VBox();
        decksVBox.getStylesheets().add("decks.css");

        StackPane stackPaneToPlace = new StackPane();
        stackPaneToPlace.setId("next-tile");

        // image de la tuile à placer
        ImageView tiletoPlaceView = new ImageView();
        ObservableValue<Image> tileToPlaceImageO = tileToPlace0.map(t -> ImageLoader.largeImageForTile(t.id()));
        tiletoPlaceView.imageProperty().bind(tileToPlaceImageO);

        tiletoPlaceView.setFitWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
        tiletoPlaceView.setFitHeight(ImageLoader.LARGE_TILE_FIT_SIZE);

        // texte à afficher sur la tuile à placer
        Text textOnTileToPlace = new Text();
        textOnTileToPlace.setWrappingWidth(0.8 * ImageLoader.LARGE_TILE_FIT_SIZE);

        textOnTileToPlace.textProperty().bind(textOnTileToPlace0);
        textOnTileToPlace.visibleProperty().bind(textOnTileToPlace0
                .map(t -> !t.isEmpty()));

        textOnTileToPlace.setOnMouseClicked(event -> eventHandler.accept(null));
        textOnTileToPlace0.addListener((T,oldText,newText) -> {
            tiletoPlaceView.visibleProperty().set(newText.isEmpty());
        });

        stackPaneToPlace.getChildren().addAll(tiletoPlaceView, textOnTileToPlace);

        HBox decksHBox = new HBox();
        decksHBox.setId("decks");

        // NORMAL DECK
        StackPane stackPaneNormal = new StackPane();

        // image
        ImageView normalTileImageView = new ImageView();
        normalTileImageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        normalTileImageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        normalTileImageView.setId("NORMAL");

        stackPaneNormal.getChildren().add(normalTileImageView);

        // text
        Text normalTilesCountText = new Text();
        normalTilesCountText.textProperty().bind(normalTilesCount0
                .map(String::valueOf));

        stackPaneNormal.getChildren().add(normalTilesCountText);

        // MENHIR DECK
        StackPane stackPaneMenhir = new StackPane();

        // image
        ImageView menhirTileImageView = new ImageView();
        menhirTileImageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        menhirTileImageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        menhirTileImageView.setId("MENHIR");

        stackPaneMenhir.getChildren().add(menhirTileImageView);

        //text
        Text menhirTilesCountText = new Text();
        menhirTilesCountText.textProperty().bind(menhirTilesCount0
                .map(String::valueOf));

        stackPaneMenhir.getChildren().add(menhirTilesCountText);

        decksHBox.getChildren().addAll(stackPaneNormal, stackPaneMenhir);
        decksVBox.getChildren().addAll(decksHBox, stackPaneToPlace);

        return decksVBox;

    }
}

