package ch.epfl.chacun;

import java.util.*;

/**
 * Classe qui représente le plateau de jeu
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public final class Board {
    private final PlacedTile[] placedTiles;
    private final int[] placedTilesIndex;
    private final ZonePartitions zonePartitions;
    private final Set<Animal> cancelledAnimals;

    /**
     * La portée du plateau (le nombre de cases qui séparent la case centrale de l'un des bords du plateau)
     */
    public static final int REACH = 12;
    private static final int DIMENSION = (2*REACH) + 1;

    private static final int BOARD_SIZE = (int) Math.pow(DIMENSION,2);

    /**
     * Le plateau vide, qui ne contient absolument aucune tuile, même pas celle de départ
     */
    public static final Board EMPTY = new Board(new PlacedTile[BOARD_SIZE], new int[0], ZonePartitions.EMPTY,
            new HashSet<>());

    /**
     * Constructeur de la classe
     * @param placedTiles un tableau de tuiles placées
     * @param placedTilesIndex un tableau contenant les index des tuiles posées sur le plateau, dans l'ordre dans
     * lequel elles ont été posées
     * @param zonePartitions paritions qui correspondent à celles des zones des tuiles posées
     * @param cancelledAnimals l'ensemble des animaux annulés
     */
    private Board(PlacedTile[] placedTiles, int[] placedTilesIndex, ZonePartitions zonePartitions,
                  Set<Animal> cancelledAnimals) {
        this.placedTiles = placedTiles;
        this.placedTilesIndex = placedTilesIndex;
        this.zonePartitions = zonePartitions;
        this.cancelledAnimals = cancelledAnimals;
    }

    /**
     * Méthode vérifiant que l'objet recu est égal au plateau, en garantissant une comparaison par structure
     * (s'assure que tous les attributs sont deux à deux égaux)
     * @param obj l'objet à comparer au plateau
     * @return vrai si l'objet recu est égal au plateau, faux sinon
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Board board)
            return Arrays.equals(board.placedTiles, placedTiles) &&
                    Arrays.equals(board.placedTilesIndex, placedTilesIndex) &&
                    board.zonePartitions.equals(zonePartitions) &&
                    board.cancelledAnimals.equals(cancelledAnimals);

        return false;
    }

    /**
     * Méthode calculant le code de hachage du plateau en fonction de tous ses attributs
     * @return le code de hachage du plateau
     */
    @Override
    public int hashCode() {

        int firstInt = Arrays.hashCode(this.placedTiles);
        int secondInt = Arrays.hashCode(this.placedTilesIndex);

        return Objects.hash(firstInt, secondInt, this.zonePartitions, this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne la tuile à la position donnée, ou null s'il n'y en a aucune ou si la position
     * se trouve hors du plateau
     * @param pos la position donnée
     * @return la tuile à la position donnée, ou null s'il n'y en a aucune ou si la position se trouve
     * hors du plateau
     */
    public PlacedTile tileAt(Pos pos) {

        if (!isWithinBoard(pos))
            return null;

        int index = findIndex(pos);

        return placedTiles[index];
    }

    /**
     * Méthode qui retourne la tuile dont l'identifiant est celui donné
     * @param tileId l'identifiant donné
     * @return la tuile dont l'identifiant est celui donné
     * @throws IllegalArgumentException si cette tuile ne se trouve pas sur le plateau
     */
    public PlacedTile tileWithId(int tileId) {

        for (int index : placedTilesIndex) {
            PlacedTile placedTile = placedTiles[index];

            if (placedTile.id() == tileId) {
                return placedTile;
            }
        }

        throw new IllegalArgumentException("la tuile ne se trouve pas sur le plateau");
    }

    /**
     * Méthode qui retourne l'ensemble des animaux annulés
     * @return l'ensemble des animaux annulés
     */
    public Set<Animal> cancelledAnimals() {
        return Collections.unmodifiableSet(this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne l'ensemble des occupants se trouvant sur les tuiles du plateau
     * @return l'ensemble des occupants se trouvant sur les tuiles du plateau
     */
    public Set<Occupant> occupants() {

        Set<Occupant> occupantSet = new HashSet<>();

        for (int index : placedTilesIndex) {
            PlacedTile placedTile = placedTiles[index];

            if (placedTile.occupant() != null)
                occupantSet.add(placedTile.occupant());
        }

        return occupantSet;
    }

    /**
     * Méthode qui retourne l'aire forêt contenant la zone donnée
     * @param forest la zone forêt donnée
     * @return l'aire forêt contenant la zone donnée
     * @throws IllegalArgumentException si la zone en question n'appartient pas au plateau
     */
    public Area<Zone.Forest> forestArea(Zone.Forest forest) {
        return zonePartitions.forests().areaContaining(forest);
    }

    /**
     * Méthode qui retourne l'aire pré contenant la zone donnée
     * @param meadow la zone pré donnée
     * @return l'aire pré contenant la zone donnée
     * @throws IllegalArgumentException si la zone en question n'appartient pas au plateau
     */
    public Area<Zone.Meadow> meadowArea(Zone.Meadow meadow) {
        return zonePartitions.meadows().areaContaining(meadow);
    }

    /**
     * Méthode qui retourne l'aire rivière contenant la zone donnée
     * @param riverZone la zone rivière donnée
     * @return l'aire rivière contenant la zone donnée
     * @throws IllegalArgumentException si la zone en question n'appartient pas au plateau
     */
    public Area<Zone.River> riverArea(Zone.River riverZone) {
        return zonePartitions.rivers().areaContaining(riverZone);
    }

    /**
     * Méthode qui retourne le réseau hydrographique contenant la zone donnée
     * @param water la zone donnée
     * @return le réseau hydrographique contenant la zone donnée
     * @throws IllegalArgumentException si la zone en question n'appartient pas au plateau
     */
    public Area<Zone.Water> riverSystemArea(Zone.Water water) {
        return zonePartitions.riverSystems().areaContaining(water);
    }

    /**
     * Méthode qui retourne l'ensemble de toutes les aires pré du plateau
     * @return l'ensemble de toutes les aires pré du plateau
     */
    public Set<Area<Zone.Meadow>> meadowAreas() {
        return zonePartitions.meadows().areas();
    }

    /**
     * Méthode qui retourne l'ensemble de tous les réseaux hydrographiques du plateau
     * @return l'ensemble de tous les réseaux hydrographiques du plateau
     */
    public Set<Area<Zone.Water>> riverSystemAreas() {
        return zonePartitions.riverSystems().areas();
    }

    /**
     * Méthode qui retourne le pré adjacent à la zone donnée, sous la forme d'une aire qui ne contient que
     * les zones de ce pré, mais tous les occupants du pré complet
     * @param pos la position de la tuile donnée
     * @param meadowZone la zone donnée
     * @return le pré adjacent à la zone donnée
     */
    public Area<Zone.Meadow> adjacentMeadow(Pos pos, Zone.Meadow meadowZone) {

        Area<Zone.Meadow> completeMeadowArea = meadowArea(meadowZone);

        Set<Zone.Meadow> adjacentMeadowZones = new HashSet<>();

        for (int i = -1; i < 2; i++) {

            for (int j = -1; j < 2; j++) {

                Pos neighborPos = pos.translated(i, j);
                PlacedTile neighborTile = tileAt(neighborPos);

                if (neighborTile != null) {
                    neighborTile.meadowZones().stream()
                            .filter(completeMeadowArea.zones()::contains)
                            .forEach(adjacentMeadowZones::add);
                }
            }

        }

        List<PlayerColor> adjacentMeadowOccupants = new ArrayList<>(completeMeadowArea.occupants());

        return new Area<>(adjacentMeadowZones, adjacentMeadowOccupants, 0);
    }

    /**
     * Méthode qui retourne le nombre d'occupants de la sorte donnée appartenant au joueur donné et se trouvant
     * sur le plateau
     * @param player le joueur donné
     * @param occupantKind la sorte d'occupant donnée
     * @return le nombre d'occupants de la sorte donnée appartenant au joueur donné et se trouvant sur le plateau
     */
    public int occupantCount(PlayerColor player, Occupant.Kind occupantKind) {

        int count = 0;

        for (int index : placedTilesIndex) {
            PlacedTile placedTile = this.placedTiles[index];

            if (placedTile.placer() == player && placedTile.idOfZoneOccupiedBy(occupantKind) != -1)
                count++;
        }

        return count;
    }
    /**
     * Méthode qui retourne l'ensemble des positions d'insertions du plateau
     * @return l'ensemble des positions d'insertions du plateau
     */
    public Set<Pos> insertionPositions() {

        Set<Pos> insertionPositions = new HashSet<>();

        for (int index : placedTilesIndex) {
            PlacedTile placedTile = placedTiles[index];
            Pos tilePos = placedTile.pos();

            for (Direction direction : Direction.ALL) {
                Pos neighborPos = tilePos.neighbor(direction);
                PlacedTile neighborTile = tileAt(neighborPos);

                if (neighborTile == null && isWithinBoard(neighborPos)) {
                    insertionPositions.add(neighborPos);
                }
            }
        }

        return insertionPositions;
    }

    /**
     * Méthode qui retourne la dernière tuile posée qui peut être la tuile de départ si la première tuile normale
     * n'a pas encore été placée ou null si le plateau est vide
     * @return la dernière tuile posée
     */
    public PlacedTile lastPlacedTile() {

        if (placedTilesIndex.length == 0)
            return null;

        int lastTileIndex = placedTilesIndex[placedTilesIndex.length - 1];

        return placedTiles[lastTileIndex];
    }

    /**
     * Méthode qui retourne l'ensemble de toutes les aires forêts qui ont été fermées suite à la pose de la
     * dernière tuile, ou un ensemble vide si le plateau est vide
     * @return l'ensemble de toutes les aires forêts qui ont été fermées suite à la pose de la dernière tuile
     */
    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {

        Set<Area<Zone.Forest>> closedForests = new HashSet<>();

        if (lastPlacedTile() != null)

            for (Zone.Forest forest : lastPlacedTile().forestZones()) {
                Area<Zone.Forest> area = forestArea(forest);

                if (area.isClosed())
                    closedForests.add(area);
            }

        return closedForests;
    }

    /**
     * Méthode qui retourne l'ensemble de toutes les aires rivières qui ont été fermées suite à la pose de
     * la dernière tuile, ou un ensemble vide si le plateau est vide
     * @return l'ensemble de toutes les aires rivières qui ont été fermées suite à la pose de la dernière tuile
     */
    public Set<Area<Zone.River>> riversClosedByLastTile() {

        Set<Area<Zone.River>> closedRivers = new HashSet<>();

        if (lastPlacedTile() != null)

            for (Zone.River river : lastPlacedTile().riverZones()) {
                Area<Zone.River> area = riverArea(river);

                if (area.isClosed()) {
                    closedRivers.add(area);
                }
            }

        return closedRivers;
    }

    /**
     * Méthode qui retourne vrai ssi la tuile placée donnée pourrait être ajoutée au plateau
     * @param tile la tuile donnée
     * @return vrai ssi la tuile placée donnée pourrait être ajoutée au plateau
     */
    public boolean canAddTile(PlacedTile tile) {

        if (!insertionPositions().contains(tile.pos()))
            return false;

        for (Direction direction : Direction.ALL) {
            Pos neighborPos = tile.pos().neighbor(direction);
            PlacedTile neighborTile = tileAt(neighborPos);

            if (neighborTile != null) {
                TileSide tileSide = tile.side(direction);
                TileSide neighborTileSide = neighborTile.side(direction.opposite());

                if (!tileSide.isSameKindAs(neighborTileSide))
                    return false;

            }
        }

        return true;
    }

    /**
     * Méthode qui retourne vrai ssi la tuile donnée pourrait être posée sur l'une des positions d'insertion du plateau
     * @param tile la tuile donnée
     * @return vrai ssi la tuile donnée pourrait être posée sur l'une des positions d'insertion du plateau
     */
    public boolean couldPlaceTile(Tile tile) {

        for (Pos position : insertionPositions()) {

            for (Rotation rotation : Rotation.ALL) {
                PlacedTile placedTile = new PlacedTile(tile, null, rotation, position, null);

                if (canAddTile(placedTile))
                    return true;

            }
        }

        return false;
    }

    /**
     * Méthode qui retourne un plateau identique au récepteur, mais avec la tuile donnée en plus
     * @param tile la tuile donnée
     * @return un plateau identique au récepteur, mais avec la tuile donnée en plus
     * @throws IllegalArgumentException si le plateau n'est pas vide et la tuile donnée ne peut pas
     * être ajoutée au plateau
     */
    public Board withNewTile(PlacedTile tile) {

        Preconditions.checkArgument(this.placedTilesIndex.length == 0 || canAddTile(tile));

        PlacedTile[] newPlacedTiles = this.placedTiles.clone();

        int index = findIndex(tile.pos());
        newPlacedTiles[index] = tile;

        int[] newPlacedTilesIndex = Arrays.copyOf(this.placedTilesIndex, this.placedTilesIndex.length + 1);
        newPlacedTilesIndex[newPlacedTilesIndex.length - 1] = index;

        ZonePartitions.Builder partitionBuilder = new ZonePartitions.Builder(this.zonePartitions);
        partitionBuilder.addTile(tile.tile());

        for (Direction direction : Direction.ALL) {
            Pos neighborPos = tile.pos().neighbor(direction);
            PlacedTile neighborTile = tileAt(neighborPos);

            if (neighborTile != null) {
                TileSide tileSide = tile.side(direction);
                TileSide neighborTileSide = neighborTile.side(direction.opposite());

                partitionBuilder.connectSides(tileSide, neighborTileSide);
            }
        }

        ZonePartitions newZonePartitions = partitionBuilder.build();

        return new Board(newPlacedTiles, newPlacedTilesIndex, newZonePartitions, this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne un plateau identique au récepteur, mais avec l'occupant donné en plus
     * @param occupant l'occupant donné
     * @return un plateau identique au récepteur, mais avec l'occupant donné en plus
     * @throws IllegalArgumentException si la tuile sur laquelle se trouverait l'occupant est deja occupée
     */
    public Board withOccupant(Occupant occupant) {

        PlacedTile[] newPlacedTiles = this.placedTiles.clone();

        int zoneId = occupant.zoneId();
        int tileId = Zone.tileId(zoneId);
        PlacedTile tile = tileWithId(tileId);

        PlacedTile newTile = tile.withOccupant(occupant);

        int index = findIndex(tile.pos());
        newPlacedTiles[index] = newTile;

        ZonePartitions.Builder partitionsBuilder = new ZonePartitions.Builder(this.zonePartitions);
        partitionsBuilder.addInitialOccupant(tile.placer(), occupant.kind(), tile.zoneWithId(zoneId));

        ZonePartitions newZonePartitions = partitionsBuilder.build();

        return new Board(newPlacedTiles, this.placedTilesIndex, newZonePartitions, this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne un plateau identique au récepteur, mais avec l'occupant donné en moins
     * @param occupant l'occupant donné
     * @return un plateau identique au récepteur, mais avec l'occupant donné en moins
     */
    public Board withoutOccupant(Occupant occupant) {

        PlacedTile[] newPlacedTiles = this.placedTiles.clone();

        int zoneId = occupant.zoneId();
        int tileId = Zone.tileId(zoneId);
        PlacedTile tile = tileWithId(tileId);

        PlacedTile newTile = tile.withNoOccupant();

        int index = findIndex(tile.pos());
        newPlacedTiles[index] = newTile;

        ZonePartitions.Builder partitionsBuilder = new ZonePartitions.Builder(this.zonePartitions);
        partitionsBuilder.removePawn(tile.placer(), tile.zoneWithId(zoneId));

        ZonePartitions newZonePartitions = partitionsBuilder.build();

        return new Board(newPlacedTiles, this.placedTilesIndex, newZonePartitions, this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne un plateau identique au récepteur, mais sans aucun occupant dans les forêts et
     * les rivières données
     * @param forests les forêts données
     * @param rivers  les rivières données
     * @return un plateau identique au récepteur, mais sans aucun occupant dans les forêts et les rivières données
     */
     public Board withoutGatherersOrFishersIn(Set<Area<Zone.Forest>> forests, Set<Area<Zone.River>> rivers) {

        PlacedTile[] newPlacedTiles = this.placedTiles.clone();
        ZonePartitions.Builder partitionsBuilder = new ZonePartitions.Builder(this.zonePartitions);

        for (Area<Zone.Forest> forestArea : forests) {
            partitionsBuilder.clearGatherers(forestArea);

            for (int index : this.placedTilesIndex) {
                PlacedTile placedTile = this.placedTiles[index];

                for (Zone.Forest forestZone : forestArea.zones())
                    if (placedTile.idOfZoneOccupiedBy(Occupant.Kind.PAWN) == forestZone.id())
                        newPlacedTiles[index] = placedTile.withNoOccupant();
            }
        }

         for (Area<Zone.River> riverArea : rivers) {
             partitionsBuilder.clearFishers(riverArea);

             for (int index : this.placedTilesIndex) {
                 PlacedTile placedTile = this.placedTiles[index];

                 for (Zone.River riverZone : riverArea.zones()) {
                     if (placedTile.riverZones().contains(riverZone)
                             && placedTile.occupant() != null
                             && placedTile.occupant().zoneId() == riverZone.id()
                             && placedTile.occupant().kind() == Occupant.Kind.PAWN) {
                         newPlacedTiles[index] = placedTile.withNoOccupant();
                     }
                 }
             }
         }

        ZonePartitions newZonePartitions = partitionsBuilder.build();

        return new Board(newPlacedTiles, this.placedTilesIndex, newZonePartitions, this.cancelledAnimals);
    }

    /**
     * Méthode qui retourne un plateau identique au récepteur, mais avec l'ensemble des animaux donnés ajouté
     * à l'ensemble des animaux annulés
     * @param newlyCancelledAnimals l'ensemble des animaux donnés
     * @return un plateau identique au récepteur, mais avec l'ensemble des animaux donnés ajouté à l'ensemble
     * des animaux annulés
     */
    public Board withMoreCancelledAnimals(Set<Animal> newlyCancelledAnimals) {

        Set<Animal> newCancelledAnimals = new HashSet<>(this.cancelledAnimals);
        newCancelledAnimals.addAll(newlyCancelledAnimals);

        return new Board(this.placedTiles, this.placedTilesIndex, this.zonePartitions, newCancelledAnimals);
    }

    /**
     * Méthode calculant l'index de la tuile placée sur le plateau donnée
     * @param tilePosition la position de la tuile
     * @return l'index de la tuile
     */
    private int findIndex(Pos tilePosition) {
        int normalizedX = tilePosition.x() + REACH;
        int normalizedY = tilePosition.y() + REACH;

        return DIMENSION * normalizedY + normalizedX;
    }

    /**
     * Méthode vérifiant que la position donnée est valide (qu'elle appartient au plateau)
     * @param pos la position donnée
     * @return vrai si la position est valide, faux sinon
     */
    private boolean isWithinBoard(Pos pos) {
        return pos.x() >= -REACH && pos.x() <= REACH && pos.y() >= -REACH && pos.y() <= REACH;
    }

}

