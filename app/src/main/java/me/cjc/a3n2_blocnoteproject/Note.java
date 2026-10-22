package me.cjc.a3n2_blocnoteproject;

/**
 * Classe Note.
 */
public class Note {
    /**
     * Id de la note.
     */
    public int id;
    /**
     * Titre de la note.
     */
    public String titre;
    /**
     * Contenu de la note.
     */
    public String contenu;
    /**
     * Date de création de la note.
     */
    public long dateCreation;
    /**
     * Date de modification de la note.
     */
    public long dateModification;
    /**
     * La hash du nip de la note. null = Pas vérouillé
     */
    public String nipHash;
    /**
     * Id du prochain objet Note à créer.
     */
    private static int idSuivant = 1;

    /**
     * Constructeur de note éxistante pour le chargement a partir de la base de données.
     */
    public Note(int id, String titre, String contenu, long dateCreation, long dateModification, String nipHash) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.nipHash = nipHash;

        if (id >= idSuivant) idSuivant = id + 1;
    }

    /**
     * Constructeur de nouvelle note avec titre.
     */
    public Note(String titre) {
        long maintenant = System.currentTimeMillis();

        this.id = idSuivant++;
        this.titre = titre;
        this.contenu = "";
        this.dateCreation = maintenant;
        this.dateModification = maintenant;
        this.nipHash = null;
    }

    /**
     * Constructeur de nouvelle note sans titre.
     */
    public Note() {
        long maintenant = System.currentTimeMillis();

        this.id = idSuivant++;
        this.titre = "";
        this.contenu = "";
        this.dateCreation = maintenant;
        this.dateModification = maintenant;
        this.nipHash = null;
    }

    /**
     * Vérifie si la note est vérouillée.
     * @return Retourne true si la note est vérouillée, false sinon.
     */
    public boolean estVerrouillee() {
        return nipHash != null && !nipHash.isEmpty();
    }
}
