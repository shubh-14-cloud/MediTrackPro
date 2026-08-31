package com.example.meditrackpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthFragment extends Fragment {

    private HealthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        CardView cardEmpty = view.findViewById(R.id.cardEmptyRecords);
        RecyclerView rvRecords = view.findViewById(R.id.rvHealthRecords);
        TextView tvCountRecords = view.findViewById(R.id.tvCountRecords);
        TextView tvCountVaccines = view.findViewById(R.id.tvCountVaccines);
        TextView tvCountLab = view.findViewById(R.id.tvCountLab);
        TextView tvCountDoctors = view.findViewById(R.id.tvCountDoctors);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvRecords.setLayoutManager(layoutManager);

        rvRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int total = layoutManager.getItemCount();
                if (dy > 0 && lastVisible >= total - 3) {
                    viewModel.loadNextPage();
                }
            }
        });

        HealthRecordAdapter adapter = new HealthRecordAdapter(
                record -> {
                    Intent intent = new Intent(requireContext(), HealthRecordDetailActivity.class);
                    intent.putExtra(HealthRecordDetailActivity.EXTRA_RECORD_ID, record.getId());
                    startActivity(intent);
                },
                record -> {
                    viewModel.delete(record);
                    Toast.makeText(requireContext(), "Record deleted", Toast.LENGTH_SHORT).show();
                }
        );
        rvRecords.setAdapter(adapter);

        Button btnAdd = view.findViewById(R.id.btnAddRecord);
        Button btnUpload = view.findViewById(R.id.btnUploadReport);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddHealthRecordActivity.class)));

        btnUpload.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CameraActivity.class)));

        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            tvCountRecords.setText(String.valueOf(records.size()));
            tvCountVaccines.setText(String.valueOf(countByType(records, "Vaccination")));
            tvCountLab.setText(String.valueOf(countByType(records, "Lab Result")));
            tvCountDoctors.setText(String.valueOf(countByType(records, "Doctor Visit")));

            if (records.isEmpty()) {
                cardEmpty.setVisibility(View.VISIBLE);
                rvRecords.setVisibility(View.GONE);
            } else {
                cardEmpty.setVisibility(View.GONE);
                rvRecords.setVisibility(View.VISIBLE);
            }
            adapter.submitList(records);
        });
    }

    private int countByType(List<HealthRecord> records, String type) {
        int count = 0;
        for (HealthRecord record : records) {
            if (type.equals(record.getType())) count++;
        }
        return count;
    }
}
