package ch.epfl.chacun;

/**
 * Enregistrement qui représente un animal situé dans un pré
 * @param id identifiant de l'animal
 * @param kind type de l'animal
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record Animal(int id, Kind kind) {

    /**
     * Enumèration qui contient les differents types d'animaux
     */
    public enum Kind { MAMMOTH, AUROCHS, DEER, TIGER }

    /**
     * Méthode qui retourne l'identifiant de la tuile sur laquelle se trouve l'animal
     * @return le nombre entier correspondant à l'identifiant de la tuile sur laquelle se trouve l'animal
     */
   public int tileId() {
       return Zone.tileId(id / 10);
    }

}
