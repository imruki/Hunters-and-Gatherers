package ch.epfl.chacun;

import java.util.List;

/**
 * Enumeration qui contient les directions correspondant aux quatre points cardinaux
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public enum Direction {
    N, E, S, W;

    /**
     * Affiche une liste contenant toutes les directions possibles (dans leur ordre de définition)
     */
    public static final List<Direction> ALL = List.of(values());

    /**
     * Affiche la longueur de la liste des directions possibles
     */
    public static final int COUNT = ALL.size();

    /**
     * Renvoie la direction obtenue post-application de la rotation à la tuile
     * @param rotation la rotation recue de la tuile
     * @return la direction obtenue
     */
    public Direction rotated(Rotation rotation) {

        int index = ( this.ordinal() + rotation.quarterTurnsCW() ) % COUNT;

        return ALL.get(index);
    }

    /**
     * Renvoie la direction opposée à celle recue
     * @return la direction opposée à celle recue
     */
    public Direction opposite() {
        return rotated(Rotation.HALF_TURN);
    }

}
