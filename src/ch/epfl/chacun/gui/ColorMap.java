package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

/**
 * Classe qui détermine les couleurs JavaFX à utiliser pour représenter à l'écran les cinq couleurs de joueur du jeu
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class ColorMap {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private ColorMap() {
    }

    /**
     * Retourne la couleur JavaFX à utiliser pour remplir, entre autres, les occupants du joueur donné
     * @param playerColor couleur du joueur donné
     * @return la couleur JavaFX à utiliser pour remplir, entre autres, les occupants du joueur donné
     */
    public static Color fillColor(PlayerColor playerColor) {
        return switch (playerColor) {
            case RED -> Color.RED;
            case BLUE -> Color.BLUE;
            case GREEN -> Color.LIME;
            case YELLOW -> Color.YELLOW;
            case PURPLE -> Color.PURPLE;
        };
    }

    /**
     * Retourne la couleur JavaFX à utiliser pour dessiner, entre autres, le contour des occupants du joueur donné
     * @param playerColor couleur du joueur donné
     * @return la couleur JavaFX à utiliser pour dessiner, entre autres, le contour des occupants du joueur donné
     */
    public static Color strokeColor(PlayerColor playerColor) {
        return switch (playerColor) {
            case YELLOW, GREEN -> fillColor(playerColor).deriveColor(0, 1, 0.6,
                    1);
            default -> Color.WHITE;
        };
    }

}
