package ch.epfl.chacun;

/**
 * Classe qui permet d'encoder et de décoder des valeurs binaires en base32
 * @author Mohamed KHARRAT (314523)
 * @author Maha EL QABLI (372471)
 */

public final class Base32 {

    /**
     * Constructeur privé empêchant l'instanciation de la classe
     */
    private Base32() {}

    /**
     * L'alphabet de la base 32, soit, une chaîne contenant les caractères correspondant aux chiffres
     * en base 32, ordonnés par poids croissant
     */
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" ;

    /**
     * Méthode qui retourne vrai ssi la chaîne de caractères donnée n'est composée que de caractères de
     * l'alphabet base 32
     * @param str la chaîne de caractères donnée
     * @return vrai ssi la chaîne de caractères donnée n'est composée que de caractères de l'alphabet base 32
     */
    public static boolean isValid (String str) {

        int length = str.length();
        Preconditions.checkArgument(length==1 || length==2);

        for (int i = 0; i < length; i++) {
            char character = str.charAt(i);

            if (ALPHABET.indexOf(character) == -1)
                return false;
        }

        return true;
    }

    /**
     * Méthode qui retourne la chaîne de caractères de longueur 1 correspondant à l'encodage en base32 des 5 bits de
     * poids faible de la valeur donnée
     * @param value la valeur donnée
     * @return la chaîne de caractères de longueur 1 correspondant à l'encodage en base32 des 5 bits de poids faible
     * de la valeur donnée
     */
    public static String encodeBits5 (int value) {

        Preconditions.checkArgument(value >= 0);

        int mask = (1 << 5) - 1;
        int fiveLowestBits = value & mask;

        return String.valueOf(ALPHABET.charAt(fiveLowestBits));
    }

    /**
     * Méthode qui retourne la chaîne de caractères de longueur 2 correspondant à l'encodage en base32 des 10 bits de
     * poids faible de la valeur donnée
     * @param value la valeur donnée
     * @return la chaîne de caractères de longueur 2 correspondant à l'encodage en base32 des 10 bits de poids faible
     * de la valeur donnée
     */
    public static String encodeBits10 (int value) {

        Preconditions.checkArgument(value >= 0);

        return encodeBits5(value >> 5) + encodeBits5(value);
    }

    /**
     * Méthode qui retourne l'entier, correspondant à la chaîne de caractères (représentant un nombre en base32),
     * de longueur 1 ou 2, donnée
     * @param str la chaîne de caractères (représentant un nombre en base32), de longueur 1 ou 2, donnée
     * @return l'entier de type correspondant à la chaîne de caractères (représentant un nombre en base32),
     * de longueur 1 ou 2, donnée
     */
    public static int decode (String str) {

        int value = 0;
        int mask;

        if (isValid(str))
            for (int i = str.length()-1, shift = 0 ; i >= 0 ; i--, shift++) {
                char character = str.charAt(i);
                mask = ALPHABET.indexOf(character);
                value |= (mask << (5*shift));
            }

        return value;
    }

}

