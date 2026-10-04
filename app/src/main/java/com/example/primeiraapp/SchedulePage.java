package com.example.primeiraapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.primeiraapp.data.ShiftCallback;
import com.example.primeiraapp.data.ShiftRepository;
import com.example.primeiraapp.model.Shift;

import java.util.Calendar;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class SchedulePage extends AppCompatActivity {

    private static final String TAG = "SchedulePage";
    private ShiftRepository shiftRepository;
    private TextView shiftResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_page);

        shiftRepository = new ShiftRepository();
        CalendarView calendarView = findViewById(R.id.scheduleCalendar);
        shiftResults = findViewById(R.id.shiftResults);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int diaDaSemana = toWeekday(year, month, dayOfMonth);
            Log.d(TAG, "Data selecionada: " + year + "-" + (month + 1) + "-"
                    + dayOfMonth + ", diaDaSemana=" + diaDaSemana);
            loadShiftsForWeekday(diaDaSemana);
        });

        Calendar today = Calendar.getInstance();
        loadShiftsForWeekday(toWeekday(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        ));
    }

    private int toWeekday(int year, int month, int dayOfMonth) {
        LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
        int diaDaSemana = selectedDate.getDayOfWeek().getValue();
        Log.d(TAG, "Data " + selectedDate + " convertida para DiaDaSemana="
                + diaDaSemana + " (domingo=7)");
        return diaDaSemana;
    }

    private void loadShiftsForWeekday(int diaDaSemana) {
        Log.d(TAG, "A carregar turnos para DiaDaSemana=" + diaDaSemana);
        shiftResults.setText("A carregar turnos...");
        shiftRepository.carregarTurno(diaDaSemana, new ShiftCallback() {
            @Override
            public void onSuccess(List<Shift> shifts) {
                if (shifts.isEmpty()) {
                    shiftResults.setText("Não existem turnos abertos para este dia.");
                    return;
                }

                StringBuilder result = new StringBuilder();
                for (Shift shift : shifts) {
                    result.append("Hora: ")
                            .append(shift.getInicio())
                            .append("\nEstado: ")
                            .append(shift.getStatus())
                            .append("\n\n");
                }
                shiftResults.setText(result.toString().trim());
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Erro completo ao carregar turnos", error);
                shiftResults.setText("Não foi possível carregar os turnos.");
                Toast.makeText(
                        SchedulePage.this,
                        "Erro ao consultar os turnos: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}