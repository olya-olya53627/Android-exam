package com.example.quiz_app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.quiz_app.models.Question;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameActivity extends AppCompatActivity {
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;

    private TextView questionText, categoryText, scoreText, questionCounter, timerText;
    private ProgressBar timerProgress;
    private Button[] answerButtons = new Button[4];
    private CountDownTimer timer;
    private LinearLayout gameContainer;
    private int timeLimit = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadQuestions();
        initializeViews();

        if (questions.size() > 0 && answerButtons[0] != null) {
            displayQuestion();
        } else {
            Toast.makeText(this, "Ошибка инициализации", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadQuestions() {
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject qObj = questionsArray.getJSONObject(i);
                Question question = new Question();
                question.setCategory(qObj.getString("category"));
                question.setText(qObj.getString("text"));
                question.setCorrectAnswer(qObj.getInt("correctAnswer"));
                question.setPoints(qObj.getInt("points"));

                List<String> options = new ArrayList<>();
                JSONArray opts = qObj.getJSONArray("options");
                for (int j = 0; j < opts.length(); j++) {
                    options.add(opts.getString(j));
                }
                question.setOptions(options);
                questions.add(question);
            }

            Collections.shuffle(questions);
        } catch (Exception e) {
            e.printStackTrace();
            Question testQuestion = new Question();
            testQuestion.setCategory("Тест");
            testQuestion.setText("2 + 2 = ?");
            testQuestion.setCorrectAnswer(1);
            testQuestion.setPoints(10);
            testQuestion.setOptions(Arrays.asList("3", "4", "5", "6"));
            questions.add(testQuestion);
        }
    }

    private void initializeViews() {
        gameContainer = findViewById(R.id.gameContainer);
        questionText = findViewById(R.id.questionText);
        categoryText = findViewById(R.id.categoryText);
        scoreText = findViewById(R.id.scoreText);
        questionCounter = findViewById(R.id.questionCounter);
        timerProgress = findViewById(R.id.timerProgress);
        timerText = findViewById(R.id.timerText);

        answerButtons[0] = findViewById(R.id.answer1);
        answerButtons[1] = findViewById(R.id.answer2);
        answerButtons[2] = findViewById(R.id.answer3);
        answerButtons[3] = findViewById(R.id.answer4);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(index));
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        questionText.setText(q.getText());
        categoryText.setText(q.getCategory());
        scoreText.setText("Score: " + score);
        questionCounter.setText((currentQuestionIndex + 1) + "/" + questions.size());

        List<String> options = q.getOptions();
        for (int i = 0; i < options.size() && i < 4; i++) {
            answerButtons[i].setText(options.get(i));
            answerButtons[i].setEnabled(true);
            // Устанавливаем золотой цвет фона
            answerButtons[i].setBackgroundColor(ContextCompat.getColor(this, R.color.button_gradient_start));
            // ОЧИЩАЕМ ВСЕ ИКОНКИ
            answerButtons[i].setCompoundDrawables(null, null, null, null);
        }

        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();

        timerProgress.setMax(100);
        timerProgress.setProgress(100);
        timerText.setText(String.valueOf(timeLimit));
        timerText.setTextColor(ContextCompat.getColor(this, R.color.button_gradient_start));

        timer = new CountDownTimer(timeLimit * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                int progress = (int)((millisUntilFinished * 100) / (timeLimit * 1000));

                timerProgress.setProgress(progress);
                timerText.setText(String.valueOf(secondsLeft));

                if (secondsLeft <= 5) {
                    timerText.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.wrong_red));
                }
            }

            @Override
            public void onFinish() {
                timerProgress.setProgress(0);
                timerText.setText("0");
                timerText.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.wrong_red));
                checkAnswer(-1);
            }
        }.start();
    }

    private void checkAnswer(int selectedIndex) {
        if (timer != null) timer.cancel();

        Question q = questions.get(currentQuestionIndex);
        boolean isCorrect = (selectedIndex == q.getCorrectAnswer());

        // Эффект тряски при неправильном ответе
        if (!isCorrect && selectedIndex >= 0) {
            shakeScreen();
        }

        // Изменяем цвет кнопок в зависимости от ответа
        if (selectedIndex >= 0) {
            if (isCorrect) {
                // ПРАВИЛЬНЫЙ ОТВЕТ - зеленый цвет
                answerButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
                score += q.getPoints();
                correctAnswers++;
                scoreText.setText("Score: " + score);
                // Показываем галочку
                showAnswerIcon(selectedIndex, true);
            } else {
                // НЕПРАВИЛЬНЫЙ ОТВЕТ - красный цвет
                answerButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_red));
                // Показываем правильный ответ зеленым цветом
                answerButtons[q.getCorrectAnswer()].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
                // Показываем крестик на неправильный ответ
                showAnswerIcon(selectedIndex, false);
                // Показываем галочку на правильный ответ
                showAnswerIcon(q.getCorrectAnswer(), true);
            }
        } else {
            // Время вышло - показываем правильный ответ зеленым
            answerButtons[q.getCorrectAnswer()].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
            showAnswerIcon(q.getCorrectAnswer(), true);
        }

        // Отключаем все кнопки
        for (Button btn : answerButtons) {
            btn.setEnabled(false);
        }

        // Переход к следующему вопросу через 1.5 секунды
        new android.os.Handler().postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1500);
    }

    // Метод для отображения иконки на кнопке
    private void showAnswerIcon(int buttonIndex, boolean isCorrect) {
        // Очищаем старые иконки
        answerButtons[buttonIndex].setCompoundDrawables(null, null, null, null);

        android.graphics.drawable.Drawable icon;
        if (isCorrect) {
            // Используем свои иконки
            icon = ContextCompat.getDrawable(this, R.drawable.ic_check);
        } else {
            icon = ContextCompat.getDrawable(this, R.drawable.ic_close);
        }

        if (icon != null) {
            int size = 60;
            icon.setBounds(0, 0, size, size);
            // Не меняем цвет иконки, оставляем белым
            answerButtons[buttonIndex].setCompoundDrawables(icon, null, null, null);
            answerButtons[buttonIndex].setCompoundDrawablePadding(20);
        }
    }

    // Эффект тряски экрана
    private void shakeScreen() {
        TranslateAnimation shake = new TranslateAnimation(0, 20, 0, 0);
        shake.setDuration(50);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        gameContainer.startAnimation(shake);
    }

    private void endGame() {
        android.content.SharedPreferences prefs = getSharedPreferences("quiz_results", MODE_PRIVATE);
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String result = date + "|" + score + "|" + correctAnswers + "/" + questions.size() + "|" +
                (correctAnswers * 100 / questions.size()) + "%";

        String existing = prefs.getString("results", "");
        if (existing.isEmpty()) {
            prefs.edit().putString("results", result).apply();
        } else {
            prefs.edit().putString("results", existing + "\n" + result).apply();
        }

        finish();
    }
}