package com.example.project;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {

    private static final int IMAGE_CAPTURE_CODE = 1;
    private static final int LOCATION_PICKER_CODE = 2;

    private static final String CHANNEL_ID = "ITEM_SUBMIT_CHANNEL";

    private EditText etItemName, etItemDescription;
    private Button btnTakePicture, btnSubmitItem, btnSelectLocation, btnSelectDate;
    private ImageView capturedImage;
    private TextView tvSelectedLocation, tvDateFound;
    private Bitmap capturedBitmap;

    private DatabaseReference databaseItems;

    private String locationDescription = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String dateFound = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);

        FirebaseApp.initializeApp(this);
        databaseItems = FirebaseDatabase.getInstance().getReference("items");

        // Bind views
        etItemName = findViewById(R.id.etItemName);
        etItemDescription = findViewById(R.id.etItemDescription);
        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnSubmitItem = findViewById(R.id.btnSubmitItem);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        capturedImage = findViewById(R.id.capturedImage);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        tvDateFound = findViewById(R.id.tvDateFound);

        // Notification channel for Android 8+
        createNotificationChannel();

        btnTakePicture.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
        });

        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddItemActivity.this, MapsActivity.class);
            startActivityForResult(intent, LOCATION_PICKER_CODE);
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSubmitItem.setOnClickListener(v -> addItem());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    dateFound = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    tvDateFound.setText("📅 " + dateFound);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void addItem() {
        String name = etItemName.getText().toString().trim();
        String description = etItemDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                capturedBitmap == null || TextUtils.isEmpty(locationDescription) || TextUtils.isEmpty(dateFound)) {
            Toast.makeText(this, "Please complete all fields including date, location, and photo", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image = encodeImageToBase64(capturedBitmap);
        String id = databaseItems.push().getKey();

        Item item = new Item(id, name, description, base64Image, latitude, longitude, locationDescription, dateFound);
        databaseItems.child(id).setValue(item)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                    showItemSubmittedNotification(); // 🔔 Trigger local notification
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Item Submission";
            String description = "Notifications for submitted items";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showItemSubmittedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // ✅ You can replace this with a better icon
                .setContentTitle("Item Submitted!")
                .setContentText("Your item has been successfully posted to Lost & Found.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build()); // 1001 is an arbitrary ID
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK && data != null) {
            capturedBitmap = (Bitmap) data.getExtras().get("data");
            capturedImage.setImageBitmap(capturedBitmap);

        } else if (requestCode == LOCATION_PICKER_CODE && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            locationDescription = data.getStringExtra("locationDesc");

            tvSelectedLocation.setText("📍 " + locationDescription);
            Toast.makeText(this, "Location set:\n" + locationDescription +
                    "\nLat: " + latitude + ", Lng: " + longitude, Toast.LENGTH_SHORT).show();
        }
    }
}
