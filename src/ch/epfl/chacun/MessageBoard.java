package ch.epfl.chacun;

import java.util.*;

/**
 * Enregistrement qui représente le contenu du tableau d'affichage
 * @param textMaker l'objet permettant d'obtenir le texte des différents messages
 * @param messages la liste des messages affichés sur le tableau, du plus ancien au plus récent
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record MessageBoard(TextMaker textMaker, List<Message> messages) {

    /**
     * Constructeur compact qui garantit l'immuabilité de la classe
     */
    public MessageBoard {
        messages = List.copyOf(messages);
    }

    /**
     * Méthode qui retourne une table associant à tous les joueurs figurant dans les gagnants (scorers) d'au moins
     * un message, le nombre total de points obtenus
     * @return une table associant à tous les joueurs figurant dans les gagnants le nombre total de points obtenus
     */
    public Map<PlayerColor, Integer> points() {

        Map<PlayerColor, Integer> pointsMap = new HashMap<>();

        for (Message message : messages) {
            for (PlayerColor scorer : message.scorers)
                pointsMap.merge(scorer, message.points, Integer::sum);

        }

        return pointsMap;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si la forêt donnée est occupée,
     * auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté
     * les points associés à sa fermeture
     * @param forest la forêt donnée
     * @return un tableau d'affichage identique au récepteur, sauf si la forêt donnée est occupée, auquel cas
     * le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points
     * associés à sa fermeture
     */
    public MessageBoard withScoredForest(Area<Zone.Forest> forest) {

        if (forest.isOccupied()) {
            Set<PlayerColor> forestMajorityOccupants = forest.majorityOccupants();

            Set<Integer> tileIds = forest.tileIds();
            int tileCount = tileIds.size();
            int mushroomGroupCount = Area.mushroomGroupCount(forest);

            int forestPoints = Points.forClosedForest(tileCount, mushroomGroupCount);

            String textMessage = textMaker.playersScoredForest(forestMajorityOccupants, forestPoints,
                    mushroomGroupCount, tileCount);
            List<Message> updatedMessages = updatedMessagesList (textMessage, forestPoints, forestMajorityOccupants,
                    tileIds);

            return new MessageBoard(textMaker, updatedMessages);
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le
     * joueur donné a le droit de jouer un second tour après avoir fermé la forêt donnée, car elle contient un ou
     * plusieurs menhirs
     * @param player le joueur donné
     * @param forest la forêt donnée
     * @return un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le joueur donné
     * a le droit de jouer un second tour après avoir fermé la forêt donnée, car elle contient un ou plusieurs menhirs
     */
    public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest) {

        String textMessage = textMaker.playerClosedForestWithMenhir(player);
        List<Message> updatedMessages = updatedMessagesList (textMessage, 0, Collections.emptySet(),
                forest.tileIds());

        return new MessageBoard(textMaker, updatedMessages);
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si la rivière donnée est occupée, auquel
     * cas le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points
     * associés à sa fermeture
     * @param river la rivière donnée
     * @return un tableau d'affichage identique au récepteur, sauf si la rivière donnée est occupée, auquel cas
     * le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points associés
     * à sa fermeture
     */
    public MessageBoard withScoredRiver(Area<Zone.River> river) {

        if (river.isOccupied()) {
            Set<PlayerColor> riverMajorityOccupants = river.majorityOccupants();

            Set<Integer> tileIds = river.tileIds();
            int tileCount = tileIds.size();
            int fishCount = Area.riverFishCount(river);

            int riverPoints = Points.forClosedRiver(tileCount, fishCount);

            String textMessage = textMaker.playersScoredRiver(riverMajorityOccupants, riverPoints, fishCount, tileCount);
            List<Message> updatedMessages = updatedMessagesList (textMessage, riverPoints, riverMajorityOccupants,
                    tileIds);

            return new MessageBoard(textMaker, updatedMessages);
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si la pose de la fosse à pieux a permis
     * au joueur donné, qui l'a posée, de remporter des points, auquel cas le tableau contient un nouveau message
     * signalant cela- le pré donné comportant les mêmes occupants que le pré contenant la fosse, mais uniquement les
     * zones se trouvant à sa portée ; les cerfs donnés ne sont pas pris en compte lors du décompte des points
     *
     * @param scorer le joueur donné
     * @param adjacentMeadow le pré donné
     * @param cancelledDeers les cerfs annulés donnés
     * @return un tableau d'affichage identique au récepteur, sauf si la pose de la fosse à pieux a permis au joueur
     * donné, qui l'a posée, de remporter des points, auquel cas le tableau contient un nouveau message signalant cela-
     * le pré donné comportant les mêmes occupants que le pré contenant la fosse, mais uniquement les zones se trouvant
     * à sa portée
     */
    public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow,
                                              Set<Animal> cancelledDeers) {

        Set<Animal> animalSet = Area.animals(adjacentMeadow, cancelledDeers);

        Map<Animal.Kind, Integer> animalMap = new HashMap<>();

        for (Animal animal : animalSet)
            animalMap.merge(animal.kind(), 1, Integer::sum);

        int mammothCount = animalMap.getOrDefault(Animal.Kind.MAMMOTH, 0);
        int aurochsCount = animalMap.getOrDefault(Animal.Kind.AUROCHS, 0);
        int deerCount = animalMap.getOrDefault(Animal.Kind.DEER, 0);

        int adjacentMeadowPoints = Points.forMeadow(mammothCount, aurochsCount, deerCount);

        if (adjacentMeadowPoints > 0) {
            String textMessage = textMaker.playerScoredHuntingTrap(scorer, adjacentMeadowPoints, animalMap);
            List<Message> updatedMessages = updatedMessagesList (textMessage, adjacentMeadowPoints, Set.of(scorer),
                    adjacentMeadow.tileIds());

            return new MessageBoard(textMaker, updatedMessages);
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que
     * le joueur donné a obtenu les points correspondants à la pose de la pirogue dans le réseau hydrographique donné
     *
     * @param scorer le joueur donné
     * @param riverSystem le réseau hydrographique donné
     * @return un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le joueur donné
     * a obtenu les points correspondants à la pose de la pirogue dans le réseau hydrographique donné
     */
    public MessageBoard withScoredLogboat(PlayerColor scorer, Area<Zone.Water> riverSystem) {

        int lakeCount = Area.lakeCount(riverSystem);
        int logboatPoints = Points.forLogboat(lakeCount);

        String textMessage = textMaker.playerScoredLogboat(scorer, logboatPoints, lakeCount);
        List<Message> updatedMessages = updatedMessagesList (textMessage, logboatPoints, Set.of(scorer),
                riverSystem.tileIds());

        return new MessageBoard(textMaker, updatedMessages);
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si le pré donné est occupé et que les
     * points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un nouveau
     * message signalant que ces joueurs-là ont remporté les points en question ; les animaux donnés ne sont pas
     * pris en compte lors du décompte des points
     *
     * @param meadow le pré donné
     * @param cancelledAnimals les animaux annulés donnés
     * @return un tableau d'affichage identique au récepteur, sauf si le pré donné est occupé et que les points qu'il
     * rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un nouveau message
     * signalant que ces joueurs-là ont remporté les points en question
     */
    public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals) {

        if (meadow.isOccupied()) {

            Set<PlayerColor> meadowMajorityOccupants = meadow.majorityOccupants();
            Set<Animal> animalSet = Area.animals(meadow, cancelledAnimals);

            Map<Animal.Kind, Integer> animalMap = new HashMap<>();
            for (Animal animal : animalSet)
                animalMap.merge(animal.kind(), 1, Integer::sum);

            int meadowPoints = meadowPoints(animalMap);

            if (meadowPoints > 0) {
                String textMessage = textMaker.playersScoredMeadow(meadowMajorityOccupants, meadowPoints, animalMap);
                List<Message> updatedMessages = updatedMessagesList (textMessage, meadowPoints, meadowMajorityOccupants,
                        meadow.tileIds());

                return new MessageBoard(textMaker, updatedMessages);
            }
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné est
     * occupé et que les points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau
     * contient un nouveau message signalant que ces joueurs-là ont remporté les points en question
     *
     * @param riverSystem le réseau hydrographique donné
     * @return un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné est occupé et que
     * les points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un
     * nouveau message signalant que ces joueurs-là ont remporté les points en question
     */
    public MessageBoard withScoredRiverSystem(Area<Zone.Water> riverSystem) {

        if (riverSystem.isOccupied()) {
            Set<PlayerColor> riverSystemMajorityOccupants = riverSystem.majorityOccupants();

            int fishCount = Area.riverSystemFishCount(riverSystem);

            int riverSystemPoints = Points.forRiverSystem(fishCount);

            if (riverSystemPoints > 0) {
                String textMessage = textMaker.playersScoredRiverSystem(riverSystemMajorityOccupants, riverSystemPoints,
                        fishCount);
                List<Message> updatedMessages = updatedMessagesList (textMessage, riverSystemPoints,
                        riverSystemMajorityOccupants, riverSystem.tileIds());

                return new MessageBoard(textMaker, updatedMessages);
            }
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si le pré donné, qui contient
     * la grande fosse à pieux, est occupé et que les points qu'il rapporte à ses occupants majoritaires sont
     * supérieurs à 0, auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté
     * les points en question ; les animaux donnés ne sont pas pris en compte lors du décompte des points
     *
     * @param adjacentMeadow le pré donné
     * @param cancelledAnimals les animaux annulés donnés
     * @return un tableau d'affichage identique au récepteur, sauf si le pré donné, qui contient la grande
     * fosse à pieux, est occupé et que les points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0,
     * auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté les points
     * en question
     */
    public MessageBoard withScoredPitTrap(Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals) {

        if (adjacentMeadow.isOccupied()) {

            Set<PlayerColor> adjacentMeadowMajorityOccupants = adjacentMeadow.majorityOccupants();

            Set<Animal> animalSet = Area.animals(adjacentMeadow, cancelledAnimals);

            Map<Animal.Kind, Integer> animalMap = new HashMap<>();
            for (Animal animal : animalSet)
                animalMap.merge(animal.kind(), 1, Integer::sum);

            int adjacentMeadowPoints = meadowPoints(animalMap);

            if (adjacentMeadowPoints > 0) {
                String textMessage = textMaker.playersScoredPitTrap(adjacentMeadowMajorityOccupants,
                        adjacentMeadowPoints, animalMap);
                List<Message> updatedMessages = updatedMessagesList (textMessage, adjacentMeadowPoints,
                        adjacentMeadowMajorityOccupants, adjacentMeadow.tileIds());

                return new MessageBoard(textMaker, updatedMessages);
            }
        }

        return this;
    }

    /**
     * Méthode qui retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné, qui
     * contient le radeau, est occupé, auquel cas le tableau contient un nouveau message signalant que ses occupants
     * majoritaires ont remporté les points correspondants
     *
     * @param riverSystem le réseau hydrographique donné
     * @return un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné, qui contient le
     * radeau, est occupé, auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires
     * ont remporté les points correspondants
     */
    public MessageBoard withScoredRaft(Area<Zone.Water> riverSystem) {

        if (riverSystem.isOccupied()) {
            Set<PlayerColor> riverSystemMajorityOccupants = riverSystem.majorityOccupants();

            int lakeCount = Area.lakeCount(riverSystem);

            int riverSystemPoints = Points.forRaft(lakeCount);

            String textMessage = textMaker.playersScoredRaft(riverSystemMajorityOccupants, riverSystemPoints, lakeCount);
            List<Message> updatedMessages = updatedMessagesList (textMessage, riverSystemPoints,
                    riverSystemMajorityOccupants, riverSystem.tileIds());

            return new MessageBoard(textMaker, updatedMessages);
        }

        return this;
    }

        /**
         * Méthode qui retourne un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant
         * que le(s) joueur(s) donné(s) a/ont remporté la partie avec le nombre de points donnés
         *
         * @param winners les joueurs gagnants donnés
         * @param points le nombre de points donnés
         * @return un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le(s)
         * joueur(s) donné(s) a/ont remporté la partie avec le nombre de points donnés
         */
        public MessageBoard withWinners (Set <PlayerColor> winners, int points){

            String textMessage = textMaker.playersWon(winners, points);
            List<Message> updatedMessages = updatedMessagesList (textMessage, 0, Collections.emptySet(),
                    Collections.emptySet());

            return new MessageBoard(textMaker, updatedMessages);
        }

    /**
     * Méthode qui retourne la liste de messages mise à jour (qui contient le nouveau)
     * @param textMessage le texte du message
     * @param points les points associés au message
     * @param scorers l'ensemble des joueurs ayant remporté les points
     * @param tileIds les identifiants des tuiles concernées par le message
     * @return la liste de messages mise à jour
     */
    private List<Message> updatedMessagesList (String textMessage, int points, Set<PlayerColor> scorers,
                                          Set<Integer> tileIds) {
        Message message = new Message(textMessage, points, scorers, tileIds);
        List<Message> updatedMessages = new ArrayList<>(messages);
        updatedMessages.add(message);

        return updatedMessages;
    }

    /**
     * Méthode qui retourne le nombre de points obtenus par les chasseurs majoritaires d'un pré, compte tenu des animaux
     * présents dans le pré
     * @param animalMap table associant chaque animal à son nombre
     * @return le nombre de points obtenus par les chasseurs majoritaires d'un pré
     */
    private int meadowPoints(Map<Animal.Kind, Integer> animalMap) {

        int mammothCount = animalMap.getOrDefault(Animal.Kind.MAMMOTH, 0);
        int aurochsCount = animalMap.getOrDefault(Animal.Kind.AUROCHS, 0);
        int deerCount = animalMap.getOrDefault(Animal.Kind.DEER, 0);

        return Points.forMeadow(mammothCount, aurochsCount, deerCount);
    }

    /**
         * Enregistrment qui représente un message affiché sur le tableau d'affichage
         * @param text le texte du message
         * @param points les points associés au message
         * @param scorers l'ensemble des joueurs ayant remporté les points
         * @param tileIds les identifiants des tuiles concernées par le message
         */
    public record Message(String text, int points, Set<PlayerColor> scorers, Set<Integer> tileIds) {

        /**
         * Constructeur compact validant les arguments qui lui sont passés et garantissant l'immuabilité des
         * ensembles
         * @throws NullPointerException si le texte passé est null
         * @throws IllegalArgumentException si points est strictement négatif
         */
        public Message {
                Objects.requireNonNull(text);
                Preconditions.checkArgument(points >= 0);
                scorers = Set.copyOf(scorers);
                tileIds = Set.copyOf(tileIds);
        }

    }

}