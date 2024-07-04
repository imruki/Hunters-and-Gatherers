package ch.epfl.chacun;

/**
 * Classe qui calcule les points obtenus par un joueur dans différentes situations
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class Points {

    /**
     * Constructeur empêchant l'instanciation de la classe
     */
    private Points() {}

    /**
     * Méthode qui retourne le nombre de points obtenus par les cueilleurs majoritaires d'une forêt fermée
     * @param tileCount le nombre de tuiles la constituant
     * @param mushroomGroupCount le nombre de groupes de champignons
     * @throws IllegalArgumentException si le nombre de tuiles inférieur ou égal à 1 ou si le nombre de groupes de
     * champignons est strictement négatif
     * @return le nombre de points obtenus
     */
    public static int forClosedForest(int tileCount, int mushroomGroupCount) {
        Preconditions.checkArgument((tileCount >1) && (mushroomGroupCount >=0) );

        return (2 * tileCount) + (3 * mushroomGroupCount);
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par les pêcheurs majoritaires d'une rivière fermée
     * @param tileCount le nombre de tuiles la constituant
     * @param fishCount le nombre de poissons nageant dans la riviere/lacs aux extremites
     * @throws IllegalArgumentException si le nombre de tuiles inférieur ou égal à 1 ou si le nombre de poissons
     * est strictement négatif
     * @return le nombre de points obtenus
     */
    public static int forClosedRiver(int tileCount, int fishCount) {
        Preconditions.checkArgument((tileCount >1) && (fishCount >= 0) );

        return tileCount + fishCount;
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par les chasseurs majoritaires d'un pré
     * @param mammothCount le nombre de mammouths contenus dans le pré
     * @param aurochsCount le nombre d'aurochs contenus dans le pré
     * @param deerCount le nombre de cerfs contenus dans le pré (hormis ceux dévorés par des smilodons)
     * @throws IllegalArgumentException si le nombre de mammouths, d'aurochs ou de cerfs est strictement négatif
     * @return le nombre de points obtenus
     */

    public static int forMeadow(int mammothCount, int aurochsCount, int deerCount) {
        Preconditions.checkArgument((mammothCount >=0) && (aurochsCount >=0) && (deerCount >=0) );

        return (3*mammothCount) + (2*aurochsCount) + deerCount;
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par les pêcheurs majoritaires d'un réseau hydrographique
     * @param fishCount le nombre de poissons nageant dans le réseau hydrographique
     * @throws IllegalArgumentException si le nombre de poissons est strictement négatif
     * @return le nombre de points obtenus
     */
    public static int forRiverSystem(int fishCount) {
        Preconditions.checkArgument(fishCount >= 0);

        return fishCount;
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par le joueur déposant la pirogue dans un réseau hydrographique
     * @param lakeCount le nombre de lacs constituant le réseau hydrographique
     * @throws IllegalArgumentException si le nombre de lacs est inférieur ou égal à 0
     * @return le nombre de points obtenus
     */
    public static int forLogboat(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);

        return 2*lakeCount;
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par les pêcheurs majoritaires du réseau hydrographique
     * contenant le radeau
     * @param lakeCount le nombre de lacs constituant le réseau hydrographique
     * @throws IllegalArgumentException si le nombre de lacs est inférieur ou égal à 0
     * @return le nombre de points obtenus
     */
    public static int forRaft(int lakeCount) {
        Preconditions.checkArgument(lakeCount > 0);

        return lakeCount;
    }

}
