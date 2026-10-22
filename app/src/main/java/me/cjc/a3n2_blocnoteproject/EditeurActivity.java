package me.cjc.a3n2_blocnoteproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

/**
 * Activité de l'éditeur de note.
 */
public class EditeurActivity extends AppCompatActivity {
    /**
     * Gestionnaire de la base de données.
     */
    private GestionnaireBDNotes bd;
    /**
     * Note à éditer.
     */
    private Note note;
    /**
     * Champ de contenu de la note.
     */
    private EditText champContenu;
    /**
     * Historique des changements de contenu.
     */
    private final HistoriqueMementos historique = new HistoriqueMementos(10);
    /**
     * Memento avant le changement de contenu.
     */
    private MementoTexte avant;
    /**
     * Flag de restauration de contenu.
     */
    private boolean restauration = false;

    /**
     * Demande de permission de localisation.
     */
    private final ActivityResultLauncher<String[]> demandePermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultats -> {
                if (Boolean.TRUE.equals(resultats.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
                    obtenirPosition();
                } else {
                    Toast.makeText(this, R.string.permissionRefusee, Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Constructeur de l'activité.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editeur);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bd = new GestionnaireBDNotes(this);
        champContenu = findViewById(R.id.champContenu);

        int idNote = getIntent().getIntExtra("idNote", -1);
        note = bd.obtenir(idNote);

        if (note == null) {
            finish();
            return;
        }

        champContenu.setText(note.contenu);

        champContenu.setSelection(champContenu.getText().length());

        champContenu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                avant = creerMemento();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (restauration) return;

                historique.viderRefaire();

                if (compterMots(s.toString()) != compterMots(avant.getTexte())) {
                    historique.enregistrer(avant);
                }
            }
        });

        TextView barreTitre = findViewById(R.id.barreTitreEditeur);
        barreTitre.setText(note.titre.trim().isEmpty()
                ? getString(R.string.carteSansTitreText)
                : note.titre);

        findViewById(R.id.boutonRetour).setOnClickListener(v -> fermerNote());
        findViewById(R.id.boutonSauvegarde).setOnClickListener(v -> enregistrer());
        findViewById(R.id.boutonUndo).setOnClickListener(v -> annuler());
        findViewById(R.id.boutonRedo).setOnClickListener(v -> refaire());
        findViewById(R.id.boutonLocalisation).setOnClickListener(v -> demanderPosition());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                fermerNote();
            }
        });
    }

    /**
     * Compte le nombre de mots dans un texte.
     * @param texte Contenu de la note actuelle.
     * @return Retourne le nombre de mots dans le texte.
     */
    private int compterMots(String texte) {
        String t = texte.trim();
        return t.isEmpty() ? 0 : t.split("\\s+").length;
    }

    /**
     * Créer un memento de texte.
     * @return Retourne un nouvel Objet MementoTexte.
     */
    private MementoTexte creerMemento() {
        return new MementoTexte(
                champContenu.getText().toString(),
                champContenu.getSelectionStart());
    }

    /**
     * Restaurer le contenu de la note.
     * @param memento Memento à restaurer.
     */
    private void restaurer(MementoTexte memento) {
        restauration = true;
        champContenu.setText(memento.getTexte());
        int position = Math.max(0, Math.min(memento.getCurseur(), memento.getTexte().length()));
        champContenu.setSelection(position);
        restauration = false;
    }

    /**
     * Annuler le dernier changement du texte.
     */
    private void annuler() {
        MementoTexte memento = historique.annuler(creerMemento());
        if (memento != null) {
            restaurer(memento);
        }
    }

    /**
     * Refaire le dernier changement du texte.
     */
    private void refaire() {
        MementoTexte memento = historique.refaire(creerMemento());
        if (memento != null) {
            restaurer(memento);
        }
    }

    /**
     * Fermer la note avec vérification de sauvegarde.
     */
    private void fermerNote() {
        String lContenu = champContenu.getText().toString();

        if (lContenu.equals(note.contenu)) {
            finish();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.titreFermetureNoteSansSauvegarde)
                    .setNegativeButton(R.string.popupAnnuler, null)
                    .setPositiveButton(R.string.popupFermer, (dialogue, bouton) -> finish())
                    .show();
        }
    }

    /**
     * Enregistrer le contenu de la note dans la BD.
     */
    private void enregistrer() {
        String lContenu = champContenu.getText().toString();
        if (!lContenu.equals(note.contenu)) {
            note.contenu = lContenu;
            bd.modifier(note);
        }
        Toast.makeText(this, getString(R.string.sauvegardeEffectuee), Toast.LENGTH_SHORT).show();
    }

    /**
     * Vérification de permissions, demande de permissions ou redirection vers obtenir la position GPS de l'appareil.
     */
    private void demanderPosition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenirPosition();
        } else {
            demandePermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    /**
     * Méthode qui obtien la position GPS de l'appareil avant d'appeler la méthode insererPosition.
     */
    @SuppressLint("MissingPermission")
    private void obtenirPosition() {
        LocationManager gestionnaire = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!gestionnaire.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, R.string.gpsDesactive, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.positionRecherche, Toast.LENGTH_SHORT).show();

        CancellationSignal annulation = new CancellationSignal();
        Handler delai = new Handler(Looper.getMainLooper());
        Runnable expiration = annulation::cancel;
        delai.postDelayed(expiration, 20000);   // give up after 20 seconds

        LocationManagerCompat.getCurrentLocation(
                gestionnaire,
                LocationManager.GPS_PROVIDER,
                annulation,
                ContextCompat.getMainExecutor(this),
                location -> {
                    delai.removeCallbacks(expiration);
                    if (isFinishing()) return;
                    if (location == null) {
                        Toast.makeText(this, R.string.positionIntrouvable, Toast.LENGTH_LONG).show();
                    } else {
                        insererPosition(location);
                    }
                });
    }

    /**
     * Insère la position GPS dans le contenu de la note.
     * @param location
     */
    private void insererPosition(Location location) {
        String latitude = String.format(Locale.getDefault(), "%.6f", location.getLatitude());
        String longitude = String.format(Locale.getDefault(), "%.6f", location.getLongitude());
        String texte = getString(R.string.positionTexte, latitude, longitude);

        int debut = Math.max(0, champContenu.getSelectionStart());
        int fin = Math.max(0, champContenu.getSelectionEnd());
        int gauche = Math.min(debut, fin);
        int droite = Math.max(debut, fin);

        champContenu.getText().replace(gauche, droite, texte);
        champContenu.setSelection(gauche + texte.length());
    }
}