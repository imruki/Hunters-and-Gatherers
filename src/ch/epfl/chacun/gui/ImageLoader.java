package ch.epfl.chacun.gui;

import javafx.scene.image.Image;

/**
 * Classe qui a pour but de charger les images des tuiles
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class ImageLoader {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private ImageLoader() {
    }

    /**
     * Taille des grandes tuiles
     */
    public static final int LARGE_TILE_PIXEL_SIZE = 512;

    /**
     * Taille d'affichage des grandes tuiles
     */
    public static final int LARGE_TILE_FIT_SIZE = LARGE_TILE_PIXEL_SIZE/2;

    /**
     * Taille des tuiles normales
     */
    public static final int NORMAL_TILE_PIXEL_SIZE = 256;

    /**
     * Taille d'affichage des tuiles normales
     */
    public static final int NORMAL_TILE_FIT_SIZE = NORMAL_TILE_PIXEL_SIZE/2;

    /**
     * Taille du marqueur
     */
    public static final int MARKER_PIXEL_SIZE = 96;

    /**
     * Taille d'affichage du marqueur
     */
    public static final int MARKER_FIT_SIZE = MARKER_PIXEL_SIZE/2;

    /**
     * Méthode qui retourne l'image de 256 pixels de côté de la face de la tuile donnée
     * @param tileId identifiant de la tuile donnée
     * @return l'image de 256 pixels de côté de la face de la tuile donnée
     */
    public static Image normalImageForTile(int tileId) {
        return new Image(String.format("/%d/%02d.jpg", NORMAL_TILE_PIXEL_SIZE, tileId));
    }

    /**
     * Méthode qui retourne l'image de 512 pixels de côté de la face de la tuile donnée
     * @param tileId identifiant de la tuile donnée
     * @return l'image de 512 pixels de côté de la face de la tuile donnée
     */
    public static Image largeImageForTile(int tileId) {
        return new Image(String.format("/%d/%02d.jpg", LARGE_TILE_PIXEL_SIZE, tileId));
    }

}
