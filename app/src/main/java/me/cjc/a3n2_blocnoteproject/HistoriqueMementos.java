package me.cjc.a3n2_blocnoteproject;

import java.util.ArrayDeque;

public class HistoriqueMementos {
    private final int limite;
    private final ArrayDeque<MementoTexte> pileAnnuler = new ArrayDeque<>();
    private final ArrayDeque<MementoTexte> pileRefaire = new ArrayDeque<>();

    public HistoriqueMementos(int limite) {
        this.limite = limite;
    }

    public void enregistrer(MementoTexte memento) {
        empiler(pileAnnuler, memento);
    }

    public void viderRefaire() {
        pileRefaire.clear();
    }

    public MementoTexte annuler(MementoTexte courant) {
        if (pileAnnuler.isEmpty()) return null;
        empiler(pileRefaire, courant);
        return pileAnnuler.pop();
    }

    public MementoTexte refaire(MementoTexte courant) {
        if (pileRefaire.isEmpty()) return null;
        empiler(pileAnnuler, courant);
        return pileRefaire.pop();
    }

    private void empiler(ArrayDeque<MementoTexte> pile, MementoTexte memento) {
        pile.push(memento);
        if (pile.size() > limite) {
            pile.removeLast();
        }
    }
}