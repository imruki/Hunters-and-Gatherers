package ch.epfl.chacun;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Classe qui contient des méthodes permettant d'encoder et de décoder des (paramètres) d'actions,
 * et d'appliquer ces actions à un état de jeu
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public class ActionEncoder {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private ActionEncoder() {}

    /**
     * Enregristrement qui représente une paire constituée d'un état de jeu résultant de l'application d'une action
     * à un état de jeu initial, et d'une chaîne de caractère qui est l'encodage, en base32, de cette action
     */
    public record StateAction (GameState gameState, String actionString) {}

    /**
     * L'encodage, en base32, correspondant au cas où une action (OCCUPY_TILE ou RETAKE_PAWN) est sautée
     */
    private final static int PASS_ACTION = 0b11111;

    /**
     * Méthode qui retourne une paire de type StateAction, constituée d'un état de jeu résultant de l'application
     * de la pose d'une tuile à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage, en base32,
     * de cette action
     * @param gameState l'état de jeu donné
     * @param placedTile la tuile à placer donnée
     * @return une paire de type StateAction, constituée d'un état de jeu résultant de l'application d'une pose
     * d'une tuile à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage, en base32,
     * de cette action
     */
    public static StateAction withPlacedTile(GameState gameState, PlacedTile placedTile) {

        GameState newGameState = gameState.withPlacedTile(placedTile);

        List <Pos> sortedFringe = sortedFringe(gameState);
        Pos tilePos = placedTile.pos();

        int indexInFringe = sortedFringe.indexOf(tilePos);
        int rotationNumber = placedTile.rotation().quarterTurnsCW();

        int value = (indexInFringe << 2) | rotationNumber;

        String actionString = Base32.encodeBits10(value);

        return new StateAction (newGameState, actionString);
    }

    /**
     * Méthode qui retourne une paire de type StateAction, constituée d'un état de jeu résultant de l'application
     * de l'occupation d'une tuile, à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage,
     * en base32, de cette action
     * @param gameState l'état de jeu donné
     * @param occupant l'occupant donné
     * @return une paire de type StateAction, constituée d'un état de jeu résultant de l'application de l'occupation
     * d'une tuile à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage, en base32,
     * de cette action
     */
    public static StateAction withNewOccupant(GameState gameState, Occupant occupant) {

        GameState newGameState = gameState.withNewOccupant(occupant);

        int value;
        if (occupant != null) {
            int occupantKindNumber = occupant.kind().ordinal();
            int occupantZoneNumber = Zone.localId(occupant.zoneId());
            value = (occupantKindNumber << 4) | occupantZoneNumber;

        } else {
            value = PASS_ACTION;
        }

        String actionString = Base32.encodeBits5(value);

        return new StateAction (newGameState, actionString);
    }

    /**
     * Méthode qui retourne une paire de type StateAction, constituée d'un état de jeu résultant de l'application
     * de la reprise d'un occupant, à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage,
     * en base32, de cette action
     * @param gameState l'état de jeu donné
     * @param occupant l'occupant donné
     * @return une paire de type StateAction, constituée d'un état de jeu résultant de l'application de la reprise
     * d'un occupant, à l'état de jeu donné, et d'une chaîne de caractère qui est l'encodage, en base32,
     * de cette action
     */
    public static StateAction withOccupantRemoved(GameState gameState, Occupant occupant) {

        GameState newGameState = gameState.withOccupantRemoved(occupant);

        int value;

        if (occupant != null) {
            List<Occupant> sortedPawns = sortedPawns(gameState);
            value = sortedPawns.indexOf(occupant);
        }
        else {
            value = PASS_ACTION;
        }

        String actionString = Base32.encodeBits5(value);

        return new StateAction (newGameState, actionString);
    }

    /**
     * Méthode qui retourne une paire de type StateAction, constituée de l'état de jeu résultant de l'application
     * d'une action donnée à l'état de jeu donné, et de la chaîne de caractères représentant l'encodage en base32,
     * de cette action
     * @param gameState l'état de jeu donné
     * @param actionString la chaîne de caractères, représentant l'encodage en base32, d'une action donnée
     * @return une paire de type StateAction, constituée de l'état de jeu résultant de l'application
     * d'une action donnée, à l'état de jeu donné, et de la chaîne de caractères représentant l'encodage en base32,
     * de cette action
     */
    public static StateAction decodeAndApply(GameState gameState, String actionString) {

        try {
            return decodeOrThrow(gameState, actionString);
        }
        catch (IllegalArgumentException e) {
            return null;
        }

    }

    /**
     * Méthode qui retourne une paire de type StateAction, constituée de l'état de jeu résultant de l'application
     * d'une action donnée à l'état de jeu donné, et de la chaîne de caractères représentant l'encodage en base32,
     * de cette action
     * @param gameState l'état de jeu donné
     * @param actionString la chaîne de caractère représentant l'encodage en base32, d'une action donnée
     * @throws IllegalArgumentException si la chaîne de caractère représentant l'encodage en base32,
     * de l'action donnée, n'est pas valide
     */
    private static StateAction decodeOrThrow(GameState gameState, String actionString)
            throws IllegalArgumentException {

        Preconditions.checkArgument(Base32.isValid(actionString));

        int action = Base32.decode(actionString);
        int strLength = actionString.length();

        GameState newGameState = null;

        switch (gameState.nextAction()) {

            case PLACE_TILE -> {

                Preconditions.checkArgument(strLength == 2);

                int posIndex = (action >> 2);
                List<Pos> sortedFringe = sortedFringe(gameState);
                Preconditions.checkArgument(posIndex>=0 && posIndex < sortedFringe.size());

                Pos tilePos = sortedFringe.get(posIndex);

                int rotationNumber = nLowestBits(2, action);
                Rotation tileRotation = Rotation.ALL.get(rotationNumber);

                PlacedTile placedTile = new PlacedTile(gameState.tileToPlace(), gameState.currentPlayer(),
                        tileRotation, tilePos, null);

                newGameState = gameState.withPlacedTile(placedTile);

                return new StateAction(newGameState, actionString);
            }

            case OCCUPY_TILE -> {

                Preconditions.checkArgument(strLength == 1);

                if (action == PASS_ACTION)
                    newGameState = gameState.withNewOccupant(null);

                else {
                    int localId = nLowestBits(4, action);
                    int occupantKindIndex = (action >> 4);

                    for (Occupant occupant : gameState.lastTilePotentialOccupants()) {
                        if ( (Zone.localId(occupant.zoneId()) == localId) &&
                                (occupant.kind() == Occupant.Kind.values()[occupantKindIndex]) )
                            newGameState = gameState.withNewOccupant(occupant);
                    }
                }

                return new StateAction(newGameState, actionString);
            }

            case RETAKE_PAWN -> {

                Preconditions.checkArgument(strLength == 1);

                if (action == PASS_ACTION)
                    newGameState = gameState.withOccupantRemoved(null);
                else {
                    List<Occupant> sortedPawns = sortedPawns(gameState);
                    Preconditions.checkArgument(action>=0 && action < sortedPawns.size());

                    Occupant occupant = sortedPawns.get(action);

                    int occupantTileId = Zone.tileId(occupant.zoneId());
                    PlacedTile occupantTile = gameState.board().tileWithId(occupantTileId);

                    if (occupant.kind() != Occupant.Kind.PAWN
                            || occupantTile.placer() != gameState.currentPlayer())
                        throw new IllegalArgumentException();

                    newGameState = gameState.withOccupantRemoved(occupant);
                }

                return new StateAction(newGameState, actionString);
            }

            default -> throw new IllegalArgumentException();
        }

    }

    /**
     * Méthode qui retourne une liste contenant les positions d'insertions disponibles (de la frange) de l'état de
     * jeu donné, triées dans l'ordre croissant, d'abord selon leur coordonnée x, puis selon leur coordonnée y
     * @param gameState l'état de jeu donné
     */
    private static List <Pos> sortedFringe (GameState gameState) {
        Set <Pos> fringe = gameState.board().insertionPositions();

        return fringe.stream()
                .sorted(Comparator.comparingInt(Pos::x)
                        .thenComparingInt(Pos::y))
                .toList();
    }

    /**
     * Méthode qui retourne une liste contenant les pions du plateau de l'état de jeu donné,
     * triés par ordre croissant selon l'identifiant de la zone qu'ils occupent
     * @param gameState l'état de jeu donné
     */
    private static List <Occupant> sortedPawns(GameState gameState) {

        Set <Occupant> allOccupants = gameState.board().occupants();

        return allOccupants.stream()
                .filter(o -> o.kind() == Occupant.Kind.PAWN)
                .sorted(Comparator.comparingInt(Occupant::zoneId))
                .toList();
    }

    /**
     * Méthode qui extrait le nombre de bits de poids faible donné, d'une valeur entière donnée
     * @param n le nombre de bits de poids faible donné
     * @param value la valeur entière donnée
     */
    private static int nLowestBits(int n, int value) {
        int mask = (1 << n) - 1;
        return value & mask;
    }

}

