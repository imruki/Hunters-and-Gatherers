package ch.epfl.chacun;

/**
 * Enregistrement qui regroupe les 4 partitions de zones du jeu
 * @param forests la partition des forêts
 * @param meadows la partition des prés
 * @param rivers la partition des rivières
 * @param riverSystems la partition des zones aquatiques (rivières et lacs)
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record ZonePartitions(ZonePartition<Zone.Forest> forests, ZonePartition<Zone.Meadow> meadows,
                             ZonePartition<Zone.River> rivers, ZonePartition<Zone.Water> riverSystems) {

    /**
     * Représente un groupe de 4 partitions vides
     */
    public final static ZonePartitions EMPTY = new ZonePartitions(new ZonePartition<>(),new ZonePartition<>(),
            new ZonePartition<>(),new ZonePartition<>());

    /**
     * Bâtisseur des 4 partitions du jeu (ZonePartitions)
     */
    public static final class Builder{
        private final ZonePartition.Builder<Zone.Forest> forestBuilder;
        private final ZonePartition.Builder<Zone.Meadow> meadowBuilder;
        private final ZonePartition.Builder<Zone.River> riverBuilder;
        private final ZonePartition.Builder<Zone.Water> riverSystemBuilder;

        /**
         * Constructeur qui retourne un nouveau bâtisseur dont les 4 partitions sont initialement identiques
         * à celles du groupe des 4 partitions donné
         * @param initial le groupe de quatre partitions donné
         */
        public Builder(ZonePartitions initial){
            this.forestBuilder = new ZonePartition.Builder<>(initial.forests);
            this.meadowBuilder = new ZonePartition.Builder<>(initial.meadows);
            this.riverBuilder = new ZonePartition.Builder<>(initial.rivers);
            this.riverSystemBuilder = new ZonePartition.Builder<>(initial.riverSystems);
        }

        /**
         * Méthode qui ajoute aux 4 partitions les aires correspondant aux zones de la tuile donnée
         * @param tile la tuile donnée
         */
        public void addTile(Tile tile) {

            int[] openConnectionsTab = new int[10];

            for (TileSide tileSide : tile.sides()) {

                for (Zone zone : tileSide.zones()) {
                    openConnectionsTab[zone.localId()]++;

                    if (zone instanceof Zone.River river && river.hasLake()) {
                        openConnectionsTab[river.localId()]++;
                        openConnectionsTab[river.lake().localId()]++;
                    }
                }
            }

            for (Zone zone : tile.zones()) {
                int opConnections = openConnectionsTab[zone.localId()];
                switch (zone) {
                    case Zone.Forest forest -> forestBuilder.addSingleton(forest, opConnections);
                    case Zone.Meadow meadow -> meadowBuilder.addSingleton(meadow, opConnections);
                    case Zone.River river -> {
                        riverSystemBuilder.addSingleton(river, opConnections);
                        riverBuilder.addSingleton(river, river.hasLake() ? opConnections - 1 : opConnections);
                    }
                    case Zone.Lake lake -> riverSystemBuilder.addSingleton(lake, opConnections);
                }
            }

            for (Zone zone : tile.zones()) {
                if (zone instanceof Zone.River river && river.hasLake())
                    riverSystemBuilder.union(river, river.lake());
            }

        }

        /**
         * Méthode qui connecte les deux bords de tuiles donnés, en connectant entre elles les aires correspondantes
         * @param s1 premier bord donné
         * @param s2 deuxième bord donné
         * @throws IllegalArgumentException si les deux bords ne sont pas de la même sorte
         */

        public void connectSides(TileSide s1, TileSide s2){

            switch (s1) {
                case TileSide.Forest(Zone.Forest f1)
                    when s2 instanceof TileSide.Forest(Zone.Forest f2) -> forestBuilder.union(f1, f2);

                case TileSide.Meadow(Zone.Meadow m1)
                    when s2 instanceof TileSide.Meadow(Zone.Meadow m2) -> meadowBuilder.union(m1, m2);

                case TileSide.River(Zone.Meadow firstMeadow1, Zone.River r1, Zone.Meadow secondMeadow1)
                    when s2 instanceof TileSide.River(Zone.Meadow firstMeadow2, Zone.River r2,
                                                      Zone.Meadow secondMeadow2) -> {
                        meadowBuilder.union(firstMeadow1, secondMeadow2);
                        meadowBuilder.union(secondMeadow1, firstMeadow2);
                        riverBuilder.union(r1, r2);
                        riverSystemBuilder.union(r1, r2);
                    }
                default -> throw new IllegalArgumentException("les deux bords ne sont pas de la même sorte");
            }

        }

        /**
         * Méthode qui ajoute un occupant initial, de la sorte donnée et appartenant au joueur donné, à l'aire
         * contenant la zone donnée
         * @param player le joueur donné
         * @param occupantKind la sorte donnée
         * @param occupiedZone la zone donnée
         * @throws IllegalArgumentException si la sorte d'occupant donnée ne peut pas occuper une zone de la sorte
         * donnée
         */

        public void addInitialOccupant(PlayerColor player, Occupant.Kind occupantKind, Zone occupiedZone){
            switch (occupiedZone) {
                case Zone.Forest occupiedForest
                    when occupantKind == Occupant.Kind.PAWN -> forestBuilder.addInitialOccupant(occupiedForest, player);
                case Zone.Meadow occupiedMeadow
                    when occupantKind == Occupant.Kind.PAWN -> meadowBuilder.addInitialOccupant(occupiedMeadow, player);
                case Zone.River occupiedRiver
                    when occupantKind == Occupant.Kind.PAWN -> riverBuilder.addInitialOccupant(occupiedRiver, player);
                case Zone.Water occupiedWater
                    when occupantKind == Occupant.Kind.HUT -> riverSystemBuilder.addInitialOccupant(occupiedWater,
                        player);
                default -> throw new IllegalArgumentException("la sorte d'occupant donnée ne peut pas occuper une " +
                        "zone de la sorte donnée");
            }
        }

        /**
         * Méthode qui supprime un pion appartenant au joueur donné de l'aire contenant la zone donnée
         * @param player le joueur donné
         * @param occupiedZone la zone donnée
         * @throws IllegalArgumentException si la zone est un lac
         */
        public void removePawn(PlayerColor player, Zone occupiedZone){
            switch (occupiedZone) {
                case Zone.Forest occupiedForest -> forestBuilder.removeOccupant(occupiedForest, player);
                case Zone.Meadow occupiedMeadow -> meadowBuilder.removeOccupant(occupiedMeadow, player);
                case Zone.River occupiedRiver -> riverBuilder.removeOccupant(occupiedRiver, player);

                default -> throw new IllegalArgumentException("la zone est un lac et donc ne peut pas contenir un pion");
            }
        }

        /**
         * Méthode qui supprime tous les cueilleurs de la forêt donnée
         * @param forest la forêt donnée
         */
        public void clearGatherers(Area<Zone.Forest> forest) {
            forestBuilder.removeAllOccupantsOf(forest);
        }

        /**
         * Méthode qui supprime tous les pêcheurs de la rivière donnée
         * @param river la rivière donnée
         */
        public void clearFishers(Area<Zone.River> river) {
            riverBuilder.removeAllOccupantsOf(river);
        }

        /**
         * Méthode qui retourne le groupe de quatre partitions en cours de construction
         * @return le groupe de quatre partitions en cours de construction
         */
        public ZonePartitions build(){
            return new ZonePartitions(forestBuilder.build(), meadowBuilder.build(), riverBuilder.build(),
                    riverSystemBuilder.build());
        }
    }

}