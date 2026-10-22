package me.cjc.a3n2_blocnoteproject;

import java.util.ArrayDeque;

/**
 * Historique des memento de texte.
 */
public class HistoriqueMementos {
    /**
     * Limite de la pile.
     */
    private final int limite;
    /**
     * Pile pour les undo.
     */
    private final ArrayDeque<MementoTexte> pileAnnuler = new ArrayDeque<>();
    /**
     * Pile pour les redo.
     */
    private final ArrayDeque<MementoTexte> pileRefaire = new ArrayDeque<>();

    /**
     * Constructeur de l'historique.
     * @param limite Limite de la pile.
     */
    public HistoriqueMementos(int limite) {
        this.limite = limite;
    }

    /**
     * Enregistre un memento de texte.
     * @param memento Memento à enregistrer.
     */
    public void enregistrer(MementoTexte memento) {
        empiler(pileAnnuler, memento);
    }

    /**
     * Vide la pile de redo.
     */
    public void viderRefaire() {
        pileRefaire.clear();
    }

    /**
     * Annule le dernier changement de texte.
     * @param courant Memento actuel.
     * @return Retourne le memento annulé.
     */
    public MementoTexte annuler(MementoTexte courant) {
        if (pileAnnuler.isEmpty()) return null;
        empiler(pileRefaire, courant);
        return pileAnnuler.pop();
    }

    /**
     * Refait le dernier changement de texte.
     * @param courant Memento actuel.
     * @return Retourne le memento refait.
     */
    public MementoTexte refaire(MementoTexte courant) {
        if (pileRefaire.isEmpty()) return null;
        empiler(pileAnnuler, courant);
        return pileRefaire.pop();
    }

    /**
     * Empile un memento dans une pile.
     * @param pile Pile à empiler.
     * @param memento Memento à empiler.
     */
    private void empiler(ArrayDeque<MementoTexte> pile, MementoTexte memento) {
        pile.push(memento);
        if (pile.size() > limite) {
            pile.removeLast();
        }
    }
}