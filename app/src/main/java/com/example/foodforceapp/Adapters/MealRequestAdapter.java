package com.example.foodforceapp.Adapters;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforceapp.Models.Meal;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealRequestAdapter extends RecyclerView.Adapter<MealRequestAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Context context;
    private OnMealClickListener listener;

    public interface OnMealClickListener {
        void onMealClick(String mealId);
    }

    public MealRequestAdapter(List<Meal> mealList, OnMealClickListener listener) {
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_request, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
        View statusIndicator;

        MealViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            numberOfPeopleTextView = itemView.findViewById(R.id.numberOfPeopleTextView);
            kosherTextView = itemView.findViewById(R.id.kosherTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMealClick(mealList.get(position).getId());
                }
            });
        }

        public void bind(final Meal meal) {
            descriptionTextView.setText(meal.getDescription());
            locationTextView.setText(meal.getLocation());
            dateTextView.setText(dateFormat.format(new Date(meal.getDate())));
            numberOfPeopleTextView.setText(String.valueOf(meal.getNumberOfPeople()));
            kosherTextView.setText(meal.isKosher() ? "Kosher" : "Not Kosher");
            statusTextView.setText(meal.getStatus());

            // Set status color
            int statusColor;
            switch (meal.getStatus().toLowerCase()) {
                case "open":
                    statusColor = ContextCompat.getColor(context, R.color.md_theme_light_inversePrimary);
                    break;
                case "accepted":
                    statusColor = ContextCompat.getColor(context, R.color.md_theme_light_tertiary);
                    break;
                case "completed":
                    statusColor = ContextCompat.getColor(context, R.color.md_theme_light_onSurface);
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.md_theme_light_shadow);
                    break;
            }
            statusIndicator.setBackgroundColor(statusColor);
        }
    }
}