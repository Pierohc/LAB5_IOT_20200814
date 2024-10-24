package com.example.seguimientodecalorias;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText peso, altura, edad, editTextCaloriesMeal;
    private RadioGroup radioGroupGender, radioObjetivo, radioActividad;
    private Button btnCalculateCalories, btnAddMeal;
    private TextView textViewResultadoCalorias, textViewTotalCaloriesConsumed;

    private int totalCaloriesConsumed = 0;
    private double controlador = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        peso = findViewById(R.id.peso);
        altura = findViewById(R.id.altura);
        edad = findViewById(R.id.edad);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioObjetivo = findViewById(R.id.radioObjetvio);
        radioActividad = findViewById(R.id.radioActividad);
        btnCalculateCalories = findViewById(R.id.btnCalculateCalories);
        textViewResultadoCalorias = findViewById(R.id.textViewResultadoCalorias);


        editTextCaloriesMeal = findViewById(R.id.editTextCaloriesMeal);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        textViewTotalCaloriesConsumed = findViewById(R.id.textViewTotalCaloriesConsumed);

        btnCalculateCalories.setOnClickListener(v -> calculateCalories());

        btnAddMeal.setOnClickListener(v -> addMealCalories());    }

    private void calculateCalories() {
        String pesoStr = peso.getText().toString();
        String alturaStr = altura.getText().toString();
        String edadStr = edad.getText().toString();

        if (pesoStr.isEmpty() || alturaStr.isEmpty() || edadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double pesoValue = Double.parseDouble(pesoStr);
        double alturaValue = Double.parseDouble(alturaStr);
        int edadValue = Integer.parseInt(edadStr);

        int genderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(genderId);
        boolean isMale = selectedGender.getText().toString().equalsIgnoreCase("Hombre");

        int objetivoId = radioObjetivo.getCheckedRadioButtonId();
        RadioButton selectedObjetivo = findViewById(objetivoId);
        String objetivo = selectedObjetivo.getText().toString();

        int actividadId = radioActividad.getCheckedRadioButtonId();
        RadioButton selectedActividad = findViewById(actividadId);
        String actividad = selectedActividad.getText().toString();

        double tmb;
        if (isMale) {
            tmb = 10 * pesoValue + 6.25 * alturaValue - 5 * edadValue + 5;
        } else {
            tmb = 10 * pesoValue + 6.25 * alturaValue - 5 * edadValue - 161;
        }

        double factorActividad;
        switch (actividad) {
            case "Sedentario":
                factorActividad = 1.2;
                break;
            case "Actividad ligera":
                factorActividad = 1.375;
                break;
            case "Actividad moderada":
                factorActividad = 1.55;
                break;
            case "Actividad intensa":
                factorActividad = 1.725;
                break;
            case "Muy intensa":
                factorActividad = 1.9;
                break;
            default:
                factorActividad = 1.0;
                break;
        }

        double caloriasDiarias = tmb * factorActividad;

        switch (objetivo) {
            case "Subir de peso":
                caloriasDiarias += 500;
                break;
            case "Bajar de peso":
                caloriasDiarias -= 500;
                break;
            case "Mantener peso":
                // No se hace ajuste
                break;
        }

        controlador = caloriasDiarias;

        textViewResultadoCalorias.setText("Calorías diarias: " + Math.round(caloriasDiarias));
    }

    private void addMealCalories() {
        String mealCaloriesStr = editTextCaloriesMeal.getText().toString();

        if (mealCaloriesStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese las calorías de la comida", Toast.LENGTH_SHORT).show();
            return;
        }

        int mealCalories = Integer.parseInt(mealCaloriesStr);
        totalCaloriesConsumed += mealCalories;

        textViewTotalCaloriesConsumed.setText("Total de calorías consumidas hoy: " + totalCaloriesConsumed);

        if (totalCaloriesConsumed > controlador) {

        }


            // Limpiar el campo de entrada
        editTextCaloriesMeal.setText("");
    }
}
