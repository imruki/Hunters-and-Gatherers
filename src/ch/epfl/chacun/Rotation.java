package ch.epfl.chacun;

import java.util.List;

/**
 * Enumeration qui contient les quatre rotations qu'il est possible d'appliquer à une tuile avant de la poser sur le plateau
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public enum Rotation {
    NONE, RIGHT, HALF_TURN, LEFT;

    /**
     * Méthode qui affiche une liste contenant toutes les rotations possibles (dans leur ordre de définition)
     */
    public static final List<Rotation> ALL = List.of(values());

    /**
     * Méthode qui affiche la longueur de la liste des rotations possibles de la tuile
     */
    public static final int COUNT = ALL.size();

    /**
     * Méthode qui renvoie le nombre de quarts de tours correspondant à la rotation recue (dans le sens horaire)
     * de la tuile
     * @return le nombre de quarts de tours correspondant à la rotation recue de la tuile
     */
    public int quarterTurnsCW(){
        return ordinal();
    }

    /**
     * Méthode qui retourne l'angle (en degrés) correspondant à la rotation recue (dans le sens horaire) de la tuile
     * @return l'angle correspondant à la rotation recue de la tuile
     */
    public int degreesCW(){
        return quarterTurnsCW()*90;
    }

    /**
     * Méthode qui renvoie la somme de la rotation recue de la tuile et de celle ajoutée
     * @param that la rotation recue de la tuile
     * @return la rotation finale de la tuile
     */
    public Rotation add(Rotation that){
        int index = (this.quarterTurnsCW() + that.quarterTurnsCW()) % COUNT;
        return ALL.get(index);
    }

    /**
     * Méthode qui renvoie la négation de la rotation recue de la tuile
     * @return la rotation à appliquer à la tuile pour obtenir la rotation nulle
     */
    public Rotation negated(){
        int index = ( COUNT - quarterTurnsCW() ) % COUNT;
        return ALL.get(index);
    }

}