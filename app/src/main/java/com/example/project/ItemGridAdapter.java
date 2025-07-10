package com.example.project;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

public class ItemGridAdapter extends BaseAdapter {

    private Activity context;
    private List<Item> itemList;
    private OnCollectClickListener collectClickListener;

    // Interface to handle collect button click
    public interface OnCollectClickListener {
        void onCollect(Item item);
    }

    // Updated constructor
    public ItemGridAdapter(Activity context, List<Item> itemList, OnCollectClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.collectClickListener = listener;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View gridItem = inflater.inflate(R.layout.griditem, null, true);

        TextView tvName = gridItem.findViewById(R.id.tvGridName);
        TextView tvDesc = gridItem.findViewById(R.id.tvGridDescription);
        TextView tvLocation = gridItem.findViewById(R.id.tvGridLocation);
        TextView tvDateFound = gridItem.findViewById(R.id.tvGridDateFound);
        ImageView imageView = gridItem.findViewById(R.id.ivGridImage);
        Button btnCollect = gridItem.findViewById(R.id.btnCollect);

        Item item = itemList.get(position);

        tvName.setText(item.getName());
        tvDesc.setText(item.getDescription());

        // Location
        String location = item.getLocationDescription();
        tvLocation.setText(location != null && !location.isEmpty() ? "📍 " + location : "📍 Location not available");

        // Date
        String dateFound = item.getDateFound();
        tvDateFound.setText(dateFound != null && !dateFound.isEmpty() ? "📅 " + dateFound : "📅 Unknown date");

        // Image
        String imageBase64 = item.getImageBase64();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // 🔥 Collect button action
        btnCollect.setOnClickListener(v -> {
            if (collectClickListener != null) {
                collectClickListener.onCollect(item);
            }
        });

        return gridItem;
    }
}
