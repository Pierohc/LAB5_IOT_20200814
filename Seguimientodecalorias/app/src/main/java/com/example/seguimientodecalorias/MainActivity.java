package com.example.seguimientodecalorias;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText peso, altura, edad, editTextMealName, editTextCaloriesMeal, sportName,sportCalories;
    private RadioGroup radioGroupGender, radioObjetivo, radioActividad;
    private Button btnCalculateCalories, btnAddMeal, btnAddSport;
    private TextView textViewResultadoCalorias, textViewTotalCaloriesConsumed, caloriasFaltantes;
    private Spinner spinnerFoodCatalog;

    private int totalCaloriesConsumed = 0;
    private double controlador = 0;
    private double exceso = 0;
    private int notificationIntervalMinutes = 0;

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
        caloriasFaltantes = findViewById(R.id.caloriasFaltantes);


        editTextMealName = findViewById(R.id.editTextMealName);
        editTextCaloriesMeal = findViewById(R.id.editTextCaloriesMeal);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        textViewTotalCaloriesConsumed = findViewById(R.id.textViewTotalCaloriesConsumed);

        sportName = findViewById(R.id.sportName);
        sportCalories = findViewById(R.id.sportCalories);
        btnAddSport = findViewById(R.id.btnAddSport);

        spinnerFoodCatalog = findViewById(R.id.spinnerFoodCatalog);

        btnCalculateCalories.setOnClickListener(v -> calculateCalories());

        btnAddMeal.setOnClickListener(v -> addMealCalories());

        btnAddSport.setOnClickListener(v -> quitarCalorias());

        spinnerFoodCatalog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!selectedItem.equals("Seleccione un alimento")) {
                    String[] parts = selectedItem.split(" - ");
                    String foodName = parts[0];
                    String foodCalories = parts[1].replace(" cal", "");

                    editTextMealName.setText(foodName);
                    editTextCaloriesMeal.setText(foodCalories);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        if (hora >= 18 && hora < 23 && totalCaloriesConsumed==0) {
            lanzarNotificacion4();
        }
        lanzarNotificacion3();
        showNotificationIntervalDialog();


    }



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
                caloriasDiarias -= 300;
                break;
            case "Mantener peso":
                // No se hace ajuste
                break;
        }

        controlador = caloriasDiarias;

        textViewResultadoCalorias.setText("Calorías diarias: " + Math.round(caloriasDiarias));
    }



    private void quitarCalorias(){
        String caloriasDeporte = sportCalories.getText().toString();
        String nombreDeporte = sportName.getText().toString();

        if (caloriasDeporte.isEmpty() || nombreDeporte.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre y las calorías de la actividad física", Toast.LENGTH_SHORT).show();
            return;
        }

        int caloriasQuemadas = Integer.parseInt(caloriasDeporte);


        if (totalCaloriesConsumed >= caloriasQuemadas) {
            totalCaloriesConsumed = totalCaloriesConsumed - caloriasQuemadas;
            textViewTotalCaloriesConsumed.setText("Total de calorías consumidas hoy: " + totalCaloriesConsumed);
        } else{
            totalCaloriesConsumed = 0;
            textViewTotalCaloriesConsumed.setText("Total de calorías consumidas hoy: " + totalCaloriesConsumed);

        }

        sportCalories.setText("");
        sportName.setText("");


    }

    private void addMealCalories() {
        String mealCaloriesStr = editTextCaloriesMeal.getText().toString();
        String mealName = editTextMealName.getText().toString();


        if (mealCaloriesStr.isEmpty() || mealName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre y las calorías de la comida", Toast.LENGTH_SHORT).show();
            return;
        }

        int mealCalories = Integer.parseInt(mealCaloriesStr);
        totalCaloriesConsumed += mealCalories;

        textViewTotalCaloriesConsumed.setText("Total de calorías consumidas hoy: " + totalCaloriesConsumed);

        if (totalCaloriesConsumed > controlador && controlador != 0) {
            exceso = totalCaloriesConsumed - controlador;
            lanzarNotificacion(exceso);
            caloriasFaltantes.setText("META DIARIA: Ha superado el total de calorias que deberia consumir en el día");
        }else if (totalCaloriesConsumed > controlador && controlador == 0){

        }else{
            Double resta;
            resta = controlador - totalCaloriesConsumed;
            caloriasFaltantes.setText("META DIARIA: Le resta consumir " + resta + " calorias.");
        }

        editTextMealName.setText("");
        editTextCaloriesMeal.setText("");
        spinnerFoodCatalog.setSelection(0);
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle("Alerta de Exceso de Calorías")
                .setContentText("Exceso: " + exceso + "\nHa consumido más calorías de las\n" +
                        "recomendadas en un día, se le\n" +
                        "sugiera realizar ejercicio o reducir las calorías en la próxima comida")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    public void lanzarNotificacion2() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle("Motivación para tu Objetivo")
                .setContentText("¡Sigue adelante! Recuerda tu objetivo y mantente motivado.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(2, builder.build()); // Usa un ID diferente para esta notificación
        }
    }

    public void lanzarNotificacion3() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        String comida = "¡No olvides registrar tu desayuno!";
        if (hora >= 1 && hora < 12) {
            comida = "¡No olvides registrar tu desayuno!";
        } else if (hora >= 12 && hora < 18) {
            comida = "¡No olvides registrar tu almuerzo!";
        } else {
            comida = "¡No olvides registrar tu cena!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle("Recordatorio")
                .setContentText(comida)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(3, builder.build());
        }

    }


    public void lanzarNotificacion4() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_send_24)
                .setContentTitle("¡Alerta!")
                .setContentText("No has registrado nunca comida durante el día.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(3, builder.build());
        }

    }





    private void showNotificationIntervalDialog() {
        String[] intervals = {"15 minutos", "30 minutos", "45 minutos", "60 minutos"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Establece el intervalo de notificación de motivación");
        builder.setCancelable(false);

        builder.setSingleChoiceItems(intervals, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    notificationIntervalMinutes = 15;
                    break;
                case 1:
                    notificationIntervalMinutes = 30;
                    break;
                case 2:
                    notificationIntervalMinutes = 45;
                    break;
                case 3:
                    notificationIntervalMinutes = 60;
                    break;
            }
        });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            if (notificationIntervalMinutes > 0) {
                Toast.makeText(this, "Notificación cada " + notificationIntervalMinutes + " minutos", Toast.LENGTH_SHORT).show();
                scheduleMotivationNotification(notificationIntervalMinutes);
            } else {
                Toast.makeText(this, "Seleccione un intervalo", Toast.LENGTH_SHORT).show();
                showNotificationIntervalDialog();
            }
        });

        builder.show();
    }

    private void scheduleMotivationNotification(int minutes) {
        Intent notificationIntent = new Intent(this, MotivationNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = (long) minutes * 60 * 1000;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, intervalMillis, pendingIntent);
    }


}
