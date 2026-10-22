package me.cjc.a3n2_blocnoteproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        adapteur = new AdapteurDeNote(note -> {
            Intent intention = new Intent(this, EditeurActivity.class);
            intention.putExtra("idNote", note.id);
            startActivity(intention);
        });

        recycler.setAdapter(adapteur);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapteur.setNotes(bd.obtenirTous());
    }
}