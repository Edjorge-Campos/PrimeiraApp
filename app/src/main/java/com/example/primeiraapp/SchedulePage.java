package com.example.primeiraapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.primeiraapp.data.ShiftCallback;
import com.example.primeiraapp.data.ShiftRepository;
import com.example.primeiraapp.model.Shift;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchedulePage extends AppCompatActivity {

    private static final String TAG = "SchedulePage";
    private ShiftRepository shiftRepository;
    private FirebaseAuth firebaseAuth;
    private LinearLayout shiftListContainer;
    private TextView emptyState;
    private final Set<String> subscribedShiftIds = new HashSet<>();
    private int selectedWeekday = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_page);

        shiftRepository = new ShiftRepository();
        firebaseAuth = FirebaseAuth.getInstance();
        CalendarView calendarView = findViewById(R.id.scheduleCalendar);
        shiftListContainer = findViewById(R.id.shiftListContainer);
        emptyState = findViewById(R.id.emptyState);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int diaDaSemana = toWeekday(year, month, dayOfMonth);
            selectedWeekday = diaDaSemana;
            loadShiftsForWeekday(diaDaSemana);
        });

        Calendar today = Calendar.getInstance();
        selectedWeekday = toWeekday(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        loadShiftsForWeekday(selectedWeekday);
    }

    private int toWeekday(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SUNDAY ? 7 : day - 1;
    }

    private void loadShiftsForWeekday(int diaDaSemana) {
        Log.d(TAG, "A carregar turnos para DiaDaSemana=" + diaDaSemana);
        selectedWeekday = diaDaSemana;
        shiftListContainer.removeAllViews();
        emptyState.setText("A carregar turnos...");
        emptyState.setVisibility(View.VISIBLE);
        subscribedShiftIds.clear();

        shiftRepository.carregarTurno(diaDaSemana, new ShiftCallback() {
            @Override
            public void onSuccess(List<Shift> shifts) {
                if (shifts.isEmpty()) {
                    emptyState.setText("Não existem turnos abertos para este dia.");
                    return;
                }

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    renderShifts(shifts, null, subscribedShiftIds);
                    return;
                }

                final int[] remaining = {shifts.size()};
                for (Shift shift : shifts) {
                    shiftRepository.verificarInscricao(shift.getId(), currentUser.getUid())
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    subscribedShiftIds.add(shift.getId());
                                }
                                remaining[0]--;
                                if (remaining[0] == 0) {
                                    renderShifts(shifts, currentUser.getUid(), subscribedShiftIds);
                                }
                            })
                            .addOnFailureListener(error -> {
                                Log.e(TAG, "Erro ao verificar inscrição do turno " + shift.getId(), error);
                                remaining[0]--;
                                if (remaining[0] == 0) {
                                    renderShifts(shifts, currentUser.getUid(), subscribedShiftIds);
                                }
                            });
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Erro ao carregar turnos", error);
                emptyState.setText("Não foi possível carregar os turnos.");
                Toast.makeText(SchedulePage.this,
                        "Erro ao consultar os turnos: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderShifts(List<Shift> shifts, String currentUserId, Set<String> subscribedSet) {
        shiftListContainer.removeAllViews();

        if (shifts == null || shifts.isEmpty()) {
            emptyState.setText("Não existem turnos abertos para este dia.");
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        emptyState.setVisibility(View.GONE);

        for (Shift shift : shifts) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(16, 16, 16, 16);

            TextView shiftInfo = new TextView(this);
            String statusText = shift.getStatus() == null ? "desconhecido" : shift.getStatus();
            String info = "Hora: " + shift.getInicio() + "\nEstado: " + statusText;
            shiftInfo.setText(info);

            LinearLayout actionRow = new LinearLayout(this);
            actionRow.setOrientation(LinearLayout.HORIZONTAL);
            actionRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            Button primaryButton = new Button(this);
            Button cancelButton = new Button(this);
            boolean alreadySubscribed = currentUserId != null && subscribedSet.contains(shift.getId());
            if (alreadySubscribed) {
                primaryButton.setText("Inscrito");
                primaryButton.setEnabled(false);
                cancelButton.setText("Cancelar inscrição");
                cancelButton.setVisibility(View.VISIBLE);
            } else if (currentUserId == null) {
                primaryButton.setText("Inscrever-me");
                primaryButton.setEnabled(false);
                cancelButton.setText("Cancelar inscrição");
                cancelButton.setVisibility(View.GONE);
            } else {
                primaryButton.setText("Inscrever-me");
                cancelButton.setText("Cancelar inscrição");
                cancelButton.setVisibility(View.GONE);
            }

            final String shiftId = shift.getId();
            final String uid = currentUserId;
            primaryButton.setOnClickListener(v -> {
                if (uid == null || uid.trim().isEmpty()) {
                    Toast.makeText(this, "Precisa de iniciar sessão para se inscrever.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (subscribedSet.contains(shiftId)) {
                    Toast.makeText(this, "Já está inscrito neste turno.", Toast.LENGTH_SHORT).show();
                    return;
                }
                createSignup(shiftId, uid, primaryButton);
            });

            cancelButton.setOnClickListener(v -> {
                if (uid == null || uid.trim().isEmpty()) {
                    Toast.makeText(this, "Precisa de iniciar sessão para cancelar a inscrição.", Toast.LENGTH_SHORT).show();
                    return;
                }
                cancelSignup(shiftId, uid, cancelButton);
            });

            actionRow.addView(primaryButton);
            actionRow.addView(cancelButton);
            row.addView(shiftInfo);
            row.addView(actionRow);
            shiftListContainer.addView(row);
        }
    }

    private void createSignup(String shiftId, String userId, Button actionButton) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        if (subscribedShiftIds.contains(shiftId)) {
            Toast.makeText(this, "Já está inscrito neste turno.", Toast.LENGTH_SHORT).show();
            return;
        }

        actionButton.setEnabled(false);
        actionButton.setText("A processar...");

        shiftRepository.inscreverUtilizador(shiftId, userId)
                .addOnSuccessListener(unused -> {
                    subscribedShiftIds.add(shiftId);
                    Toast.makeText(this, "Inscrição efetuada com sucesso.", Toast.LENGTH_SHORT).show();
                    loadShiftsForWeekday(selectedWeekday);
                })
                .addOnFailureListener(error -> {
                    actionButton.setEnabled(true);
                    actionButton.setText("Inscrever-me");
                    Toast.makeText(this,
                            "Não foi possível concluir a inscrição: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro ao criar inscrição no turno " + shiftId, error);
                });
    }

    private void cancelSignup(String shiftId, String userId, Button actionButton) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        actionButton.setEnabled(false);
        actionButton.setText("A cancelar...");

        shiftRepository.cancelarInscricao(shiftId, userId)
                .addOnSuccessListener(unused -> {
                    subscribedShiftIds.remove(shiftId);
                    Toast.makeText(this, "Inscrição cancelada com sucesso.", Toast.LENGTH_SHORT).show();
                    loadShiftsForWeekday(selectedWeekday);
                })
                .addOnFailureListener(error -> {
                    actionButton.setEnabled(true);
                    actionButton.setText("Cancelar inscrição");
                    Toast.makeText(this,
                            "Não foi possível cancelar a inscrição: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro ao cancelar inscrição no turno " + shiftId, error);
                });
    }

    private int toWeekdayFromCalendar() {
        return selectedWeekday;
    }
}