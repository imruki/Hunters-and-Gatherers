package ch.epfl.chacun;

import java.util.Objects;

/**
 * Enregristrement qui représente un occupant d'une zone
 * @param kind la sorte de l'occupant
 * @param zoneId l'identifiant de la zone de l'occupant
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record Occupant(Kind kind, int zoneId) {

    /**
     * Enumeration qui contient les types d'occupants
     */
    public enum Kind { PAWN, HUT }

    /**
     * Constructeur compact validant les arguments qui lui sont passés
     * @throws NullPointerException si kind est null
     * @throws IllegalArgumentException si zoneId est strictement négatif
     */
    public Occupant {
        Objects.requireNonNull(kind);
        Preconditions.checkArgument(zoneId >= 0);
    }

    /**
     * Méthode qui retourne le nombre d'occupants du type donné que possède un joueur
     * @param kind le type d'occupant donné
     * @return le nombre d'occupants correspondant accordé au joueur
     */
    public static int occupantsCount(Kind kind) {
        return switch (kind) {
            case PAWN -> 5;
            case HUT -> 3;
        };
    }

}
