package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

/**
 * Classe qui permet d'obtenir les nœuds représentant les occupants des différents joueurs
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public final class Icon {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private Icon() {}

    /**
     * Retourne le nœud représentant le type d'occupant donné
     * @param playerColor couleur du joueur donné
     * @param kind type d'occupant donné
     * @return le nœud représentant le type d'occupant donné
     */
    public static Node newFor(PlayerColor playerColor, Occupant.Kind kind) {

        SVGPath svgPath = new SVGPath();

        String svgCode = switch (kind) {
            case PAWN -> "M -10 10 H -4 L 0 2 L 6 10 H 12 L 5 0 L 12 -2 L 12 -4 L 6 -6 L 6 -10 L 0 -10 L -2 -4 L -6 " +
                    "-2 L -8 -10 L -12 -10 L -8 6 Z";
            case HUT -> "M -8 10 H 8 V 2 H 12 L 0 -10 L -12 2 H -8 Z";
        };

        svgPath.setContent(svgCode);
        svgPath.setFill(ColorMap.fillColor(playerColor));
        svgPath.setStroke(ColorMap.strokeColor(playerColor));

        return svgPath;
    }

}