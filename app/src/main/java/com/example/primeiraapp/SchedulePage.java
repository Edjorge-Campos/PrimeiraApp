package com.example.primeiraapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SchedulePage extends AppCompatActivity {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm";

    private CalendarView calendarView;
    private TextView selectedDateView;
    private TextView stateMessage;
    private ProgressBar loading;
    private LinearLayout shiftList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private final List<Shift> shifts = new ArrayList<>();
    private String selectedDate;
    private String loadedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_page);

        calendarView = findViewById(R.id.shiftCalendar);
        selectedDateView = findViewById(R.id.selectedDate);
        stateMessage = findViewById(R.id.stateMessage);
        loading = findViewById(R.id.loading);
        shiftList = findViewById(R.id.shiftList);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        selectedDate = formatDate(new Date());
        loadedMonth = selectedDate.substring(0, 7);
        calendarView.setDate(System.currentTimeMillis(), false, true);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            selectedDateView.setText(formatDisplayDate(selectedDate));
            String monthKey = selectedDate.substring(0, 7);
            if (!monthKey.equals(loadedMonth)) {
                loadedMonth = monthKey;
                loadShifts();
            } else {
                renderSelectedDate();
            }
        });
        selectedDateView.setText(formatDisplayDate(selectedDate));
        loadShifts();
    }

    private void loadShifts() {
        setLoading(true);
        db.collection("shifts")
                .whereNotEqualTo("status", "cancelled")
                .get()
                .addOnSuccessListener(this::onShiftsLoaded)
                .addOnFailureListener(error -> {
                    setLoading(false);
                    showState("Não foi possível carregar os turnos. Tente novamente.");
                });
    }

    private void onShiftsLoaded(QuerySnapshot snapshot) {
        shifts.clear();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String date = document.getString("date");
            String startTime = document.getString("startTime");
            String endTime = document.getString("endTime");
            String title = document.getString("title");
            String status = document.getString("status");
            if (document.exists() && date != null && startTime != null && endTime != null
                    && title != null && status != null && date.startsWith(loadedMonth)) {
                shifts.add(new Shift(document.getId(), date, startTime, endTime, title,
                        document.getString("description"), status));
            }
        }
        Collections.sort(shifts, (left, right) -> {
            int dateComparison = left.date.compareTo(right.date);
            return dateComparison != 0 ? dateComparison : left.startTime.compareTo(right.startTime);
        });
        setLoading(false);
        renderSelectedDate();
    }

    private void renderSelectedDate() {
        shiftList.removeAllViews();
        List<Shift> selectedShifts = new ArrayList<>();
        for (Shift shift : shifts) {
            if (shift.date.equals(selectedDate)) {
                selectedShifts.add(shift);
            }
        }
        if (selectedShifts.isEmpty()) {
            showState("Não há turnos para esta data.");
            return;
        }
        hideState();
        for (Shift shift : selectedShifts) {
            addShiftCard(shift);
        }
    }

    private void addShiftCard(Shift shift) {
        MaterialCardView card = new MaterialCardView(this);
        card.setUseCompatPadding(true);
        card.setCardElevation(2);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        content.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText(shift.title);
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        content.addView(title);

        TextView time = new TextView(this);
        time.setText(shift.startTime + " - " + shift.endTime);
        content.addView(time);
        if (shift.description != null && !shift.description.trim().isEmpty()) {
            TextView description = new TextView(this);
            description.setText(shift.description);
            description.setPadding(0, dp(6), 0, 0);
            content.addView(description);
        }

        TextView status = new TextView(this);
        status.setPadding(0, dp(8), 0, 0);
        content.addView(status);
        Button action = new Button(this);
        content.addView(action);
        card.addView(content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        shiftList.addView(card, params);

        if ("cancelled".equalsIgnoreCase(shift.status)) {
            status.setText("Cancelado");
            action.setVisibility(View.GONE);
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            status.setText("Inicie sessão para se inscrever.");
            action.setText("Inscrever-me");
            action.setEnabled(false);
            return;
        }
        db.collection("shifts").document(shift.id).collection("signups")
                .document(user.getUid()).get()
                .addOnSuccessListener(signup -> {
                    if (signup.exists()) {
                        status.setText("Está inscrito neste turno.");
                        action.setText("Cancelar inscrição");
                    } else {
                        status.setText("Vagas abertas.");
                        action.setText("Inscrever-me");
                    }
                    action.setOnClickListener(view -> {
                        action.setEnabled(false);
                        if (signup.exists()) {
                            cancelSignup(shift, user.getUid(), action, status);
                        } else {
                            createSignup(shift, user.getUid(), action, status);
                        }
                    });
                })
                .addOnFailureListener(error -> {
                    status.setText("Não foi possível verificar a inscrição.");
                    action.setEnabled(false);
                });
    }

    private void createSignup(Shift shift, String uid, Button action, TextView status) {
        if (!isBeforeShiftStart(shift)) {
            status.setText("Este turno já começou.");
            action.setEnabled(false);
            return;
        }
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            com.google.firebase.firestore.DocumentReference signup = db.collection("shifts")
                    .document(shift.id).collection("signups").document(uid);
            if (transaction.get(signup).exists()) {
                throw new FirebaseFirestoreException("Já está inscrito.",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("userId", uid);
            data.put("shiftId", shift.id);
            data.put("createdAt", FieldValue.serverTimestamp());
            transaction.set(signup, data);
            return null;
        }).addOnSuccessListener(unused -> {
            status.setText("Está inscrito neste turno.");
            action.setText("Cancelar inscrição");
            action.setEnabled(true);
            action.setOnClickListener(view -> {
                action.setEnabled(false);
                cancelSignup(shift, uid, action, status);
            });
        }).addOnFailureListener(error -> {
            status.setText(error instanceof FirebaseFirestoreException
                    && ((FirebaseFirestoreException) error).getCode()
                    == FirebaseFirestoreException.Code.ALREADY_EXISTS
                    ? "Já está inscrito neste turno." : "Não foi possível inscrever-se.");
            action.setEnabled(true);
        });
    }

    private void cancelSignup(Shift shift, String uid, Button action, TextView status) {
        if (!isBeforeShiftStart(shift)) {
            status.setText("O cancelamento só é possível antes do início.");
            action.setEnabled(false);
            return;
        }
        db.collection("shifts").document(shift.id).collection("signups").document(uid)
                .delete()
                .addOnSuccessListener(unused -> {
                    status.setText("Inscrição cancelada.");
                    action.setText("Inscrever-me");
                    action.setEnabled(true);
                    action.setOnClickListener(view -> {
                        action.setEnabled(false);
                        createSignup(shift, uid, action, status);
                    });
                })
                .addOnFailureListener(error -> {
                    status.setText("Não foi possível cancelar a inscrição.");
                    action.setEnabled(true);
                });
    }

    private boolean isBeforeShiftStart(Shift shift) {
        try {
            Date start = new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT, Locale.US)
                    .parse(shift.date + " " + shift.startTime);
            return start != null && new Date().before(start);
        } catch (ParseException e) {
            return false;
        }
    }

    private void setLoading(boolean isLoading) {
        loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            hideState();
            shiftList.removeAllViews();
        }
    }

    private void showState(String message) {
        stateMessage.setText(message);
        stateMessage.setVisibility(View.VISIBLE);
    }

    private void hideState() {
        stateMessage.setVisibility(View.GONE);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(date);
    }

    private String formatDisplayDate(String date) {
        try {
            Date parsed = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(date);
            return new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(parsed);
        } catch (ParseException e) {
            return date;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Shift {
        final String id;
        final String date;
        final String startTime;
        final String endTime;
        final String title;
        final String description;
        final String status;

        Shift(String id, String date, String startTime, String endTime, String title,
              String description, String status) {
            this.id = id;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.title = title;
            this.description = description;
            this.status = status;
        }
    }
}