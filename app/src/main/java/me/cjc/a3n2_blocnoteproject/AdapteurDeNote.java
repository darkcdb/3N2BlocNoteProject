package me.cjc.a3n2_blocnoteproject;

import android.content.Context;
import java.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class AdapteurDeNote extends RecyclerView.Adapter<AdapteurDeNote.NoteHolder> {
    /**
     * Formatage de la date pour l'affichage.
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    /**
     * Liste des notes.
     */
    private final ArrayList<Note> notes = new ArrayList<>();

    /**
     * Interface de type Listener pour la gestion du clic sur une note.
     */
    public interface Listener {
        void onClick(Note note);
    }

    /**
     * Listener de clic sur une note.
     */
    private final Listener listener;

    /**
     * Créer une nouvelle vue.
     * @param parent Le groupe de vue dans lequel la vue va être ajouter.
     * @param viewType Le type de vue de la nouvelle vue.
     *
     * @return Retourne un NoteHolder de la nouvelle vue.
     */
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext()).inflate(R.layout.carte_note, parent, false);
        return new NoteHolder(vue);
    }

    /**
     * Met à jour le contenu de la vue.
     * @param holder Le ViewHolder qui est mise a jour pour représenté le contenu de la note à la position donné.
     * @param position La position  de la note dans le data set de l'adapteur.
     */
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Context contexte = holder.itemView.getContext();
        Note note = notes.get(position);

        String lTitre = note.titre.trim().isEmpty()
                ? contexte.getString(R.string.carteSansTitreText)
                : note.titre;

        String lApercu = note.contenu.replaceAll("\\s+", " ").trim();
        if (lApercu.isEmpty()) {
            lApercu = contexte.getString(R.string.carteSansApercuText);
        }

        holder.titre.setText(lTitre);
        holder.apercu.setText(lApercu);
        holder.dateModification.setText(contexte.getString(R.string.carteModifieText, sdf.format(note.dateModification)));
        holder.dateCreation.setText(contexte.getString(R.string.carteCreeText, sdf.format(note.dateCreation)));

        holder.itemView.setOnClickListener(v -> listener.onClick(note));
        holder.modifBtn.setOnClickListener(v -> listener.onClick(note));
    }

    /**
     * Retourne la taille du data set.
     * @return Retourne la taille du data set.
     */
    @Override
    public int getItemCount() {
        return notes.size();
    }

    /**
     * Constructeur de l'adapteur.
     * @param listener Le Listener de clic sur une note.
     */
    public AdapteurDeNote(Listener listener) {
        this.listener = listener;
    }

    /**
     * Met à jour la liste des notes.
     * @param nouvellesNotes La nouvelle liste de notes.
     */
    public void setNotes(ArrayList<Note> nouvellesNotes) {
        ArrayList<Note> anciennesNotes = new ArrayList<>(notes);

        DiffUtil.DiffResult resultat = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return anciennesNotes.size();
            }

            @Override
            public int getNewListSize() {
                return nouvellesNotes.size();
            }

            @Override
            public boolean areItemsTheSame(int ancienne, int nouvelle) {
                return anciennesNotes.get(ancienne).id == nouvellesNotes.get(nouvelle).id;
            }

            @Override
            public boolean areContentsTheSame(int ancienne, int nouvelle) {
                Note a = anciennesNotes.get(ancienne);
                Note n = nouvellesNotes.get(nouvelle);
                return a.titre.equals(n.titre)
                        && a.contenu.equals(n.contenu)
                        && a.dateModification == n.dateModification;
            }
        });

        notes.clear();
        notes.addAll(nouvellesNotes);
        resultat.dispatchUpdatesTo(this);
    }

    /**
     * ViewHolder de la vue.
     */
    public static class NoteHolder extends RecyclerView.ViewHolder {
        TextView titre, apercu, dateModification, dateCreation;
        ImageView modifBtn;

        NoteHolder(View vue) {
            super(vue);
            titre = vue.findViewById(R.id.carteTitre);
            apercu = vue.findViewById(R.id.carteAppercu);
            dateModification = vue.findViewById(R.id.carteDateModification);
            dateCreation = vue.findViewById(R.id.carteDateCreation);
            modifBtn = vue.findViewById(R.id.carteModification);
        }

    }

}
