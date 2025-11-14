package com.pascm.fintrack.ui.grupo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.repository.GroupRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.util.ImageHelper;

import android.graphics.Bitmap;

import java.text.NumberFormat;
import java.util.Locale;

public class DetalleMiembroFragment extends Fragment {

    private TextView tvMemberName;
    private TextView tvMemberEmail;
    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpenses;
    private TextView tvTransactionCount;
    private Chip chipAdmin;
    private ImageView ivProfilePhoto;

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private GroupRepository groupRepository;

    private long userId;
    private long groupId;
    private boolean isAdmin = false;

    private double totalIncome = 0.0;
    private double totalExpenses = 0.0;
    private int transactionCount = 0;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_miembro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        userRepository = new UserRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());
        groupRepository = new GroupRepository(requireContext());

        // Get arguments
        if (getArguments() != null) {
            userId = getArguments().getLong("userId", -1);
            groupId = getArguments().getLong("groupId", -1);
        }

        initViews(view);
        setupToolbar(view);

        if (userId != -1) {
            loadMemberData();
            loadStatistics();
        }
    }

    private void initViews(View view) {
        tvMemberName = view.findViewById(R.id.tv_member_name);
        tvMemberEmail = view.findViewById(R.id.tv_member_email);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpenses = view.findViewById(R.id.tv_expenses);
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        chipAdmin = view.findViewById(R.id.chip_admin);
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadMemberData() {
        // Load user info
        userRepository.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvMemberEmail.setText(user.getEmail());

                // Load user profile for full name and photo
                userRepository.getUserProfile(userId).observe(getViewLifecycleOwner(), profile -> {
                    if (profile != null) {
                        String fullName = profile.getFullName();
                        if (fullName != null && !fullName.isEmpty()) {
                            tvMemberName.setText(fullName);
                        } else {
                            tvMemberName.setText(user.getEmail());
                        }

                        // Load profile photo if available
                        String photoUrl = profile.getAvatarUrl();
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Bitmap bitmap = ImageHelper.loadBitmapFromPath(photoUrl);
                            if (bitmap != null) {
                                ivProfilePhoto.setImageBitmap(bitmap);
                            }
                        }
                    } else {
                        tvMemberName.setText(user.getEmail());
                    }
                });
            }
        });

        // Check if member is admin
        if (groupId != -1) {
            groupRepository.getMembersByGroupId(groupId).observe(getViewLifecycleOwner(), members -> {
                if (members != null) {
                    for (var member : members) {
                        if (member.getUserId() == userId && member.isAdmin()) {
                            isAdmin = true;
                            chipAdmin.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            });
        }
    }

    private void loadStatistics() {
        // Track how many stats have been loaded (need 3: income, expenses, count)
        final int[] statsLoaded = {0};
        final int TOTAL_STATS = 3;

        // Load total income
        transactionRepository.getTotalIncome(userId).observe(getViewLifecycleOwner(), income -> {
            totalIncome = income != null ? income : 0.0;
            tvIncome.setText(currencyFormat.format(totalIncome));
            statsLoaded[0]++;
            if (statsLoaded[0] == TOTAL_STATS) {
                updateBalance();
            }
        });

        // Load total expenses
        transactionRepository.getTotalExpenses(userId).observe(getViewLifecycleOwner(), expenses -> {
            totalExpenses = expenses != null ? expenses : 0.0;
            tvExpenses.setText(currencyFormat.format(totalExpenses));
            statsLoaded[0]++;
            if (statsLoaded[0] == TOTAL_STATS) {
                updateBalance();
            }
        });

        // Load transaction count
        transactionRepository.getTransactionCount(userId).observe(getViewLifecycleOwner(), count -> {
            transactionCount = count != null ? count : 0;
            tvTransactionCount.setText(String.valueOf(transactionCount));
            statsLoaded[0]++;
            if (statsLoaded[0] == TOTAL_STATS) {
                updateBalance();
            }
        });
    }

    private void updateBalance() {
        double balance = totalIncome - totalExpenses;
        tvBalance.setText(currencyFormat.format(balance));

        // Set color based on balance
        if (balance > 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.success_green, null));
        } else if (balance < 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.error_red, null));
        } else {
            tvBalance.setTextColor(getResources().getColor(R.color.on_surface, null));
        }
    }
}
