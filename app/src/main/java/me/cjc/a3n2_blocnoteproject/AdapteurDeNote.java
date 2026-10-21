package me.cjc.a3n2_blocnoteproject;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class AdapteurDeNote extends RecyclerView.Adapter<AdapteurDeNote.NoteHolder> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    public interface Listener {
        void onClick(Note note);
    }
    private final ArrayList<Note> notes = new ArrayList<>();

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext()).inflate(R.layout.carte_note, parent, false);
        return new NoteHolder(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Context contexte = holder.itemView.getContext();
        Note note = notes.get(position);

        String lTitre = note.titre.trim().isEmpty()
                ? contexte.getString(R.string.carteSansTitreText)
                : note.titre;

        String lApercu = note.contenu.isEmpty()
                ? contexte.getString(R.string.carteSansApercuText)
                : note.contenu.replaceAll("\\s+", " ").trim();

        holder.titre.setText(lTitre);
        holder.apercu.setText(lApercu);
        holder.dateModification.setText(String.format("Modifié: %s", sdf.format(note.dateModification)));
        holder.dateCreation.setText(String.format("Créé: %s", sdf.format(note.dateCreation)));

        holder.itemView.setOnClickListener(v -> listener.onClick(note));
        holder.modifBtn.setOnClickListener(v -> listener.onClick(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
    private final Listener listener;

    public AdapteurDeNote(Listener listener) {
        this.listener = listener;
    }

    public void setNotes(ArrayList<Note> nouvellesNotes) {
        notes.clear();
        notes.addAll(nouvellesNotes);
        notifyDataSetChanged();
    }

    static class NoteHolder extends RecyclerView.ViewHolder {
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
