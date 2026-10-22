package me.cjc.a3n2_blocnoteproject;

public final class MementoTexte {
    private final String texte;
    private final int curseur;

    public MementoTexte(String texte, int curseur) {
        this.texte = texte;
        this.curseur = curseur;
    }

    public String getTexte() {
        return texte;
    }

    public int getCurseur() {
        return curseur;
    }
}