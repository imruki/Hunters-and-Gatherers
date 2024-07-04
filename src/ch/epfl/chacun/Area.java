package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Enregistrement qui représente une aire
 * @param zones l'ensemble des zones constituant l'aire
 * @param occupants les couleurs des éventuels joueurs occupant l'aire (triés par couleur)
 * @param openConnections le nombre de connexions ouvertes de l'aire
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record Area<Z extends Zone>(Set<Z> zones, List<PlayerColor> occupants, int openConnections) {

    /**
     * Constructeur compact vérifiant la validité de l'attribut "openConnections" et garantissant l'immuabilité de la
     * classe
     * @throws IllegalArgumentException si openConnections est strictement négatif
     */
    public Area {
        Preconditions.checkArgument(openConnections >=0 );

        zones = Set.copyOf(zones);
        List<PlayerColor> sortedOccupants = new ArrayList<>(occupants);
        Collections.sort(sortedOccupants);
        occupants = List.copyOf(sortedOccupants);


    }

    /**
     * Méthode qui retourne vrai si et seulement si l'aire forêt donnée contient au moins un menhir
     * @param forest l'aire forêt donnée
     * @return vrai si et seulement si l'aire forêt donnée contient au moins un menhir
     */
    public static boolean hasMenhir(Area<Zone.Forest> forest){

        for (Zone.Forest forestZone : forest.zones() ) {

            if (forestZone.kind() == Zone.Forest.Kind.WITH_MENHIR)
                return true;

        }

        return false;
    }

    /**
     * Méthode qui retourne le nombre de groupes de champignons que contient l'aire forêt donnée
     * @param forest l'aire forêt donnée
     * @return le nombre de groupes de champignons que contient l'aire forêt donnée
     */
    public static int mushroomGroupCount(Area<Zone.Forest> forest){

        int mushroomGroupCount = 0;
        for (Zone.Forest forestZone : forest.zones)
            if (forestZone.kind() == Zone.Forest.Kind.WITH_MUSHROOMS)
                mushroomGroupCount++;

        return mushroomGroupCount;
    }

    /**
     * Méthode qui retourne l'ensemble des animaux se trouvant dans l'aire pré donnée, mais qui ne font pas partie de
     * l'ensemble des animaux annulés donné
     * @param meadow l'aire pré donnée
     * @param cancelledAnimals l'ensemble des animaux annulés donné
     * @return l'ensemble des animaux se trouvant dans l'aire pré donnée, mais qui ne font pas partie de l'ensemble des
     * animaux annulés donné
     */
    public static Set<Animal> animals(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals){

        return meadow.zones().stream()
                .flatMap(meadowZone -> meadowZone.animals().stream())
                .filter(animal -> !cancelledAnimals.contains(animal))
                .collect(Collectors.toSet());
    }

    /**
     * Méthode qui retourne le nombre de poissons nageant dans l'aire rivière donnée ou dans l'un des éventuels lacs se
     * trouvant à ses extrémités
     * @param river l'aire rivière donnée
     * @return le nombre de poissons nageant dans l'aire rivière donnée ou dans l'un des éventuels lacs se trouvant à
     * ses extrémités
     */
    public static int riverFishCount(Area<Zone.River> river){

        int totalFishCount = 0;

        Set <Zone.Lake> seenLakes = new HashSet<>();

        for (Zone.River riverZone : river.zones() ) {
            totalFishCount += riverZone.fishCount();

            if ( riverZone.hasLake() && (seenLakes.add(riverZone.lake())) ) {
                totalFishCount += riverZone.lake().fishCount();
            }
        }
        return totalFishCount;

    }

    /**
     * Méthode qui retourne le nombre de poissons nageant dans l'aire réseau hydrographique donnée
     * @param riverSystem l'aire réseau hydrographique donnée
     * @return le nombre de poissons nageant dans l'aire réseau hydrographique donnée
     */
    public static int riverSystemFishCount(Area<Zone.Water> riverSystem){

        int totalFishCount = 0;

        for (Zone.Water waterZone : riverSystem.zones())
            totalFishCount += waterZone.fishCount();

        return totalFishCount;
    }
    /**
     * Méthode qui retourne le nombre de lacs de l'aire réseau hydrographique donnée
     * @param riverSystem l'aire réseau hydrographique donnée
     * @return le nombre de lacs de l'aire réseau hydrographique donnée
     */
    public static int lakeCount(Area<Zone.Water> riverSystem){

        int lakeCount = 0;
        for (Zone.Water zone : riverSystem.zones)
            if (zone instanceof Zone.Lake)
                lakeCount++;

        return lakeCount;
    }

    /**
     * Méthode qui retourne vrai ssi l'aire est fermée
     * @return vrai ssi l'aire est fermée
     */
    public boolean isClosed(){
        return openConnections == 0;
    }

    /**
     * Méthode qui retourne vrai ssi l'aire est occupée par au moins un occupant
     * @return vrai ssi l'aire est occupée par au moins un occupant
     */
    public boolean isOccupied(){
        return !occupants.isEmpty();
    }

    /**
     * Méthode qui retourne l'ensemble des occupants majoritaires de l'aire
     * @return l'ensemble des occupants majoritaires de l'aire
     */
    public Set<PlayerColor> majorityOccupants(){

        int[] countByColor = new int[PlayerColor.ALL.size()];
        int maxCount = 0;

        for (PlayerColor occupant : occupants) {
            int index = PlayerColor.ALL.indexOf(occupant);
            countByColor[index]++;
            if (countByColor[index] > maxCount) {
                maxCount = countByColor[index];
            }
        }

        Set<PlayerColor> leadingColors = new HashSet<>();
        if (maxCount > 0) {
            for (int i = 0; i < countByColor.length; i++) {
                if (countByColor[i] == maxCount) {
                    leadingColors.add(PlayerColor.ALL.get(i));
                }
            }
        }

        return leadingColors;
    }

    /**
     * Méthode qui retourne l'aire résultant de la connexion du récepteur à l'aire donnée
     * @param that l'aire donnée
     * @return l'aire résultant de la connexion du récepteur (this) à l'aire donnée
     */

    public Area<Z> connectTo(Area<Z> that) {

        if (this == that) {
            return new Area<>(this.zones, this.occupants, Math.max(0, this.openConnections - 2));
        }

        Set<Z> combinedZones = new HashSet<>(this.zones);
        combinedZones.addAll(that.zones);

        List<PlayerColor> combinedOccupants = new ArrayList<>(this.occupants);
        combinedOccupants.addAll(that.occupants);

        int openConnectionsLeft = Math.max(0, this.openConnections + that.openConnections - 2);

        return new Area<>(combinedZones, combinedOccupants, openConnectionsLeft);

    }

    /**
     * Méthode qui retourne une aire identique au récepteur, si ce n'est qu'elle est occupée par l'occupant donné
     * @param occupant l'occupant donné
     * @throws IllegalArgumentException si le récepteur est déjà occupé
     * @return une aire identique au récepteur, si ce n'est qu'elle est occupée par l'occupant donné
     */
    public Area<Z> withInitialOccupant(PlayerColor occupant){
        Preconditions.checkArgument(!isOccupied());

        return new Area<>(zones, new ArrayList<>(singleton(occupant)), openConnections);
    }

    /**
     * Méthode qui retourne une aire identique au récepteur, mais qui comporte un occupant de la couleur donnée en moins
     * @param occupant la couleur donnée
     * @throws IllegalArgumentException si le récepteur ne contient aucun occupant de la couleur donnée
     * @return une aire identique au récepteur, mais qui comporte un occupant de la couleur donnée en moins
     */
    public Area<Z> withoutOccupant(PlayerColor occupant){
        Preconditions.checkArgument(occupants.contains(occupant));

        List<PlayerColor> newOccupants = new ArrayList<>(occupants);
        newOccupants.remove(occupant);

        return new Area<>(zones, newOccupants, openConnections);
    }

    /**
     * Méthode qui retourne une aire identique au récepteur, mais totalement dénuée d'occupants
     * @return une aire identique au récepteur, mais totalement dénuée d'occupants
     */
    public Area<Z> withoutOccupants(){
        return new Area<>(zones, new ArrayList<>(), openConnections);
    }

    /**
     * Méthode qui retourne l'ensemble de l'identité des tuiles contenant l'aire
     * @return l'ensemble de l'identité des tuiles contenant l'aire
     */
    public Set<Integer> tileIds() {
        return zones.stream().map(Zone::tileId).collect(Collectors.toSet());
    }

    /**
     * Méthode qui retourne la zone de l'aire qui possède le pouvoir spécial donné, ou null s'il n'en existe aucune
     * @param specialPower le pouvoir spécial donné
     * @return la zone de l'aire qui possède le pouvoir spécial donné, ou null s'il n'en existe aucune
     */
    public Zone zoneWithSpecialPower(Zone.SpecialPower specialPower){

        for (Zone zone : zones ){
            if (zone.specialPower() == specialPower)
                return zone;
        }

        return null;
    }

}
