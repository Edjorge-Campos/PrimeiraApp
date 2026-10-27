package com.example.primeiraapp;

import android.os.Bundle;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class Disponibility extends AppCompatActivity {

    private static final int FIRST_HOUR = 8;
    private static final int LAST_HOUR = 20;
    private static final int CELL_WIDTH_DP = 90;

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

        createWeeklyCalendar();
    }
    private void createWeeklyCalendar() {
        TableLayout calendar = findViewById(R.id.tableLayoutDisponibility);
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE dd/MM");

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
                TextView availabilityCell = createCell("", CELL_WIDTH_DP, false);
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
        TextView cell = (TextView) view;
        boolean selected = "Disponível".contentEquals(cell.getText());
        cell.setText(selected ? "" : "Disponível");
        cell.setTextColor(selected
                ? getResources().getColor(R.color.text_primary)
                : getResources().getColor(R.color.secondary));
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected
                ? getResources().getColor(R.color.surface)
                : getResources().getColor(R.color.primary_container));
        cell.setBackground(background);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
