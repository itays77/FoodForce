package com.example.foodforceapp.Adapters;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforceapp.Models.Meal;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.foodforceapp.R;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        TextView mealTitleTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
        ImageView locationIcon, calendarIcon, peopleIcon, kosherIcon;
        CardView cardView;

        MealViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            mealTitleTextView = itemView.findViewById(R.id.mealTitleTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            numberOfPeopleTextView = itemView.findViewById(R.id.numberOfPeopleTextView);
            kosherTextView = itemView.findViewById(R.id.kosherTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);

            locationIcon = itemView.findViewById(R.id.locationIcon);
            calendarIcon = itemView.findViewById(R.id.calendarIcon);
            peopleIcon = itemView.findViewById(R.id.peopleIcon);
            kosherIcon = itemView.findViewById(R.id.kosherIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMealClick(mealList.get(position).getId());
                }
            });
        }

        public void bind(final Meal meal) {
            mealTitleTextView.setText(meal.getDescription());
            locationTextView.setText(meal.getLocation());
            dateTextView.setText(dateFormat.format(new Date(meal.getDate())));
            numberOfPeopleTextView.setText(String.valueOf(meal.getNumberOfPeople()));
            kosherTextView.setText(meal.isKosher() ? "Kosher" : "Not Kosher");
            statusTextView.setText(meal.getStatus());

            // Set status color
            int statusColor;
            switch (meal.getStatus().toLowerCase()) {
                case "open":
                    statusColor = ContextCompat.getColor(context, R.color.status_open);
                    break;
                case "accepted":
                    statusColor = ContextCompat.getColor(context, R.color.status_accepted);
                    break;
                case "completed":
                    statusColor = ContextCompat.getColor(context, R.color.status_completed);
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.status_default);
                    break;
            }

            // Apply a light tint to the CardView background
            int tintedColor = ColorUtils.setAlphaComponent(statusColor, 25); // 10% opacity
            cardView.setCardBackgroundColor(tintedColor);

            // Set the status text color to match the status
            statusTextView.setTextColor(statusColor);

            // Set icons
            locationIcon.setImageResource(R.drawable.location);
            calendarIcon.setImageResource(R.drawable.calendar);
            peopleIcon.setImageResource(R.drawable.people);
            kosherIcon.setImageResource(R.drawable.kosher);
        }
    }
}