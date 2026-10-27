package com.example.primeiraapp;

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Disponibility extends AppCompatActivity {

    private static final int FIRST_HOUR = 8;
    private static final int LAST_HOUR = 20;
    private static final int CELL_WIDTH_DP = 90;
    private static final String USERS_COLLECTION = "Users";
    private static final String FIELD_AVAILABILITY = "availability";

    private final Set<String> selectedSlots = new HashSet<>();
    private TableLayout calendar;
    private TextView availabilityStatus;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_disponibility);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        calendar = findViewById(R.id.tableLayoutDisponibility);
        availabilityStatus = findViewById(R.id.availabilityStatus);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createWeeklyCalendar();
        }
        loadAvailability();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createWeeklyCalendar() {
        calendar.removeAllViews();

        java.time.LocalDate monday = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE dd/MM");

        TableRow header = new TableRow(this);
        header.addView(createCell("Hora", 64, true));
        for (int day = 0; day < 7; day++) {
            header.addView(createCell(
                    monday.plusDays(day).format(dayFormatter),
                    CELL_WIDTH_DP,
                    true
            ));
        }
        calendar.addView(header);

        for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
            TableRow row = new TableRow(this);
            row.addView(createCell(String.format("%02d:00", hour), 64, true));

            for (int day = 0; day < 7; day++) {
                String slotKey = day + "-" + hour;
                TextView availabilityCell = createCell("", CELL_WIDTH_DP, false);
                availabilityCell.setTag(slotKey);
                availabilityCell.setOnClickListener(this::toggleAvailability);
                row.addView(availabilityCell);
            }
            calendar.addView(row);
        }
    }

    private TextView createCell(String text, int widthDp, boolean header) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setGravity(Gravity.CENTER);
        cell.setTextColor(header
                ? getResources().getColor(R.color.on_primary)
                : getResources().getColor(R.color.text_primary));
        cell.setTextSize(header ? 13 : 14);
        cell.setPadding(4, 12, 4, 12);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                dpToPx(widthDp),
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(1, 1, 1, 1);
        cell.setLayoutParams(params);

        GradientDrawable background = new GradientDrawable();
        background.setColor(header
                ? getResources().getColor(R.color.primary)
                : getResources().getColor(R.color.surface));
        cell.setBackground(background);
        return cell;
    }

    private void toggleAvailability(View view) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Precisa de iniciar sessão para guardar a disponibilidade.", Toast.LENGTH_SHORT).show();
            updateStatus("Sessão expirada. Faça login novamente.");
            return;
        }

        TextView cell = (TextView) view;
        String slotKey = String.valueOf(cell.getTag());
        if (slotKey == null || slotKey.trim().isEmpty() || !slotKey.contains("-")) {
            return;
        }

        boolean alreadySelected = selectedSlots.contains(slotKey);
        if (alreadySelected) {
            selectedSlots.remove(slotKey);
        } else {
            selectedSlots.add(slotKey);
        }

        applyCellVisual(cell, !alreadySelected);
        saveAvailability(currentUser.getUid());
    }

    private void loadAvailability() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            updateStatus("Sessão expirada. Faça login novamente.");
            return;
        }

        updateStatus("A carregar disponibilidade...");
        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    selectedSlots.clear();
                    if (documentSnapshot.exists()) {
                        String savedAvailability = documentSnapshot.getString(FIELD_AVAILABILITY);
                        selectedSlots.addAll(parseAvailabilityString(savedAvailability));
                    }
                    refreshCalendarSelection();
                    updateStatus("Disponibilidade carregada.");
                })
                .addOnFailureListener(error -> {
                    updateStatus("Erro ao carregar a disponibilidade.");
                    Toast.makeText(this, "Erro ao carregar disponibilidade: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveAvailability(String uid) {
        updateStatus("A guardar disponibilidade...");

        db.collection(USERS_COLLECTION)
                .document(uid)
                .set(Collections.singletonMap(FIELD_AVAILABILITY, serializeAvailability(selectedSlots)), SetOptions.merge())
                .addOnSuccessListener(unused -> updateStatus("Disponibilidade guardada."))
                .addOnFailureListener(error -> {
                    updateStatus("Erro ao guardar a disponibilidade.");
                    Toast.makeText(this, "Erro ao guardar disponibilidade: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void refreshCalendarSelection() {
        for (int i = 0; i < calendar.getChildCount(); i++) {
            View row = calendar.getChildAt(i);
            if (!(row instanceof TableRow)) {
                continue;
            }

            TableRow tableRow = (TableRow) row;
            for (int j = 0; j < tableRow.getChildCount(); j++) {
                View cellView = tableRow.getChildAt(j);
                if (!(cellView instanceof TextView)) {
                    continue;
                }

                TextView cell = (TextView) cellView;
                String tag = String.valueOf(cell.getTag());
                if (tag != null && tag.contains("-")) {
                    applyCellVisual(cell, selectedSlots.contains(tag));
                }
            }
        }
    }

    private void applyCellVisual(TextView cell, boolean selected) {
        if (selected) {
            cell.setText("Disponível");
            cell.setTextColor(getResources().getColor(R.color.secondary));
            GradientDrawable background = new GradientDrawable();
            background.setColor(getResources().getColor(R.color.primary_container));
            cell.setBackground(background);
        } else {
            cell.setText("");
            cell.setTextColor(getResources().getColor(R.color.text_primary));
            GradientDrawable background = new GradientDrawable();
            background.setColor(getResources().getColor(R.color.surface));
            cell.setBackground(background);
        }
    }

    private void updateStatus(String message) {
        if (availabilityStatus != null) {
            availabilityStatus.setText(message);
        }
    }

    public static Set<String> parseAvailabilityString(String availability) {
        Set<String> values = new HashSet<>();
        if (availability == null || availability.trim().isEmpty()) {
            return values;
        }

        String[] parts = availability.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && trimmed.matches("\\d+-\\d+")) {
                values.add(trimmed);
            }
        }
        return values;
    }

    public static String serializeAvailability(Set<String> selectedSlots) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            return "";
        }

        List<String> ordered = new ArrayList<>(selectedSlots);
        Collections.sort(ordered);
        return String.join(",", ordered);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
