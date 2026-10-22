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

public class MainActivity extends AppCompatActivity {

    private GestionnaireBDNotes bd;
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
                Intent intention = new Intent(MainActivity.this, EditeurActivity.class);
                intention.putExtra("idNote", note.id);
                startActivity(intention);
            }
            @Override
            public void onClickModifier(Note note) {
                afficherMenuModification(note);
            }
        });

        /*
        adapteur = new AdapteurDeNote(note -> {
            Intent intention = new Intent(this, EditeurActivity.class);
            intention.putExtra("idNote", note.id);
            startActivity(intention);
        });
         */

        recycler.setAdapter(adapteur);
        findViewById(R.id.boutonAjouter).setOnClickListener(v -> afficherNoteCreationPopup());
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapteur.setNotes(bd.obtenirTous());
    }

    private void afficherNoteCreationPopup() {
        View vue = getLayoutInflater().inflate(R.layout.popup_creation_note, null);
        EditText champTitre = vue.findViewById(R.id.champTitre);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.titreLayoutPopupCreation)
                .setView(vue)
                .setNegativeButton(R.string.popupAnnuler, null)
                .setPositiveButton(R.string.popupCreationCree, (dialogue, bouton) -> {
                    String lTitre = champTitre.getText().toString().trim();
                    Note note = new Note(lTitre);
                    bd.ajouter(note);
                    adapteur.setNotes(bd.obtenirTous());
                })
                .show();
    }



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
                            afficherModificationTitre(note);
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
}