package ch.epfl.chacun;

import java.util.*;

import static ch.epfl.chacun.Animal.Kind.TIGER;
import static java.lang.StringTemplate.STR;

/**
 * Enregristrement qui permet de générer tout le texte nécessaire à l'interface graphique du jeu (en francais)
 * @param fromColorsToNamesMap table associant les couleurs aux noms des joueurs
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record TextMakerFr(Map<PlayerColor, String> fromColorsToNamesMap) implements TextMaker {

    /**
     * Constructeur compact qui garantit l'immuabilité de la classe
     * @param fromColorsToNamesMap table associant les couleurs aux noms des joueurs
     */
    public TextMakerFr {
        fromColorsToNamesMap = Map.copyOf(fromColorsToNamesMap);
    }

    @Override
    public String playerName(PlayerColor playerColor) {
        return fromColorsToNamesMap.get(playerColor);
    }

    @Override
    public String points(int points) {
        return STR."\{points} point" + (points == 1 ? "" : "s");
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return STR."\{playerName(player)} a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.";
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'une forêt composée de \{tilesNumber(tileCount)}"
            + ( (mushroomGroupCount > 0) ? STR." et de \{mushroomNumber(mushroomGroupCount)}" : "" ) + ".";
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'une rivière composée de \{tilesNumber(tileCount)}"
            + ( (fishCount > 0) ? STR." et contenant \{fishNumber(fishCount)}" : "") + ".";
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
        return
              STR."\{playerName(scorer)} a remporté \{points(points)} en plaçant la fosse à pieux dans un pré dans "
            + STR."lequel elle est entourée de \{animalSequence(animals)}.";
    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        return
              STR."\{playerName(scorer)} a remporté \{points(points)} en plaçant la pirogue dans un réseau "
            + STR."hydrographique contenant \{lakeNumber(lakeCount)}.";
    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'un pré contenant \{animalSequence(animals)}.";
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'un réseau hydrographique contenant \{fishNumber(fishCount)}.";
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'un pré contenant la grande fosse à pieux entourée de \{animalSequence(animals)}.";
    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        int numberOfPlayers = scorers.size();

        return
              STR."\{nameSequence(scorers)} \{win(numberOfPlayers)} \{points(points)} \{majOccupants(numberOfPlayers)}"
            + STR." d'un réseau hydrographique contenant le radeau et \{lakeNumber(lakeCount)}.";
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        int numberOfWinners = winners.size();

        return STR."\{nameSequence(winners)} \{win(numberOfWinners)} la partie avec \{points(points)} !";
    }

    @Override
    public String clickToOccupy() {
        return "Cliquez sur le pion ou la hutte que vous désirez placer, ou ici pour ne pas en placer.";
    }

    @Override
    public String clickToUnoccupy() {
        return "Cliquez sur le pion que vous désirez reprendre, ou ici pour ne pas en reprendre.";
    }

    /**
     * Méthode qui retourne la représentation textuelle de l'ensemble de joueurs donné
     * @param players ensemble de joueurs donné
     * @return la représentation textuelle des noms de l'ensemble des joueurs donnés
     */
    private String nameSequence(Set<PlayerColor> players){

        StringBuilder b = new StringBuilder();

        players.stream()
                .sorted()
                .map(this::playerName)
                .forEach(player -> {
                    if (!b.isEmpty())
                        b.append(", ");

                    b.append(player);
                });

        return replaceCommaAndReturn(b);
    }

    /**
     * Methode qui retourne la représentation textuelle des animaux donnés
     * @param animalsMap table associant les animaux à leur nombre, donnée
     * @return la représentation textuelle des animaux donnés
     */
    private String animalSequence(Map<Animal.Kind, Integer> animalsMap) {

        StringBuilder b = new StringBuilder();

        animalsMap.entrySet().stream()
                .filter(animal -> animal.getKey() != TIGER)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (!b.isEmpty())
                        b.append(", ");

                    b.append(STR."\{entry.getValue()} ");

                    switch (entry.getKey()) {
                        case MAMMOTH -> b.append("mammouth");
                        case AUROCHS -> b.append("auroch");
                        case DEER -> b.append("cerf");
                    }

                    if (entry.getValue() > 1) {
                        b.append("s");
                    }
                });

        return replaceCommaAndReturn(b);

    }

    /**
     * Méthode qui retourne la chaine de caractères construite, et dont la dernière virgule est
     * remplacée par un "et"
     * @param b la chaine de caracteres en cours de construction
     * @return la chaine de caractères construite, et dont la dernière virgule est remplacée par un "et"
     */
    private String replaceCommaAndReturn(StringBuilder b) {
        int indexOfLastComma = b.lastIndexOf(","); // retourne -1 si le string ne contient pas de virgule
        if (indexOfLastComma != -1)
            b.replace(indexOfLastComma, indexOfLastComma + 1, " et");

        return b.toString();
    }

    /**
     * Méthode qui retourne "a remporté", accordé au pluriel si l'ensemble contient plusieurs joueurs
     * @param numberOfPlayers le nombre de joueurs
     * @return "a remporté", accordé au pluriel si l'ensemble contient plusieurs joueurs
     */
    private String win(int numberOfPlayers) {
        return STR."\{numberOfPlayers == 1 ? "a" : "ont"} remporté";
    }

    /**
     * Méthode qui retourne "en tant qu'occupant·e majoritaire", accordé au pluriel si l'ensemble contient
     * plusieurs joueurs
     * @param numberOfPlayers le nombre de joueurs
     * @return "en tant qu'occupant·e majoritaire", accordé au pluriel si l'ensemble contient plusieurs joueurs
     */
    private String majOccupants(int numberOfPlayers) {
        return STR."en tant qu'occupant·e\{numberOfPlayers == 1 ? " majoritaire" : "·s majoritaires"}";
    }

    /**
     * Méthode qui retourne la représentation textuelle du nombre de tuiles donné, accordée au pluriel s'il est
     * supérieur à un
     * @param tileCount nombre de tuiles donné
     * @return la représentation textuelle du nombre de tuiles donné, accordée au pluriel s'il est supérieur à un
     */
    private String tilesNumber(int tileCount) {
        return STR."\{tileCount} tuile" + (tileCount == 1 ? "" : "s");
    }

    /**
     * Méthode qui retourne la représentation textuelle du nombre de groupes de champignons donné, accordée au pluriel
     * s'il est supérieur à un
     * @param mushroomGroupCount nombre de groupes de champignons donné
     * @return la représentation textuelle du nombre de groupes de champignons donné, accordée au pluriel s'il est
     * supérieur à un
     */
    private String mushroomNumber(int mushroomGroupCount) {
        return STR."\{mushroomGroupCount} groupe" + (mushroomGroupCount == 1 ? "" : "s") + " de champignons";
    }

    /**
     * Méthode qui retourne la représentation textuelle du nombre de poissons donné, accordée au pluriel s'il est
     * supérieur à un
     * @param fishCount nombre de poissons donné
     * @return la représentation textuelle du nombre de poissons donné, accordée au pluriel s'il est supérieur à un
     */
    private String fishNumber(int fishCount) {
        return STR."\{fishCount} poisson" + (fishCount == 1 ? "" : "s");
    }

    /**
     * Méthode qui retourne la représentation textuelle du nombre de lacs donné, accordée au pluriel s'il est
     * supérieur à un
     * @param lakeCount nombre de lacs donné
     * @return la représentation textuelle du nombre de lacs donné, accordée au pluriel s'il est supérieur à un
     */
    private String lakeNumber(int lakeCount) {
        return STR."\{lakeCount} lac" + (lakeCount == 1 ? "" : "s");
    }

}
