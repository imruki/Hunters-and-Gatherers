package ch.epfl.chacun;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enregistrement qui représente une partition de zones d'un type donné
 * @param areas l'ensemble des aires formant la partition
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record ZonePartition<Z extends Zone>(Set<Area<Z>> areas) {

    /**
     * Constructeur compact qui garantit l'immuabilité de la classe
     */
    public ZonePartition{
        areas = Set.copyOf(areas);
    }

    /**
     * Constructeur secondaire sans arguments
     */
    public ZonePartition(){
        this(new HashSet<>());
    }

    /**
     * Méthode qui retourne l'aire contenant la zone passée en argument
     * @param zone la zone donnée
     * @throws IllegalArgumentException si la zone n'appartient à aucune aire de la partition
     * @return l'aire contenant la zone passée en argument
     */
    public Area<Z> areaContaining(Z zone){

        for (Area<Z> area : areas){
            for (Z areaZone : area.zones()){
                if (areaZone.equals(zone))
                    return area;
            }
        }

        throw new IllegalArgumentException("la zone n'appartient à aucune aire de la partition");
    }

    /**
     * Méthode qui retourne l'aire contenant la zone passée en argument (pour pouvoir etre invoquee par Zone.Builder)
     * @param zone la zone donnée
     * @throws IllegalArgumentException si la zone n'appartient à aucune aire de la partition
     * @return l'aire contenant la zone passée en argument
     */
    private static <Z extends Zone> Area<Z> areaContaining(Z zone, Set<Area<Z>> areas){
        for (Area<Z> area : areas){
            for (Z areaZone : area.zones()){
                if (areaZone.equals(zone))
                    return area;
            }
        }
        throw new IllegalArgumentException("la zone n'appartient à aucune aire de la partition");
    }

    /**
     * Batisseur d'une partition de zones (ZonePartition)
     */
    public static final class Builder<Z extends Zone>{

        private final HashSet<Area<Z>> builderAreas;

        /**
         * Constructeur qui permet de créer une partition de zones à partir d'une autre déjà existante
         * @param partition une partition de zones existante
         */
        public Builder(ZonePartition<Z> partition) {
            this.builderAreas = new HashSet<>(partition.areas);
        }

        /**
         * Méthode qui ajoute à la partition en cours de construction une nouvelle aire inoccupée,
         * constituée uniquement de la zone donnée, et possédant le nombre de connexions ouvertes donné
         * @param zone la zone donnée
         * @param openConnections le nombre de connexions ouvertes donné
         */
        public void addSingleton(Z zone, int openConnections){
            Area<Z> areaToAdd = new Area<>(Set.of(zone), Collections.emptyList(), openConnections);
            builderAreas.add(areaToAdd);
        }

        /**
         * Méthode qui ajoute à l'aire contenant la zone donnée un occupant initial du joueur de la couleur donnée
         * @param zone la zone donnée
         * @param color la couleur du joueur donné
         * @throws IllegalArgumentException si la zone n'appartient pas à une aire de la partition, ou si l'aire est déjà occupée
         */
        public void addInitialOccupant(Z zone, PlayerColor color){
            Area<Z> areaContainingZone = ZonePartition.areaContaining(zone, builderAreas);
            builderAreas.remove(areaContainingZone);
            builderAreas.add(areaContainingZone.withInitialOccupant(color));
        }

        /**
         * Méthode qui supprime de l'aire contenant la zone donnée un occupant du joueur de la couleur donnée
         * @param zone la zone donnée
         * @param color la couleur du joueur donné
         * @throws IllegalArgumentException si la zone n'appartient pas à une aire de la partition,
         * ou si elle n'est pas occupée par au moins un occupant du joueur de la couleur donnée
         */
        public void removeOccupant(Z zone, PlayerColor color){
            Area<Z> areaContainingZone = ZonePartition.areaContaining(zone, builderAreas);
            builderAreas.remove(areaContainingZone);
            builderAreas.add(areaContainingZone.withoutOccupant(color));
        }

        /**
         * Méthode qui supprime tous les occupants de l'aire donnée
         * @param area l'aire donnée
         * @throws IllegalArgumentException si l'aire ne fait pas partie de la partition de zones
         */
        public void removeAllOccupantsOf(Area<Z> area){

            Preconditions.checkArgument(builderAreas.contains(area));

            builderAreas.remove(area);
            builderAreas.add(area.withoutOccupants());
        }

        /**
         * Méthode qui connecte entre elles les aires contenant les zones données pour en faire une aire plus grande
         * @param zone1 première zone donnée
         * @param zone2 seconde zone donnée
         * @throws IllegalArgumentException si l'une des deux zones n'appartient pas à une aire de la partition
         */
        public void union(Z zone1, Z zone2) {

            Area<Z> area1 = ZonePartition.areaContaining(zone1, builderAreas);
            Area<Z> area2 = ZonePartition.areaContaining(zone2, builderAreas);

            Area<Z> bigArea = area1.connectTo(area2);

            builderAreas.add(bigArea);
            builderAreas.remove(area1);

            if (area1 != area2)
                builderAreas.remove(area2);
        }

        /**
         * Méthode qui construit la partition de zones
         * @return la partition de zones
         */
        public ZonePartition<Z> build(){
            return new ZonePartition<>(new HashSet<>(builderAreas));
        }

    }

}

