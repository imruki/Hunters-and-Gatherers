package ch.epfl.chacun;

import java.util.List;

/**
 * Interface scellée qui représente une zone d'une tuile
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public sealed interface Zone {

    /**
     * Enumeration qui contient les 6 pouvoirs spéciaux qu'une zone peut posséder
     */
    enum SpecialPower{
        SHAMAN, LOGBOAT, HUNTING_TRAP, PIT_TRAP, WILD_FIRE, RAFT
    }

    /**
     * Méthode qui retourne l'identifiant de la tuile contenant la zone donnee
     * @param zoneId l'identifiant de la zone
     * @return l'identifiant de la tuile contenant la zone donnee
     */
    static int tileId(int zoneId){
        return (zoneId/10);
    }

    /**
     * Méthode qui retourne l'identifiant local de la zone donnee
     * @param zoneId l'identifiant de la zone
     * @return l'identifiant local de la zone donnee
     */
    static int localId(int zoneId){
        return (zoneId % 10);
    }

    /**
     * Méthode qui retourne l'identifiant de la zone donnee
     * @return l'identifiant de la zone donnee
     */
    int id();

    /**
     * Méthode qui retourne l'identifiant de la tuile contenant la zone donnee
     * @return l'identifiant de la tuile contenant la zone donnee
     */
    default int tileId() {
        return tileId(id());
    }

    /**
     * Méthode qui retourne l'identifiant local de la zone donnee (entre 0 et 9)
     * @return l'identifiant de local de la zone donnee
     */
    default int localId() {
        return localId(id());
    }

    /**
     * Méthode qui retourne le pouvoir spécial de la zone donnee
     * @return le pouvoir spécial de la zone donnee
     */
    default SpecialPower specialPower() {
        return null;
    }

    /**
     * Enregistrement qui représente une zone de type forêt
     * @param id l'identifiant de la zone
     * @param kind le type de forêt dont il s'agit
     */
    record Forest(int id, Kind kind) implements Zone {

        /**
         * Enumeration qui contient les 3 types de zone foret
         */
        public enum Kind { PLAIN, WITH_MENHIR, WITH_MUSHROOMS }

    }

    /**
     * Enregistrement qui représente une zone de type pré
     * @param id l'identifiant de la zone
     * @param animals la liste des animaux contenus dans le pré
     * @param specialPower l'éventuel pouvoir spécial du pré
     */
    record Meadow(int id, List<Animal> animals, SpecialPower specialPower) implements Zone {

        /**
         * Constructeur compact copiant la liste des animaux reçue
         */
        public Meadow {
            animals = List.copyOf(animals);
        }
    }

    /**
     * Interface scellée qui représente une zone aquatique (rivière/lac)
     */
    sealed interface Water extends Zone {

        /**
         * Méthode qui retourne le nombre de poissons nageant dans la zone
         * @return le nombre de poissons nageant dans la zone
         */
        int fishCount();
    }

    /**
     * Enregistrement qui représente une zone de type lac
     * @param id l'identifiant de la zone
     * @param fishCount le nombre de poissons nageant dans le lac
     * @param specialPower l'éventuel pouvoir spécial du lac
     */
    record Lake(int id, int fishCount, SpecialPower specialPower) implements Water {
    }

    /**
     * Enregistrement qui représente une zone de type rivière
     * @param id l'identifiant de la zone
     * @param fishCount le nombre de poissons nageant dans la rivière
     * @param lake l'éventuel lac auquel la rivière est connectée
     */
    record River(int id, int fishCount, Lake lake) implements Water {

        /**
         * Méthode qui vérifie si la rivière est connectée à un lac ou non
         * @return vrai ssi la rivière est connectée à un lac, faux sinon
         */
        public boolean hasLake(){
            return (lake != null);
        }
    }

}
