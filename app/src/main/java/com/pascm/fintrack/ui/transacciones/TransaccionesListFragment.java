package com.pascm.fintrack.ui.transacciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.databinding.FragmentTransaccionesListBinding;
import com.pascm.fintrack.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TransaccionesListFragment extends Fragment {

    private FragmentTransaccionesListBinding binding;
    private TransactionRepository transactionRepository;
    private TransaccionesAdapter adapter;
    private Transaction.TransactionType currentFilter = null; // null = todas

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransaccionesListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionRepository = new TransactionRepository(requireContext());

        // Obtener filtro de argumentos si existe
        if (getArguments() != null) {
            String filterType = getArguments().getString("filterType", "ALL");
            currentFilter = getFilterFromString(filterType);
        }

        setupUI();
        setupRecyclerView();
        loadTransactions();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Botones de filtro
        binding.btnAll.setOnClickListener(v -> filterTransactions(null));
        binding.btnGastos.setOnClickListener(v -> filterTransactions(Transaction.TransactionType.EXPENSE));
        binding.btnIngresos.setOnClickListener(v -> filterTransactions(Transaction.TransactionType.INCOME));
        binding.btnTransferencias.setOnClickListener(v -> filterTransactions(Transaction.TransactionType.TRANSFER));

        updateFilterUI();
    }

    private void setupRecyclerView() {
        adapter = new TransaccionesAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void loadTransactions() {
        long userId = SessionManager.getUserId(requireContext());

        transactionRepository.getAllTransactions(userId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                List<Transaction> filtered = filterByType(transactions);
                adapter.setTransactions(filtered);

                if (filtered.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.rvTransactions.setVisibility(View.GONE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.rvTransactions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private List<Transaction> filterByType(List<Transaction> all) {
        if (currentFilter == null) return all;

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            if (t.getType() == currentFilter) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private void filterTransactions(Transaction.TransactionType type) {
        currentFilter = type;
        updateFilterUI();
        loadTransactions();
    }

    private void updateFilterUI() {
        // Reset todos
        binding.btnAll.setBackgroundResource(R.drawable.bg_filter_button_unselected);
        binding.btnGastos.setBackgroundResource(R.drawable.bg_filter_button_unselected);
        binding.btnIngresos.setBackgroundResource(R.drawable.bg_filter_button_unselected);
        binding.btnTransferencias.setBackgroundResource(R.drawable.bg_filter_button_unselected);

        // Marcar seleccionado
        if (currentFilter == null) {
            binding.btnAll.setBackgroundResource(R.drawable.bg_filter_button_selected);
        } else if (currentFilter == Transaction.TransactionType.EXPENSE) {
            binding.btnGastos.setBackgroundResource(R.drawable.bg_filter_button_selected);
        } else if (currentFilter == Transaction.TransactionType.INCOME) {
            binding.btnIngresos.setBackgroundResource(R.drawable.bg_filter_button_selected);
        } else {
            binding.btnTransferencias.setBackgroundResource(R.drawable.bg_filter_button_selected);
        }
    }

    private Transaction.TransactionType getFilterFromString(String filter) {
        switch (filter) {
            case "EXPENSE": return Transaction.TransactionType.EXPENSE;
            case "INCOME": return Transaction.TransactionType.INCOME;
            case "TRANSFER": return Transaction.TransactionType.TRANSFER;
            default: return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
