package com.example.seguimientodecalorias;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private EditText peso, altura, edad, editTextCaloriesMeal;
    private RadioGroup radioGroupGender, radioObjetivo, radioActividad;
    private Button btnCalculateCalories, btnAddMeal;
    private TextView textViewResultadoCalorias, textViewTotalCaloriesConsumed;

    private int totalCaloriesConsumed = 0;
    private double controlador = 0;
    private double exceso = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        createNotificationChannel();

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
            exceso = totalCaloriesConsumed - controlador;
            lanzarNotificacion(exceso);

        }


            // Limpiar el campo de entrada
        editTextCaloriesMeal.setText("");
    }


    String channelId = "channelDefaultPri";
    public void createNotificationChannel() {
        //android.os.Build.VERSION_CODES.O == 26
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Canal notificaciones default",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Canal para notificaciones con prioridad default");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            askPermission();
        }

    }

    public void askPermission(){
        //android.os.Build.VERSION_CODES.TIRAMISU == 33
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{POST_NOTIFICATIONS},
                    101);
        }
    }

    public void lanzarNotificacion(double exceso) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle("Alerta de Exceso de Calorías")
                .setContentText("Exceso: " + exceso + "\nHa consumido más calorías de las\n" +
                        "recomendadas en un día, se le\n" +
                        "sugiera realizar ejercicio o reducir las calorías en la próxima comida")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }
}
