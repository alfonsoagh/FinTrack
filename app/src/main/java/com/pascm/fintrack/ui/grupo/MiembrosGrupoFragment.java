package com.pascm.fintrack.ui.grupo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.GroupEntity;
import com.pascm.fintrack.data.local.entity.GroupMemberEntity;
import com.pascm.fintrack.data.repository.GroupRepository;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MiembrosGrupoFragment extends Fragment {

    private RecyclerView rvMiembros;
    private MaterialButton btnInvitarMiembros;
    private View layoutAdminActions;
    private TextView tvGroupTitle;
    private GroupMembersAdapter adapter;
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private long userId;
    private long groupId;
    private boolean isCurrentUserAdmin = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_miembros_grupo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        groupRepository = new GroupRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        userId = SessionManager.getUserId(requireContext());

        // Get group ID from arguments
        if (getArguments() != null) {
            groupId = getArguments().getLong("groupId", -1);
        }

        initViews(view);
        setupListeners(view);

        if (groupId != -1) {
            loadGroupData();
            loadMembers();
        } else {
            loadUserGroup();
        }
    }

    private void initViews(View view) {
        rvMiembros = view.findViewById(R.id.rv_miembros);
        btnInvitarMiembros = view.findViewById(R.id.btn_invitar_miembros);
        layoutAdminActions = view.findViewById(R.id.layout_admin_actions);
        tvGroupTitle = view.findViewById(R.id.tv_group_title);

        rvMiembros.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GroupMembersAdapter(false); // Will update after checking admin status
        rvMiembros.setAdapter(adapter);
    }

    private void setupListeners(View view) {
        // Botón atrás
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        // Botón invitar miembros (solo admin)
        btnInvitarMiembros.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Funcionalidad de invitar miembros próximamente", Toast.LENGTH_SHORT).show();
        });

        // Listener para eliminar miembros
        adapter.setOnMemberActionListener(member -> showDeleteMemberDialog(member));
    }

    private void loadUserGroup() {
        // If no group ID was passed, try to load user's active group
        groupRepository.getActiveGroupByMemberId(userId).observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                groupId = group.getGroupId();
                loadGroupData();
                loadMembers();
            } else {
                Toast.makeText(requireContext(), "No perteneces a ningún grupo", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void loadGroupData() {
        groupRepository.getGroupById(groupId).observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                tvGroupTitle.setText("Miembros de " + group.getGroupName());
                isCurrentUserAdmin = (group.getAdminUserId() == userId);
                updateAdminUI();
            }
        });
    }

    private void loadMembers() {
        groupRepository.getMembersByGroupId(groupId).observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                loadMembersWithDetails(members);
            }
        });
    }

    private void loadMembersWithDetails(List<GroupMemberEntity> members) {
        List<GroupMemberWithStats> membersWithStats = new ArrayList<>();

        for (GroupMemberEntity member : members) {
            userRepository.getUserById(member.getUserId()).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    // Load user profile for full name
                    userRepository.getUserProfile(user.getUserId()).observe(getViewLifecycleOwner(), profile -> {
                        GroupMemberWithStats memberStats = new GroupMemberWithStats();
                        memberStats.setUserId(user.getUserId());
                        memberStats.setUserName(profile != null && profile.getFullName() != null ?
                                profile.getFullName() : user.getEmail());
                        memberStats.setUserEmail(user.getEmail());
                        memberStats.setAdmin(member.isAdmin());
                        memberStats.setTotalExpenses(0.0); // TODO: Calculate from transactions
                        memberStats.setTransactionCount(0); // TODO: Calculate from transactions

                        membersWithStats.add(memberStats);
                        adapter.setMembers(new ArrayList<>(membersWithStats));
                    });
                }
            });
        }
    }

    private void updateAdminUI() {
        if (isCurrentUserAdmin) {
            layoutAdminActions.setVisibility(View.VISIBLE);
        } else {
            layoutAdminActions.setVisibility(View.GONE);
        }

        // Update adapter with admin status
        adapter = new GroupMembersAdapter(isCurrentUserAdmin);
        adapter.setOnMemberActionListener(member -> showDeleteMemberDialog(member));
        rvMiembros.setAdapter(adapter);
    }

    private void showDeleteMemberDialog(GroupMemberWithStats member) {
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar miembro?")
                .setMessage("¿Estás seguro de que deseas eliminar a " + member.getUserName() + " del grupo?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteMember(member))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMember(GroupMemberWithStats member) {
        groupRepository.removeMember(groupId, member.getUserId());
        Toast.makeText(requireContext(), member.getUserName() + " eliminado del grupo", Toast.LENGTH_SHORT).show();
    }
}
