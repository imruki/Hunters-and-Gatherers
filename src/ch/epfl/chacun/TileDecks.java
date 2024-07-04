package ch.epfl.chacun;

import java.util.List;
import java.util.function.Predicate;

/**
 * Enregistrement qui représente une tuile qui a été placée
 * @param startTiles la tuile de départ (ou rien du tout)
 * @param normalTiles les tuiles normales restantes
 * @param menhirTiles les tuiles menhir restantes
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public record TileDecks(List<Tile> startTiles, List<Tile> normalTiles, List<Tile> menhirTiles) {

    /**
     * Constructeur compact qui garantit l'immuabilité de la classe
     */
    public TileDecks {
        startTiles = List.copyOf(startTiles);
        normalTiles = List.copyOf(normalTiles);
        menhirTiles = List.copyOf(menhirTiles);
    }

    /**
     * Méthode qui retourne le nombre de tuiles disponibles dans le tas contenant les tuiles de la sorte donnée
     * @param kind la sorte de la tuile donnée
     * @return le nombre de tuiles disponibles dans le tas contenant les tuiles de la sorte donnée
     */
    public int deckSize(Tile.Kind kind){

        return switch (kind) {
            case START -> startTiles.size();
            case NORMAL -> normalTiles.size();
            case MENHIR -> menhirTiles.size();
        };
    }

    /**
     * Méthode qui retourne la tuile au sommet du tas contenant les tuiles de la sorte donnée ou null si le tas est vide
     * @param kind la sorte de la tuile donnée
     * @return la tuile au sommet du tas contenant les tuiles de la sorte donnée ou null si le tas est vide
     */
    public Tile topTile(Tile.Kind kind){

        return switch (kind) {
            case START -> startTiles.isEmpty() ? null : startTiles.getFirst();
            case NORMAL -> normalTiles.isEmpty() ? null : normalTiles.getFirst();
            case MENHIR -> menhirTiles.isEmpty() ? null : menhirTiles.getFirst();
        };
    }

    /**
     * Méthode qui retourne un nouveau triplet de tas sans la tuile au sommet du tas de la sorte donnée
     * @param kind : sorte de la tuile donnée
     * @return un nouveau triplet de tas sans la tuile au sommet du tas de la sorte donnée
     * @throws IllegalArgumentException si le tas est vide (si la taille du tas n'est pas strictement positive)
     */
    public TileDecks withTopTileDrawn(Tile.Kind kind) {

        Preconditions.checkArgument(deckSize(kind) > 0);

        return switch (kind) {
            case START -> new TileDecks(startTiles.subList(1, startTiles.size()), normalTiles, menhirTiles);
            case NORMAL -> new TileDecks(startTiles, normalTiles.subList(1, normalTiles.size()), menhirTiles);
            case MENHIR -> new TileDecks(startTiles, normalTiles, menhirTiles.subList(1, menhirTiles.size()));
        };
    }

    /**
     * Méthode qui retourne un nouveau triplet de tas sans les tuiles au sommet du tas de la sorte donnée,
     * qui sont impossibles à placer
     * @param kind la sorte de la tuile donnée
     * @param predicate le prédicat à respecter
     * @return un nouveau triplet de tas sans les tuiles au sommet du tas de la sorte donnée,
     * qui sont impossibles à placer
     */
    public TileDecks withTopTileDrawnUntil(Tile.Kind kind, Predicate<Tile> predicate) {

        TileDecks newDecks = new TileDecks(startTiles, normalTiles, menhirTiles);
        Tile topTile = newDecks.topTile(kind);

        while (topTile != null && !predicate.test(topTile)) {
            newDecks = newDecks.withTopTileDrawn(kind);
            topTile = newDecks.topTile(kind);
        }

        return newDecks;
    }

}
