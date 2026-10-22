package me.cjc.a3n2_blocnoteproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Classe GestionnaireBDNotes pour gérer la base de données de l'application.
 */
public class GestionnaireBDNotes extends SQLiteOpenHelper {

    /**
     * Constructeur de GestionnaireBDNotes.
     * @param context Contexte de la BD.
     */
    public GestionnaireBDNotes(@Nullable Context context) {
        super(context, "BDNotes", null, 2);
    }

    /**
     * Méthode appelée lors de la création de la base de données.
     * @param db La Base de données.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY, titre TEXT, contenu TEXT, dateCreation INTEGER, dateModification INTEGER, nipHash TEXT)");
    }

    /**
     *
     * @param db La Base de données.
     * @param oldVersion La vieille version de la BD.
     * @param newVersion La nouvelle version de la BD.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS notes");
        //onCreate(db);
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE notes ADD COLUMN nipHash TEXT");
        }
    }

    /**
     * Méthode pour ajouter une note à la base de données.
     * @param note La note a ajouter.
     */
    public void ajouter(Note note) {
        ContentValues valeurs = new ContentValues();
        valeurs.put("id", note.id);
        valeurs.put("titre", note.titre);
        valeurs.put("contenu", note.contenu);
        valeurs.put("dateCreation", note.dateCreation);
        valeurs.put("dateModification", note.dateModification);
        valeurs.put("nipHash", note.nipHash);
        getWritableDatabase().insert("notes", null, valeurs);
    }

    /**
     * Méthode pour supprimer une note de la base de données.
     * @param id le id de la note à supprimer.
     */
    public void supprimer(int id) {
        getWritableDatabase().delete("notes", "id = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Méthode pour modifier une note de la base de données.
     * @param note Note à modifier.
     */
    public void modifier(Note note) {
        note.dateModification = System.currentTimeMillis();

        ContentValues valeurs = new ContentValues();
        valeurs.put("titre", note.titre);
        valeurs.put("contenu", note.contenu);
        valeurs.put("dateModification", note.dateModification);
        getWritableDatabase().update("notes", valeurs, "id=?", new String[]{String.valueOf(note.id)});
    }

    public void definirNip(int id, String nipHash) {
        ContentValues valeurs = new ContentValues();
        valeurs.put("nipHash", nipHash);
        getWritableDatabase().update("notes", valeurs, "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Méthode pour créer un objet Note à partir d'un curseur de BD.
     * @param curseur Le curseur de la BD.
     * @return Retourne un objet Note.
     */
    private Note creationNoteParCurseur(Cursor curseur) {
        return new Note(curseur.getInt(0), curseur.getString(1), curseur.getString(2), curseur.getLong(3), curseur.getLong(4), curseur.getString(5));
    }

    /**
     * Méthode pour obtenir une note de la base de données.
     * @param id Identifiant de la Note à obtenir.
     * @return retourne un objet Note.
     */
    public Note obtenir(int id) {
        Note note = null;

        Cursor curseur = getReadableDatabase().query("notes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);

        if (curseur.moveToFirst()) {
            note = creationNoteParCurseur(curseur);
        }

        curseur.close();

        return note;
    }

    /**
     * Méthode pour obtenir toutes les notes de la base de données.
     * @return Retourne en ArrayList toute les notes de la BD sous forme d'objet Note.
     */
    public ArrayList<Note> obtenirTous() {
        ArrayList<Note> notes = new ArrayList<>();

        Cursor curseur = getReadableDatabase().query("notes", null, null, null, null, null, "dateModification DESC");

        while (curseur.moveToNext()) {
            notes.add(creationNoteParCurseur(curseur));

        }

        curseur.close();
        return notes;
    }
}
