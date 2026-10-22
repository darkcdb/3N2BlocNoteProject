package me.cjc.a3n2_blocnoteproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Activité principale de l'application.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Gestionnaire de la base de données.
     */
    private GestionnaireBDNotes bd;
    /**
     * Adapteur de la liste des notes.
     */
    private AdapteurDeNote adapteur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bd = new GestionnaireBDNotes(this);

        RecyclerView recycler = findViewById(R.id.recyclerNotes);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));

        adapteur = new AdapteurDeNote(new AdapteurDeNote.Listener() {
            @Override
            public void onClickNote(Note note) {
                if (note.estVerrouillee()) {
                    Toast.makeText(MainActivity.this, getString(R.string.impossibleVerrouiller), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intention = new Intent(MainActivity.this, EditeurActivity.class);
                    intention.putExtra("idNote", note.id);
                    startActivity(intention);
                }
            }
            @Override
            public void onClickModifier(Note note) {
                afficherMenuModification(note);
            }
        });

        recycler.setAdapter(adapteur);
        findViewById(R.id.boutonAjouter).setOnClickListener(v -> afficherNoteCreationPopup("", ""));
        if (savedInstanceState == null) {
            traiterIntentImport(getIntent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapteur.setNotes(bd.obtenirTous());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        traiterIntentImport(intent);
    }

    /**
     * Affiche un popup de création de note.
     * @param contenuInitial Contenu de la note.
     * @param titreInitial Titre de la note.
     */
    private void afficherNoteCreationPopup(String contenuInitial, String titreInitial) {
        View vue = getLayoutInflater().inflate(R.layout.popup_creation_note, null);
        EditText champTitre = vue.findViewById(R.id.champTitre);
        champTitre.setText(titreInitial);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.titreLayoutPopupCreation)
                .setView(vue)
                .setNegativeButton(R.string.popupAnnuler, null)
                .setPositiveButton(R.string.popupCreationCree, (dialogue, bouton) -> {
                    String lTitre = champTitre.getText().toString().trim();
                    Note note = new Note(lTitre);
                    note.contenu = contenuInitial;
                    bd.ajouter(note);
                    adapteur.setNotes(bd.obtenirTous());
                })
                .show();
    }


    /**
     * Affiche un menu de modification de note.
     * @param note Note à modifier.
     */
    private void afficherMenuModification(Note note) {
        String[] options = {
                getString(R.string.titreLayoutModificationTitre),
                getString(R.string.OptionSupprimerNote),
                getString(note.estVerrouillee()
                        ? R.string.titreLayoutDeVerrNote
                        : R.string.titreLayoutVerrNote)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.titreLayoutModificationNote)
                .setItems(options, (dialogue, choix) -> {
                    switch (choix) {
                        case 0:
                            if (!note.estVerrouillee()) {
                                afficherModificationTitre(note);
                            } else {
                                Toast.makeText(this, getString(R.string.impossibleVerrouiller), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1:
                            afficherModificationSuppression(note);
                            break;
                        case 2:
                            afficherModificationVerrouillage(note, !note.estVerrouillee());
                            break;
                    }
                })
                .show();
    }

    /**
     * Affiche un popup de modification de verrouillage de note.
     * @param note Note à modifier.
     * @param verouiller Status de la note. (Vérouiller ou non)
     */
    private void afficherModificationVerrouillage(Note note, boolean verouiller) {
        View vue = getLayoutInflater().inflate(R.layout.popup_verrou, null);
        EditText champNip = vue.findViewById(R.id.champNip);
        boolean estVerrouillee = note.estVerrouillee();
        String lTitre = (estVerrouillee) ? getString(R.string.titreLayoutDeVerrNote) : getString(R.string.titreLayoutVerrNote);
        int lPosBouton = (estVerrouillee) ? R.string.OptionDeVerrouiller : R.string.OptionVerrouiller;

        if (verouiller) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(lTitre)
                    .setView(vue)
                    .setNegativeButton(R.string.popupAnnuler, null)
                    .setPositiveButton(lPosBouton, (dialogue, bouton) -> {
                        String lNip = champNip.getText().toString().trim();
                        if ((lNip.length() == 4 || lNip.length() == 5) && Integer.parseInt(lNip) > 0) {
                            String lNipHash = OutilsNip.hash(note.id, lNip);
                            bd.definirNip(note.id, lNipHash);
                            adapteur.setNotes(bd.obtenirTous());
                        } else {
                            Toast.makeText(this, getString(R.string.popupNipErreur), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(lTitre)
                    .setView(vue)
                    .setNegativeButton(R.string.popupAnnuler, null)
                    .setPositiveButton(lPosBouton, (dialogue, bouton) -> {
                        String lNip = champNip.getText().toString().trim();
                        if ((lNip.length() == 4 || lNip.length() == 5) && Integer.parseInt(lNip) > 0) {
                            String lNipHash = OutilsNip.hash(note.id, lNip);
                            if (OutilsNip.estMemeHash(lNipHash, note.nipHash)) {
                                bd.definirNip(note.id, null);
                                adapteur.setNotes(bd.obtenirTous());
                            } else {
                                Toast.makeText(this, getString(R.string.popupNipIncorrect), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.popupNipErreur), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }

    /**
     * Affiche un popup de suppression de note.
     * @param note Note à supprimer.
     */
    private void afficherModificationSuppression(Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.titreLayoutSupprimerNote))
                .setNegativeButton(R.string.popupAnnuler, null)
                .setPositiveButton(R.string.OptionSupprimerNote, (dialogue, bouton) -> {
                    bd.supprimer(note.id);
                    adapteur.setNotes(bd.obtenirTous());
                })
                .show();
    }

    /**
     * Affiche un popup de modification de titre de note.
     * @param note Note à modifier.
     */
    private void afficherModificationTitre(Note note) {
        View vue = getLayoutInflater().inflate(R.layout.popup_modification_titre, null);
        EditText champTitre = vue.findViewById(R.id.champTitre);
        champTitre.setText(note.titre);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.titreLayoutModificationTitre)
                .setView(vue)
                .setNegativeButton(R.string.popupAnnuler, null)
                .setPositiveButton(R.string.popupEnregistrer, (dialogue, bouton) -> {
                    String lTitre = champTitre.getText().toString().trim();
                    Note note2 = new Note(note.id, lTitre, note.contenu,
                            note.dateCreation, note.dateModification, note.nipHash);
                    bd.modifier(note2);
                    adapteur.setNotes(bd.obtenirTous());
                })
                .show();
    }

    /**
     * Traite l'intent d'import.
     * @param intention Intention à traiter.
     */
    private void traiterIntentImport(Intent intention) {
        if (intention == null) return;

        String action = intention.getAction();
        CharSequence texte = null;

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(intention.getType())) {
            texte = intention.getCharSequenceExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            texte = intention.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        }

        if (texte != null && !texte.toString().trim().isEmpty()) {
            String contenu = texte.toString();
            afficherNoteCreationPopup(contenu, suggererTitre(contenu));
        }
    }

    /**
     * Génère un titre à partir du contenu de la note.
     * @param texte Contenu de la note.
     * @return Retourne une suggestion de titre.
     */
    private String suggererTitre(String texte) {
        String premiereLigne = texte.trim().split("\\R", 2)[0].trim();
        return premiereLigne.length() > 30
                ? premiereLigne.substring(0, 30).trim()
                : premiereLigne;
    }
}