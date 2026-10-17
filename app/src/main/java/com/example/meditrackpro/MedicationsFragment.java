package com.example.meditrackpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class MedicationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnToday = view.findViewById(R.id.btnTabToday);
        Button btnAll = view.findViewById(R.id.btnTabAll);
        Button btnRefill = view.findViewById(R.id.btnTabRefill);

        View layoutToday = view.findViewById(R.id.layoutToday);
        View layoutAll = view.findViewById(R.id.layoutAll);
        View layoutRefill = view.findViewById(R.id.layoutRefill);

        List<Button> allButtons = Arrays.asList(btnToday, btnAll, btnRefill);
        List<View> allLayouts = Arrays.asList(layoutToday, layoutAll, layoutRefill);

        UiHelper.setChoiceSelected(btnToday, true);
        UiHelper.setChoiceSelected(btnAll, false);
        UiHelper.setChoiceSelected(btnRefill, false);

        btnToday.setOnClickListener(v -> showTab(btnToday, layoutToday, allButtons, allLayouts));
        btnAll.setOnClickListener(v -> showTab(btnAll, layoutAll, allButtons, allLayouts));
        btnRefill.setOnClickListener(v -> showTab(btnRefill, layoutRefill, allButtons, allLayouts));

        Button btnAddMed = view.findViewById(R.id.btnAddMed);
        btnAddMed.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddMedicineActivity.class)));

        MedicineViewModel viewModel = ((MainActivity) requireActivity()).getSharedMedicineViewModel();

        // TODAY tab
        CardView cardEmptyToday = view.findViewById(R.id.cardEmptyToday);
        RecyclerView rvToday = view.findViewById(R.id.rvTodayMeds);
        rvToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        MedicineAdapter todayAdapter = new MedicineAdapter(
                this::launchEdit,
                med -> {
                    viewModel.delete(med);
                    Toast.makeText(requireContext(), med.getName() + " removed", Toast.LENGTH_SHORT).show();
                }
        );
        rvToday.setAdapter(todayAdapter);

        // ALL MEDS tab
        CardView cardEmptyAll = view.findViewById(R.id.cardEmptyAll);
        RecyclerView rvAll = view.findViewById(R.id.rvAllMeds);
        rvAll.setLayoutManager(new LinearLayoutManager(requireContext()));
        MedicineAdapter allAdapter = new MedicineAdapter(
                this::launchEdit,
                med -> {
                    viewModel.delete(med);
                    Toast.makeText(requireContext(), med.getName() + " removed", Toast.LENGTH_SHORT).show();
                }
        );
        rvAll.setAdapter(allAdapter);

        // REFILL tab
        CardView cardEmptyRefill = view.findViewById(R.id.cardEmptyRefill);
        RecyclerView rvRefill = view.findViewById(R.id.rvRefillMeds);
        rvRefill.setLayoutManager(new LinearLayoutManager(requireContext()));
        RefillAdapter refillAdapter = new RefillAdapter();
        rvRefill.setAdapter(refillAdapter);

        viewModel.getAllMedicines().observe(getViewLifecycleOwner(), medicines -> {
            boolean empty = medicines == null || medicines.isEmpty();

            cardEmptyToday.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvToday.setVisibility(empty ? View.GONE : View.VISIBLE);

            cardEmptyAll.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvAll.setVisibility(empty ? View.GONE : View.VISIBLE);

            cardEmptyRefill.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvRefill.setVisibility(empty ? View.GONE : View.VISIBLE);

            todayAdapter.submitList(medicines);
            allAdapter.submitList(medicines);
            refillAdapter.submitList(medicines);

            if (!empty) {
                viewModel.ensureDoseLogsForToday(medicines);
            }
        });
    }

    private void showTab(Button activeBtn, View activeLayout, List<Button> allButtons, List<View> allLayouts) {
        for (Button btn : allButtons) {
            UiHelper.setChoiceSelected(btn, btn == activeBtn);
        }
        for (View layout : allLayouts) {
            layout.setVisibility(View.GONE);
        }
        activeLayout.setVisibility(View.VISIBLE);
    }

    private void launchEdit(Medicine med) {
        Intent intent = new Intent(requireContext(), AddMedicineActivity.class);
        intent.putExtra(AddMedicineActivity.EXTRA_MEDICINE_ID, med.getId());
        startActivity(intent);
    }
}
