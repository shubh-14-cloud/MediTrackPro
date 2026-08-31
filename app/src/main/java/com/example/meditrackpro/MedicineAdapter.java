package com.example.meditrackpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

public class MedicineAdapter extends ListAdapter<Medicine, MedicineAdapter.ViewHolder> {

    private final Consumer<Medicine> onEdit;
    private final Consumer<Medicine> onDelete;

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

    public MedicineAdapter(Consumer<Medicine> onEdit, Consumer<Medicine> onDelete) {
        super(DIFF);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine med = getItem(position);
        holder.tvName.setText(med.getName() + " " + med.getDose());
        holder.tvDetails.setText(med.getTimes() + " · Refill in " + med.getRefillDays() + " days");
        holder.tvEdit.setOnClickListener(v -> onEdit.accept(med));
        holder.tvDelete.setOnClickListener(v -> onDelete.accept(med));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDetails;
        final TextView tvEdit;
        final TextView tvDelete;

        ViewHolder(@NonNull View view) {
            super(view);
            tvName = view.findViewById(R.id.tvMedName);
            tvDetails = view.findViewById(R.id.tvMedDetails);
            tvEdit = view.findViewById(R.id.tvEditMed);
            tvDelete = view.findViewById(R.id.tvDeleteMed);
        }
    }
}
