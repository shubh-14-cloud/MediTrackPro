package com.example.meditrackpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class RefillAdapter extends ListAdapter<Medicine, RefillAdapter.ViewHolder> {

    /** Default refill cycle in days used to calculate the refill progress percentage */
    private static final float DAYS_IN_MONTH = 30f;

    private static final DiffUtil.ItemCallback<Medicine> DIFF = new DiffUtil.ItemCallback<Medicine>() {
        @Override
        public boolean areItemsTheSame(@NonNull Medicine a, @NonNull Medicine b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Medicine a, @NonNull Medicine b) {
            return a.equals(b);
        }
    };

    public RefillAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refill_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine med = getItem(position);
        holder.tvName.setText(med.getName());
        holder.tvDaysLeft.setText(med.getRefillDays() + " days left");
        int pct = (int) ((med.getRefillDays() / DAYS_IN_MONTH) * 100);
        pct = Math.max(0, Math.min(100, pct));
        holder.progress.setProgress(pct);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDaysLeft;
        final ProgressBar progress;

        ViewHolder(@NonNull View view) {
            super(view);
            tvName = view.findViewById(R.id.tvRefillMedName);
            tvDaysLeft = view.findViewById(R.id.tvRefillDaysLeft);
            progress = view.findViewById(R.id.progressRefill);
        }
    }
}
