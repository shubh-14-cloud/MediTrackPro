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

public class HealthRecordAdapter extends ListAdapter<HealthRecord, HealthRecordAdapter.ViewHolder> {

    private final Consumer<HealthRecord> onClick;
    private final Consumer<HealthRecord> onLongClick;

    private static final DiffUtil.ItemCallback<HealthRecord> DIFF = new DiffUtil.ItemCallback<HealthRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull HealthRecord a, @NonNull HealthRecord b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull HealthRecord a, @NonNull HealthRecord b) {
            return a.equals(b);
        }
    };

    public HealthRecordAdapter(Consumer<HealthRecord> onClick, Consumer<HealthRecord> onLongClick) {
        super(DIFF);
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthRecord record = getItem(position);

        holder.tvTitle.setText(record.getTitle());
        holder.tvSubtitle.setText(record.getType() + " · " + record.getDate());
        holder.tvStatus.setText(record.getStatus());

        // Short type code shown in the tile; every record uses the same badge look.
        holder.tvIcon.setText(typeCode(record.getType()));

        holder.itemView.setOnClickListener(v -> onClick.accept(record));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClick.accept(record);
            return true;
        });
    }

    static String typeCode(String type) {
        switch (type) {
            case "Lab Result":
                return "Lab";
            case "Prescription":
                return "Rx";
            case "Doctor Visit":
                return "Dr";
            case "Vaccination":
                return "Vax";
            default:
                return "Rec";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon;
        final TextView tvTitle;
        final TextView tvSubtitle;
        final TextView tvStatus;

        ViewHolder(@NonNull View view) {
            super(view);
            tvIcon = view.findViewById(R.id.tvRecordIcon);
            tvTitle = view.findViewById(R.id.tvRecordTitle);
            tvSubtitle = view.findViewById(R.id.tvRecordSubtitle);
            tvStatus = view.findViewById(R.id.tvRecordStatus);
        }
    }
}
