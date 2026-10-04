package com.example.primeiraapp.data;

import com.example.primeiraapp.model.Shift;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShiftRepository {

    private static final String TAG = "ShiftRepository";
    private static final String COLLECTION_TURNOS = "turnos";

    private final FirebaseFirestore firestore;

    public ShiftRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> TurnosDaSemana(int diaDaSemana) {
        Log.d(TAG, "A consultar collection=turnos, DiaDaSemana=" + diaDaSemana
                + " (Number), Status=open");
        return firestore.collection(COLLECTION_TURNOS)
                .whereEqualTo("DiaDaSemana", diaDaSemana)
                .whereEqualTo("Status", "open")
                .get();
    }

    public List<Shift> DocsParaTurnos(QuerySnapshot snapshot) {
        Log.d(TAG, "Quantidade de documentos devolvidos: " + snapshot.size());
        List<Shift> shifts = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Shift shift = document.toObject(Shift.class);
            if (shift != null) {
                Log.d(TAG, "Turno recebido: id=" + document.getId()
                        + ", DiaDaSemana=" + shift.getDiaDaSemana()
                        + ", Inicio=" + shift.getInicio()
                        + ", Status=" + shift.getStatus());
                shifts.add(shift);
            }
        }
        return shifts;
    }

    public void carregarTurno(int diaDaSemana, ShiftCallback callback) {
        if (diaDaSemana < 1 || diaDaSemana > 7) {
            callback.onError(new IllegalArgumentException("O dia da semana deve estar entre 1 e 7."));
            return;
        }

        TurnosDaSemana(diaDaSemana)
                .addOnSuccessListener(snapshot -> {
                    List<Shift> shifts = DocsParaTurnos(snapshot);
                    callback.onSuccess(shifts);
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Erro completo ao consultar turnos", error);
                    callback.onError(error);
                });
    }

}