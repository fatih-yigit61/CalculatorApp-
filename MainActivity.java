package com.yigit.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private String currentExpression = "";
    private boolean isOpenParenthesis = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextText);
        editText.setKeyListener(null); // Kullanıcının klavyeden değiştirmesini engelleme

        int[] buttonIds = {
                R.id.n0, R.id.n1, R.id.n2, R.id.n3, R.id.n4,
                R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9,
                R.id.add, R.id.substract, R.id.multiply, R.id.divide,
                R.id.percen, R.id.dot
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(this::onButtonClick);
        }

        // AC Butonu (Temizleme)
        findViewById(R.id.ac).setOnClickListener(v -> {
            currentExpression = "";
            editText.setText("0");
            isOpenParenthesis = true;
        });

        // Parantez Butonu
        findViewById(R.id.pha).setOnClickListener(v -> {
            if (isOpenParenthesis) {
                currentExpression += "(";
            } else {
                currentExpression += ")";
            }
            isOpenParenthesis = !isOpenParenthesis;
            editText.setText(currentExpression);
        });

        // Eşittir (=) Butonu (Sonuç Hesaplama)
        findViewById(R.id.equal).setOnClickListener(v -> {
            try {
                double result = evaluateExpression(currentExpression.replace("x", "*"));
                editText.setText(String.valueOf(result));
                currentExpression = String.valueOf(result);
            } catch (Exception e) {
                editText.setText("Error");
                currentExpression = "";
            }
        });
    }

    private void onButtonClick(View v) {
        Button button = (Button) v;
        currentExpression += button.getText().toString();
        currentExpression = currentExpression.replace("x", "*");
        editText.setText(currentExpression);
    }

    // Basit matematiksel ifade hesaplama fonksiyonu
    private double evaluateExpression(String expression) {
        try {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm(); // addition
                        else if (eat('-')) x -= parseTerm(); // subtraction
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor(); // multiplication
                        else if (eat('/')) x /= parseFactor(); // division
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double x;
                    int startPos = this.pos;
                    if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else if (eat('(')) { // parentheses
                        x = parseExpression();
                        eat(')');
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }
                    return x;
                }
            }.parse();
        } catch (Exception e) {
            return 0;
        }
    }
}
