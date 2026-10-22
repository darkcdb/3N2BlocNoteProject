package me.cjc.a3n2_blocnoteproject;

/**
 * Memento de texte.
 */
public final class MementoTexte {
    /**
     * Contenu de la note.
     */
    private final String texte;
    /**
     * Curseur de la note.
     */
    private final int curseur;

    /**
     * Constructeur de MementoTexte.
     * @param texte Contenu de la note.
     * @param curseur Curseur de la note.
     */
    public MementoTexte(String texte, int curseur) {
        this.texte = texte;
        this.curseur = curseur;
    }

    /**
     * Récupère le contenu de la note.
     * @return Retourne le contenu de la note.
     */
    public String getTexte() {
        return texte;
    }

    /**
     * Récupère le curseur de la note.
     * @return Retourne le curseur de la note.
     */
    public int getCurseur() {
        return curseur;
    }
}