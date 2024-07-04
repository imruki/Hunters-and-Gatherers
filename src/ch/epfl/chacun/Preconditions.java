package ch.epfl.chacun;

/**
 * Classe qui valide les arguments passés aux méthodes
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class Preconditions {

    /**
     * Constructeur empêchant l'instanciation de la classe
     */
    private Preconditions() {}

    /**
     * Vérifie la validité des arguments d'une méthode
     * @param shouldBeTrue condition que l'argument doit respecter
     * @throws IllegalArgumentException si la condition n'est pas respectée
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue)
            throw new IllegalArgumentException("la condition n'est pas respectée");
    }

}
