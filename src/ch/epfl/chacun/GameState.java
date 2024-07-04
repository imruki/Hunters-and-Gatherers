package ch.epfl.chacun;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enregistrement qui représente l'état complet d'une partie de ChaCuN et contient la totalité des informations liées
 * à une partie en cours
 * @param players la liste de tous les joueurs de la partie avec le joueur courant en tête de liste
 * @param tileDecks les trois tas des tuiles restantes
 * @param tileToPlace l'éventuelle tuile à placer, qui a été prise du sommet du tas des tuiles normales ou du tas des
 * tuiles menhir
 * @param board le plateau de jeu
 * @param nextAction la prochaine action à effectuer, le type Action étant décrit ci-dessous
 * @param messageBoard le tableau d'affichage contenant les messages générés jusqu'à présent dans la partie
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */
public record GameState(List<PlayerColor> players, TileDecks tileDecks, Tile tileToPlace, Board board, Action nextAction,
                        MessageBoard messageBoard) {

    /**
     * Enumeration qui contient les prochaines actions possibles à effectuer dans la partie
     */
    public enum Action {
        START_GAME, PLACE_TILE, RETAKE_PAWN, OCCUPY_TILE, END_GAME
    }

    /**
     * Constructeur compact validant les arguments qui lui sont passés et garantissant l'immuabilité de la classe
     * @throws NullPointerException si les tas de cartes, le plateau de jeu, la prochaine action ou le tableau
     * d'affichage sont nuls
     * @throws IllegalArgumentException s'il y a moins de 2 joueurs ou la tuile à placer n'est pas null et la prochaine
     * action n'est pas PLACE_TILE
     */
    public GameState {
        Preconditions.checkArgument(players.size() >= 2);
        Preconditions.checkArgument( (tileToPlace == null) ^ (nextAction == Action.PLACE_TILE) );
        Objects.requireNonNull(tileDecks);
        Objects.requireNonNull(board);
        Objects.requireNonNull(nextAction);
        Objects.requireNonNull(messageBoard);
        players = List.copyOf(players);
    }

    /**
     * Méthode qui retourne l'état de jeu initial pour les joueurs, tas et « créateur de texte » donnés
     * @param players les joueurs donnés
     * @param tileDecks les tas de tuiles donnés
     * @param textMaker le « créateur de texte » donné
     * @return l'état de jeu initial pour les joueurs, tas et « créateur de texte » donnés
     */
    public static GameState initial(List<PlayerColor> players, TileDecks tileDecks, TextMaker textMaker) {
        return new GameState(players, tileDecks, null, Board.EMPTY, Action.START_GAME,
                new MessageBoard(textMaker, Collections.emptyList()));
    }

    /**
     * Méthode qui retourne le joueur courant, ou null s'il n'y en a pas
     * @return le joueur courant, ou null s'il n'y en a pas
     */
    public PlayerColor currentPlayer() {

        if (nextAction == Action.START_GAME || nextAction == Action.END_GAME)
            return null;

        return players.getFirst();
    }

    /**
     * Méthode qui retourne le nombre d'occupants libres du type donné et appartenant au joueur donné
     * @param player le joueur donné
     * @param kind la sorte d'occupant donnée
     * @return le nombre d'occupants libres du type donné et appartenant au joueur donné
     */
    public int freeOccupantsCount(PlayerColor player, Occupant.Kind kind) {
        return Occupant.occupantsCount(kind) - board.occupantCount(player, kind);
    }

    /**
     * Méthode qui retourne l'ensemble des occupants potentiels de la dernière tuile posée
     * @return l'ensemble des occupants potentiels de la dernière tuile posée
     * @throws IllegalArgumentException si le plateau est vide
     */
    public Set<Occupant> lastTilePotentialOccupants() {
        PlacedTile lastPlacedTile = board.lastPlacedTile();

        Preconditions.checkArgument(lastPlacedTile != null);

        Set<Occupant> potentialOccupantsSet = new HashSet<>(lastPlacedTile.potentialOccupants());

        potentialOccupantsSet.removeIf(occupant -> {

            Zone occupantZone = lastPlacedTile.zoneWithId(occupant.zoneId());

                    return switch (occupantZone) {
                        case Zone.Forest forestZone -> board.forestArea(forestZone).isOccupied();
                        case Zone.Meadow meadowZone -> board.meadowArea(meadowZone).isOccupied();
                        case Zone.River riverZone when occupant.kind() == Occupant.Kind.PAWN
                                -> board.riverArea(riverZone).isOccupied();
                        case Zone.Water waterZone -> board.riverSystemArea(waterZone).isOccupied();
                    }

                    || (freeOccupantsCount(currentPlayer(), occupant.kind()) == 0);

        });

        return potentialOccupantsSet;
    }

    /**
     * Méthode qui gère la transition de START_GAME à PLACE_TILE en plaçant la tuile de départ au centre du plateau
     * et en tirant la première tuile du tas des tuiles normales, qui devient la tuile à jouer
     * @return l'état du jeu mis à jour
     * @throws IllegalArgumentException si la prochaine action n'est pas START_GAME
     */
    public GameState withStartingTilePlaced() {

        Preconditions.checkArgument(nextAction == Action.START_GAME);

        PlacedTile startPlacedTile = new PlacedTile(tileDecks.topTile(Tile.Kind.START), null, Rotation.NONE,
                Pos.ORIGIN, null);
        Board newBoard = board.withNewTile(startPlacedTile);

        Tile newTileToPlace = tileDecks.topTile(Tile.Kind.NORMAL);
        TileDecks newTileDecks = tileDecks.withTopTileDrawn(Tile.Kind.START);
        newTileDecks = newTileDecks.withTopTileDrawn(Tile.Kind.NORMAL);

        return new GameState(players, newTileDecks, newTileToPlace, newBoard, Action.PLACE_TILE, messageBoard);
    }

    /**
     * Méthode qui gère toutes les transitions à partir de PLACE_TILE en ajoutant la tuile donnée au plateau,
     * attribuant les éventuels points obtenus suite à la pose de la pirogue ou de la fosse à pieux, et déterminant
     * l'action suivante, qui peut être RETAKE_PAWN si la tuile posée contient le chaman
     * @param tile la tuile donnée
     * @return l'état du jeu mis à jour
     * @throws IllegalArgumentException si la prochaine action n'est pas PLACE_TILE
     */

    public GameState withPlacedTile(PlacedTile tile) {

        Preconditions.checkArgument(nextAction == Action.PLACE_TILE && tile.occupant() == null);

        MessageBoard newMessageBoard = messageBoard;
        Board newBoard = board.withNewTile(tile);

        int pawnCount = newBoard.occupantCount(currentPlayer(), Occupant.Kind.PAWN);

        switch (tile.specialPowerZone()) {
            case Zone.Lake lakeZone
                    when lakeZone.specialPower() == Zone.SpecialPower.LOGBOAT ->
                    newMessageBoard = newMessageBoard.withScoredLogboat(currentPlayer(),
                            newBoard.riverSystemArea(lakeZone));

            case Zone.Meadow meadowZone1
                    when ((meadowZone1.specialPower() == Zone.SpecialPower.SHAMAN) && (pawnCount > 0)) -> {
                return new GameState(players, tileDecks, null, newBoard, Action.RETAKE_PAWN, newMessageBoard);
            }

            case Zone.Meadow meadowZone2
                    when meadowZone2.specialPower() == Zone.SpecialPower.HUNTING_TRAP -> {

                Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(tile.pos(), meadowZone2);
                Set<Animal> adjacentAnimalsSet = Area.animals(adjacentMeadow, newBoard.cancelledAnimals());

                Set<Animal> cancelledDeers = cancelledDeers(adjacentAnimalsSet);
                newMessageBoard = newMessageBoard.withScoredHuntingTrap(currentPlayer(), adjacentMeadow, cancelledDeers);

                newBoard = newBoard.withMoreCancelledAnimals(adjacentAnimalsSet);
            }

            case null, default -> {}

        }

        GameState newGameState = new GameState(players, tileDecks, null, newBoard, Action.OCCUPY_TILE,
                newMessageBoard);

        return newGameState.withTurnFinishedIfOccupationImpossible();
    }

    /**
     * Méthode qui gère toutes les transitions à partir de RETAKE_PAWN, en supprimant l'occupant donné, sauf s'il vaut
     * null, ce qui indique que le joueur ne désire pas reprendre de pion
     * @param occupant l'occupant donné
     * @return l'état du jeu mis à jour
     * @throws IllegalArgumentException si la prochaine action n'est pas RETAKE_PAWN
     */
    public GameState withOccupantRemoved(Occupant occupant) {

        Preconditions.checkArgument((nextAction == Action.RETAKE_PAWN) &&
                ( (occupant == null) || (occupant.kind() == (Occupant.Kind.PAWN)) ) );

        Board newBoard = board;

        if (occupant != null)
            newBoard = newBoard.withoutOccupant(occupant);

        GameState newGameState = new GameState(players, tileDecks, null, newBoard, Action.OCCUPY_TILE,
                messageBoard);

        return newGameState.withTurnFinishedIfOccupationImpossible();
    }

    /**
     * Méthode qui gère toutes les transitions à partir de OCCUPY_TILE en ajoutant l'occupant donné à la dernière tuile
     * posée, sauf s'il vaut null, ce qui indique que le joueur ne désire pas placer
     * @param occupant l'occupant donné
     * @return l'etat du jeu mis à jour
     * @throws IllegalArgumentException si la prochaine action n'est pas OCCUPY_TILE
     */
    public GameState withNewOccupant(Occupant occupant) {

        Preconditions.checkArgument (nextAction == Action.OCCUPY_TILE);

        Board newBoard = board;

        if (occupant != null)
            newBoard = newBoard.withOccupant(occupant);

        GameState newGameState = new GameState(players, tileDecks, null, newBoard, nextAction, messageBoard);

        return newGameState.withTurnFinished();
    }

    /**
     * Methode qui se charge de la transition vers le tour suivant
     * @return un Game State de la même partie, mais avec le tour actuel fini
     */
    private GameState withTurnFinished() {

        List<PlayerColor> newPlayersList = new ArrayList<>(players);
        MessageBoard newMessageBoard = messageBoard;
        TileDecks newDecks = tileDecks;
        Tile nextTileToPlace;

        boolean canPlayAgain = false;

        Set<Area<Zone.River>> closedRiversSet = board.riversClosedByLastTile();

        for (Area<Zone.River> closedRiver : closedRiversSet)
            newMessageBoard = newMessageBoard.withScoredRiver(closedRiver);


        Set<Area<Zone.Forest>> closedForestsSet = board.forestsClosedByLastTile();

        for (Area<Zone.Forest> closedForest : closedForestsSet) {
            newMessageBoard = newMessageBoard.withScoredForest(closedForest);

            if ( Area.hasMenhir(closedForest) &&
                    (board.lastPlacedTile() != null) &&
                    board.lastPlacedTile().kind() == Tile.Kind.NORMAL) {

                newDecks = newDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, board::couldPlaceTile);

                if ( (newDecks.deckSize(Tile.Kind.MENHIR) > 0) && !canPlayAgain ) {
                    canPlayAgain = true; // pour afficher le message qu'une fois
                    newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), closedForest);
                }
            }
        }

        Board newBoard = board.withoutGatherersOrFishersIn(closedForestsSet, closedRiversSet);

        if (canPlayAgain) {
            // LE JOUEUR COURANT REJOUE
            nextTileToPlace = newDecks.topTile(Tile.Kind.MENHIR);
            newDecks = newDecks.withTopTileDrawn(Tile.Kind.MENHIR);

        } else {
            newDecks = newDecks.withTopTileDrawnUntil(Tile.Kind.NORMAL, newBoard::couldPlaceTile);

            if (newDecks.deckSize(Tile.Kind.NORMAL) > 0) {
                // ON PASSE AU JOUEUR SUIVANT
                Collections.rotate(newPlayersList, -1);
                nextTileToPlace = newDecks.topTile(Tile.Kind.NORMAL);
                newDecks = newDecks.withTopTileDrawn(Tile.Kind.NORMAL);

            } else {
                // FIN DU JEU
                GameState endGameState = new GameState(players, newDecks, null, newBoard, Action.END_GAME,
                        newMessageBoard);
                return endGameState.withFinalPointsCounted();
            }
        }

        return new GameState(newPlayersList, newDecks, nextTileToPlace, newBoard, Action.PLACE_TILE, newMessageBoard);
    }

    /**
     * Méthode qui retourne le même Game State si on peut occuper la dernière tuile posée, sinon met fin au tour actuel
     * @return le même Game State si on peut occuper la dernière tuile posée, sinon met fin au tour actuel
     */
    private GameState withTurnFinishedIfOccupationImpossible() {
        if (!lastTilePotentialOccupants().isEmpty())
            return this;

        return this.withTurnFinished();
    }

    /**
     * Méthode qui retourne un Game State avec le decompte final des points à la fin d'une partie
     * @return un Game State avec le decompte final des points à la fin d'une partie
     */
    private GameState withFinalPointsCounted() {

        Board newBoard = board;
        MessageBoard newMessageBoard = messageBoard;

        boolean hasWildFire;
        boolean hasPitTrap;
        boolean hasRaft;

        for (Area<Zone.Meadow> meadowArea : newBoard.meadowAreas()) {

            hasWildFire = meadowArea.zoneWithSpecialPower(Zone.SpecialPower.WILD_FIRE) != null;

            Zone pitTrapZone = meadowArea.zoneWithSpecialPower(Zone.SpecialPower.PIT_TRAP);
            hasPitTrap = pitTrapZone != null;

            Set<Animal> areaAnimalsSet = Area.animals(meadowArea, newBoard.cancelledAnimals());

            // WILD_FIRE et PIT_TRAP
            if (hasWildFire && hasPitTrap) {
                newMessageBoard = newMessageBoard.withScoredPitTrap(meadowArea, newBoard.cancelledAnimals());
            }

            // PIT_TRAP uniquement
            else if (!hasWildFire && hasPitTrap) {

                PlacedTile pitTrapTile = newBoard.tileWithId(pitTrapZone.tileId());

                Area<Zone.Meadow> adjacentMeadow = newBoard.adjacentMeadow(pitTrapTile.pos(), (Zone.Meadow) pitTrapZone);
                Set<Animal> adjacentAnimalsSet = Area.animals(adjacentMeadow, newBoard.cancelledAnimals());

                Set<Animal> allDeers = areaAnimalsSet.stream()
                        .filter(animal -> animal.kind() == Animal.Kind.DEER)
                        .collect(Collectors.toSet());

                Set<Animal> nonAdjacentDeers = allDeers.stream()
                        .filter(deer -> !adjacentAnimalsSet.contains(deer))
                        .collect(Collectors.toSet());

                int tigerCount = (int) areaAnimalsSet.stream()
                        .filter(animal -> animal.kind() == Animal.Kind.TIGER)
                        .count();

                Set<Animal> optimizedCancelledDeers = Stream
                        .concat(nonAdjacentDeers.stream(),allDeers.stream())
                        .distinct()
                        .limit(tigerCount)
                        .collect(Collectors.toSet());

                newBoard = newBoard.withMoreCancelledAnimals(optimizedCancelledDeers);
                newMessageBoard = newMessageBoard.withScoredPitTrap(adjacentMeadow, newBoard.cancelledAnimals());
            }

            // ni WILD_FIRE, ni PIT_TRAP
            else if (!hasWildFire){
                Set<Animal> cancelledDeers = cancelledDeers(areaAnimalsSet); // comme pour la HUNTING_TRAP
                newBoard = newBoard.withMoreCancelledAnimals(cancelledDeers);

            }

            newMessageBoard = newMessageBoard.withScoredMeadow(meadowArea, newBoard.cancelledAnimals());
        }

        for (Area<Zone.Water> riverSystem : newBoard.riverSystemAreas()) {

            hasRaft = riverSystem.zoneWithSpecialPower(Zone.SpecialPower.RAFT) != null;
            newMessageBoard = newMessageBoard.withScoredRiverSystem(riverSystem);

            if (hasRaft)
                newMessageBoard = newMessageBoard.withScoredRaft(riverSystem);
        }

        Map<PlayerColor, Integer> totalPoints = newMessageBoard.points();

        int maxPoints = totalPoints.isEmpty() ? 0 : Collections.max(totalPoints.values());
        Set<PlayerColor> winners = totalPoints
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == maxPoints)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        newMessageBoard = newMessageBoard.withWinners(winners, maxPoints);

        return new GameState(players, tileDecks, null, newBoard, nextAction, newMessageBoard);
    }

    /**
     * Méthode qui retourne les cerfs à annuler parmi un ensemble d'animaux donné d'une aire
     * @param areaAnimalsSet l'ensemble d'animaux donné
     * @return l'ensemble de cerfs à anunuler
     */
    private Set<Animal> cancelledDeers(Set<Animal> areaAnimalsSet) {

        int tigerCount = (int) areaAnimalsSet.stream()
                .filter(animal -> animal.kind() == Animal.Kind.TIGER)
                .count();

        Set<Animal> deersSet = areaAnimalsSet.stream()
                .filter(animal -> animal.kind() == Animal.Kind.DEER)
                .collect(Collectors.toSet());

        return deersSet.stream()
                .limit(Math.min(deersSet.size(), tigerCount))
                .collect(Collectors.toSet());
    }

}


