package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnAddItem, btnLogout, btnAboutUs, btnOurTeam;
    GridView gridViewItems;
    DatabaseReference databaseItems;
    List<Item> itemList;
    ItemGridAdapter adapter;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize views
        btnAddItem = findViewById(R.id.btnAddItem);
        btnLogout = findViewById(R.id.btnLogout);
        btnAboutUs = findViewById(R.id.btnAboutUs);
        btnOurTeam = findViewById(R.id.btnOurTeam);
        gridViewItems = findViewById(R.id.gridViewItems);

        // Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Firebase Database
        databaseItems = FirebaseDatabase.getInstance().getReference("items");

        // Initialize adapter with collect action
        itemList = new ArrayList<>();
        adapter = new ItemGridAdapter(this, itemList, itemToRemove -> {
            if (itemToRemove.getFirebaseKey() != null) {
                databaseItems.child(itemToRemove.getFirebaseKey()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, itemToRemove.getName() + " collected!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Failed to collect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        gridViewItems.setAdapter(adapter);

        // Button actions
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        btnAboutUs.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
        });

        btnOurTeam.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, OurTeamActivity.class));
        });

        // Listen for Firebase updates
        databaseItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    Item item = postSnap.getValue(Item.class);
                    if (item != null) {
                        item.setFirebaseKey(postSnap.getKey());
                        itemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
