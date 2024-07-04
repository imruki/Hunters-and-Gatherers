package ch.epfl.chacun;

import java.util.List;

/**
 * Enumeration qui contient les couleurs associées aux joueurs
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public enum PlayerColor {
    RED, BLUE, GREEN, YELLOW, PURPLE;

    /**
     * Méthode qui affiche une liste contenant toutes les couleurs associées aux joueurs
     * (dans leur ordre de définition)
     */
    public static final List<PlayerColor> ALL = List.of(values());

}
