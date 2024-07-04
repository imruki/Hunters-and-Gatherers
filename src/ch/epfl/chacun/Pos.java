package ch.epfl.chacun;

/**
 * Enregistrement qui représente la position d'une case du plateau de jeu
 * @param x la coordonnée x de la position
 * @param y la coordonnée y de la position
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public record Pos(int x, int y) {

    /**
     * Position de l'origine (case centrale du plateau de jeu)
     */
    public static final Pos ORIGIN = new Pos(0,0);

    /**
     * Méthode qui retourne la position obtenue par translation de la position recue
     * @param dX translation selon la coordonnée x
     * @param dY translation selon la coordonnée y
     * @return la position finale de la case
     */
    public Pos translated(int dX, int dY) {
        return new Pos(x + dX, y + dY);
    }

    /**
     * Méthode qui retourne la position voisine de la position donnée dans une direction donnée
     * @param direction la direction donnée
     * @return la position voisine
     */
    public Pos neighbor(Direction direction){

        return switch (direction){
            case N -> translated(0, -1);
            case E -> translated(1, 0);
            case S -> translated(0, 1);
            case W -> translated(-1, 0);
        };
    }

}
