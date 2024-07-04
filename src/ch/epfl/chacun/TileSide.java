package ch.epfl.chacun;

import java.util.List;

/**
 * Interface scellée qui représente un bord de tuile
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public sealed interface TileSide {

    /**
     * Méthode qui retourne une liste des zones touchant le bord donné
     * @return une liste des zones touchant le bord donné
     */
    List<Zone> zones();

    /**
     * Méthode qui vérifie que le bord donné est du meme type que celui du récepteur
     * @return vrai ssi le bord donné est du meme type que celui du récepteur, faux sinon
     */
    boolean isSameKindAs(TileSide that);

    /**
     * Enregistrement qui représente un bord de tuile forêt
     * @param forest la forêt qui touche le bord
     */
    record Forest(Zone.Forest forest) implements TileSide {

        /**
         * Méthode qui retourne une liste des zones touchant le bord de type foret donné
         * @return une liste des zones touchant le bord de type foret donné
         */
        @Override
        public List<Zone> zones() {
            return List.of(forest);
        }

        /**
         * Méthode qui vérifie que le bord donné est de type foret
         * @return vrai ssi le bord donné est de type foret, faux sinon
         */
        @Override
        public boolean isSameKindAs(TileSide that) {
            return (that instanceof TileSide.Forest);
        }
    }

    /**
     * Enregistrement qui représente un bord de tuile pré
     * @param meadow le pré qui touche le bord
     */
    record Meadow(Zone.Meadow meadow) implements TileSide {

        /**
         * Méthode qui retourne une liste des zones touchant le bord de type pré donné
         * @return une liste des zones touchant le bord de type pré donné
         */
        @Override
        public List<Zone> zones() {
            return List.of(meadow);
        }

        /**
         * Méthode qui vérifie que le bord donné est de type pré
         * @return vrai ssi le bord donné est de type pré, faux sinon
         */
        @Override
        public boolean isSameKindAs(TileSide that) { return (that instanceof TileSide.Meadow); }
    }

    /**
     * Enregistrement qui représente un bord de tuile rivière
     * @param meadow1 le premier pré qui entoure la rivière et touche le bord
     * @param river la rivière qui touche le bord
     * @param meadow2 le second pré qui entoure la rivière et touche le bord
     */
    record River(Zone.Meadow meadow1, Zone.River river, Zone.Meadow meadow2) implements TileSide {

        /**
         * Méthode qui retourne une liste des zones touchant le bord de type rivière donné
         * @return une liste des zones touchant le bord de type rivière donné
         */
        @Override
        public List<Zone> zones() {
            return List.of(meadow1, river, meadow2);
        }

        /**
         * Méthode qui vérifie que le bord donné est de type rivière
         * @return vrai ssi le bord donné est de type rivière, faux sinon
         */
        @Override
        public boolean isSameKindAs(TileSide that) {
            return (that instanceof TileSide.River);
        }

    }

}
