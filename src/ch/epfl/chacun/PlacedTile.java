package ch.epfl.chacun;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enregristrement qui représente une tuile qui a été placée
 * @param tile la tuile qui a été placée
 * @param placer le placeur de la tuile, ou null pour la tuile de départ
 * @param rotation la rotation appliquée à la tuile lors de son placement
 * @param pos la position à laquelle la tuile a été placée
 * @param occupant l'occupant de la tuile, ou null si elle n'est pas occupée
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos, Occupant occupant) {

    /**
     * Constructeur compact validant les arguments qui lui sont passés
     * @throws NullPointerException si la tuile, la rotation ou la position est null
     */
    public PlacedTile {
        Objects.requireNonNull(tile);
        Objects.requireNonNull(rotation);
        Objects.requireNonNull(pos);
    }

    /**
     * Constructeur secondaire sans le dernier argument (occupant)
     */
    public PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos) {
        this(tile, placer, rotation, pos, null);
    }

    /**
     * Méthode qui retourne l'identifiant de la tuile placée
     * @return l'identifiant de la tuile placée
     */
    public int id() {
        return tile.id();
    }

    /**
     * Méthode qui retourne la sorte de la tuile placée
     * @return la sorte de la tuile placée
     */
    public Tile.Kind kind() {
        return tile.kind();
    }

    /**
     * Méthode qui retourne le côté de la tuile dans la direction donnée
     * @return le côté de la tuile dans la direction donnée
     */
    public TileSide side(Direction direction) {

        int sideIndex = direction.rotated(rotation.negated()).ordinal();

        return tile.sides().get(sideIndex);
    }

    /**
     * Méthode qui retourne la zone de la tuile dont l'identifiant est celui donné
     * @return la zone de la tuile dont l'identifiant est celui donné
     * @throws IllegalArgumentException si la tuile ne possède pas de zone avec cet identifiant
     */

    public Zone zoneWithId(int id) {

        for (Zone zone : tile.zones()) {

            if (zone.id() == id)
                return zone;
        }

        throw new IllegalArgumentException();
    }

    /**
     * Méthode qui retourne la zone de la tuile ayant un pouvoir spécial ou null s'il n'y en a aucune
     * @return la zone de la tuile ayant un pouvoir spécial ou null s'il n'y en a aucune
     */
    public Zone specialPowerZone() {

        for (Zone zone : tile.zones()) {

            if (zone.specialPower() != null)
                return zone;
        }

        return null;
    }

    /**
     * Méthode qui retourne l'ensemble des zones forêt de la tuile
     * @return l'ensemble des zones forêt de la tuile
     */
    public Set<Zone.Forest> forestZones() {

        return tile.zones().stream()
                .filter(zone -> zone instanceof Zone.Forest)
                .map(zone -> (Zone.Forest) zone)
                .collect(Collectors.toSet());
    }

    /**
     * Méthode qui retourne l'ensemble des zones pré de la tuile
     * @return l'ensemble des zones pré de la tuile
     */
    public Set<Zone.Meadow> meadowZones() {

        return tile.zones().stream()
                .filter(zone -> zone instanceof Zone.Meadow)
                .map(zone -> (Zone.Meadow) zone)
                .collect(Collectors.toSet());
    }

    /**
     * Méthode qui retourne l'ensemble des zones rivière de la tuile
     * @return l'ensemble des zones rivière de la tuile
     */
    public Set<Zone.River> riverZones() {

        return tile.zones().stream()
                .filter(zone -> zone instanceof Zone.River)
                .map(zone -> (Zone.River) zone)
                .collect(Collectors.toSet());
    }

    /**
     * Méthode qui retourne l'ensemble de tous les occupants potentiels de la tuile ou un ensemble vide
     * si la tuile est celle de départ
     * @return l'ensemble de tous les occupants potentiels de la tuile ou un ensemble vide si la tuile
     * est celle de départ
     */
    public Set<Occupant> potentialOccupants() {

        if (placer == null)
            return Set.of();

        Set<Occupant> occupantsSet = new HashSet<>();

        for (Zone zone : tile.sideZones()) {
            Occupant zonePawn = new Occupant(Occupant.Kind.PAWN, zone.id());
            occupantsSet.add(zonePawn);
        }

        for (Zone.River river : riverZones()) {

            if (river.hasLake()) {
                Occupant lakeHut = new Occupant(Occupant.Kind.HUT, river.lake().id());
                occupantsSet.add(lakeHut);

            } else {
                Occupant riverHut = new Occupant(Occupant.Kind.HUT, river.id());
                occupantsSet.add(riverHut);
            }

        }

        return occupantsSet;
    }

    /**
     * Méthode qui retourne une tuile placée identique au récepteur, mais occupée par l'occupant donné
     * @return une tuile placée identique au récepteur, mais occupée par l'occupant donné
     * @throws IllegalArgumentException si le récepteur est déjà occupé (si son occupant n'est pas null)
     */
    public PlacedTile withOccupant(Occupant occupant) {
        Preconditions.checkArgument(this.occupant == null);

        return new PlacedTile(this.tile, this.placer, this.rotation, this.pos, occupant);
    }

    /**
     * Méthode qui retourne une tuile placée identique au récepteur, mais sans occupant
     * @return une tuile placée identique au récepteur, mais sans occupant
     */
    public PlacedTile withNoOccupant() {
        return new PlacedTile(this.tile, this.placer, this.rotation, this.pos);
    }

    /**
     * Méthode qui retourne l'identifiant de la zone occupée par un occupant de la sorte donnée (pion ou hutte)
     * @return -1 si la tuile n'est pas occupée ou si l'occupant n'est pas de la bonne sorte
     */
    public int idOfZoneOccupiedBy(Occupant.Kind occupantKind) {

        if ( (occupant == null) || (occupant.kind() != occupantKind) )
            return -1;

        return occupant.zoneId();
    }

}