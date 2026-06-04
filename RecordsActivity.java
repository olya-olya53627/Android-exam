package com.example.quiz_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quiz_app.adapters.RecordsAdapter;
import com.example.quiz_app.models.QuizResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private RecordsAdapter adapter;
    private List<QuizResult> recordsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        recyclerView = findViewById(R.id.recyclerView);
        emptyText = findViewById(R.id.emptyText);

        // Настройка RecyclerView
        adapter = new RecordsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadRecords();

        // Кнопка назад
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadRecords() {
        SharedPreferences prefs = getSharedPreferences("quiz_results", MODE_PRIVATE);
        String results = prefs.getString("results", "");

        if (results.isEmpty()) {
            emptyText.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            emptyText.setVisibility(android.view.View.GONE);
            recyclerView.setVisibility(android.view.View.VISIBLE);

            String[] lines = results.split("\n");
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String date = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    String[] correctTotal = parts[2].split("/");
                    int correctAnswers = Integer.parseInt(correctTotal[0]);
                    int totalQuestions = Integer.parseInt(correctTotal[1]);
                    // percentage = parts[3].replace("%", "");

                    QuizResult result = new QuizResult(date, score, totalQuestions, correctAnswers);
                    recordsList.add(result);
                }
            }

            // Сортировка по дате (новые сверху)
            Collections.sort(recordsList, new Comparator<QuizResult>() {
                @Override
                public int compare(QuizResult o1, QuizResult o2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    try {
                        Date date1 = sdf.parse(o1.getDate());
                        Date date2 = sdf.parse(o2.getDate());
                        return date2.compareTo(date1); // Обратный порядок (новые сверху)
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            });

            adapter.setRecords(recordsList);
        }
    }
}