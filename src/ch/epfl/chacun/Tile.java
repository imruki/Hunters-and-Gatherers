package ch.epfl.chacun;

import java.util.*;

/**
 * Enregistrement qui représente une tuile qui n'a pas encore été placée
 * @param id l'identifiant de la tuile
 * @param kind la sorte de la tuile
 * @param n le côté nord (north) de la tuile
 * @param e le côté est (east) de la tuile
 * @param s le côté sud (south) de la tuile
 * @param w le côté ouest (west) de la tuile
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record Tile(int id, Kind kind, TileSide n, TileSide e, TileSide s, TileSide w) {

    /**
     * Enumeration qui contient les differentes sortes de tuile du jeu
     */
    public enum Kind{ START, NORMAL, MENHIR }

    /**
     * Méthode qui retourne la liste des 4 côtés de la tuile (dans le sens horaire)
     * @return la liste des 4 côtés de la tuile
     */
    public List<TileSide> sides(){
        return List.of(n, e, s, w);
    }

    /**
     * Méthode qui retourne l'ensemble des zones de bordure de la tuile
     * @return l'ensemble des zones de bordure de la tuile
     */
    public Set<Zone> sideZones(){

        Set<Zone> sideZonesSet = new HashSet<>();

        for (TileSide tileSide : sides())
            sideZonesSet.addAll(tileSide.zones());

        return sideZonesSet;
    }

    /**
     * Méthode qui retourne l'ensemble de toutes les zones de la tuile (lacs compris)
     * @return l'ensemble de toutes les zones de la tuile (lacs compris)
     */
    public Set<Zone> zones() {

        Set<Zone> zonesSet = new HashSet<>();

        for (Zone zone : sideZones()) {
            zonesSet.add(zone);
            if (zone instanceof Zone.River river && river.hasLake())
                zonesSet.add(river.lake());
        }
        return zonesSet;
    }

}
