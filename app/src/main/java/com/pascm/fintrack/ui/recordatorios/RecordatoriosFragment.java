package com.pascm.fintrack.ui.recordatorios;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.databinding.FragmentRecordatoriosBinding;

public class RecordatoriosFragment extends Fragment {

    private FragmentRecordatoriosBinding binding;

    public RecordatoriosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecordatoriosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.fabAddReminder.setOnClickListener(v ->
                Toast.makeText(requireContext(), binding.fabAddReminder.getContentDescription(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
