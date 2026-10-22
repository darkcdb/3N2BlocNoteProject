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

public class EditeurActivity extends AppCompatActivity {

    private GestionnaireBDNotes bd;
    private Note note;
    private EditText champContenu;

    private final HistoriqueMementos historique = new HistoriqueMementos(10);
    private MementoTexte avant;
    private boolean restauration = false;

    private final ActivityResultLauncher<String[]> demandePermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultats -> {
                if (Boolean.TRUE.equals(resultats.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
                    obtenirPosition();
                } else {
                    Toast.makeText(this, R.string.permissionRefusee, Toast.LENGTH_SHORT).show();
                }
            });

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

    private int compterMots(String texte) {
        String t = texte.trim();
        return t.isEmpty() ? 0 : t.split("\\s+").length;
    }

    private MementoTexte creerMemento() {
        return new MementoTexte(
                champContenu.getText().toString(),
                champContenu.getSelectionStart());
    }

    private void restaurer(MementoTexte memento) {
        restauration = true;
        champContenu.setText(memento.getTexte());
        int position = Math.max(0, Math.min(memento.getCurseur(), memento.getTexte().length()));
        champContenu.setSelection(position);
        restauration = false;
    }

    private void annuler() {
        MementoTexte memento = historique.annuler(creerMemento());
        if (memento != null) {
            restaurer(memento);
        }
    }

    private void refaire() {
        MementoTexte memento = historique.refaire(creerMemento());
        if (memento != null) {
            restaurer(memento);
        }
    }

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

    private void enregistrer() {
        String lContenu = champContenu.getText().toString();
        if (!lContenu.equals(note.contenu)) {
            note.contenu = lContenu;
            bd.modifier(note);
        }
        Toast.makeText(this, getString(R.string.sauvegardeEffectuee), Toast.LENGTH_SHORT).show();
    }

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