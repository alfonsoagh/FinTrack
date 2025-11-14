package com.pascm.fintrack.ui.grupo;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.GroupMemberEntity;
import com.pascm.fintrack.data.local.entity.NotificationEntity;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.repository.GroupRepository;
import com.pascm.fintrack.data.repository.NotificationRepository;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.util.ImageHelper;
import com.pascm.fintrack.util.SessionManager;

import java.time.Instant;

public class InvitarMiembrosDialogFragment extends DialogFragment {

    private TextInputEditText etEmail;
    private MaterialButton btnBuscar;
    private MaterialButton btnInvitar;
    private MaterialButton btnCancelar;
    private MaterialCardView cardUserInfo;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvMessage;
    private ImageView ivUserPhoto;

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private NotificationRepository notificationRepository;

    private User foundUser = null;
    private long groupId;
    private String groupName;
    private long currentUserId;

    public static InvitarMiembrosDialogFragment newInstance(long groupId, String groupName) {
        InvitarMiembrosDialogFragment fragment = new InvitarMiembrosDialogFragment();
        Bundle args = new Bundle();
        args.putLong("groupId", groupId);
        args.putString("groupName", groupName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_FinTrack);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_invitar_miembros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        userRepository = new UserRepository(requireContext());
        groupRepository = new GroupRepository(requireContext());
        notificationRepository = new NotificationRepository(requireContext());
        currentUserId = SessionManager.getUserId(requireContext());

        // Get arguments
        if (getArguments() != null) {
            groupId = getArguments().getLong("groupId", -1);
            groupName = getArguments().getString("groupName", "");
        }

        initViews(view);
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        btnBuscar = view.findViewById(R.id.btn_buscar);
        btnInvitar = view.findViewById(R.id.btn_invitar);
        btnCancelar = view.findViewById(R.id.btn_cancelar);
        cardUserInfo = view.findViewById(R.id.card_user_info);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvMessage = view.findViewById(R.id.tv_message);
        ivUserPhoto = view.findViewById(R.id.iv_user_photo);
    }

    private void setupListeners() {
        btnBuscar.setOnClickListener(v -> buscarUsuario());
        btnInvitar.setOnClickListener(v -> invitarUsuario());
        btnCancelar.setOnClickListener(v -> dismiss());
    }

    private void buscarUsuario() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            showMessage("Por favor ingresa un email", false);
            return;
        }

        // Hide previous results
        cardUserInfo.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
        foundUser = null;
        btnInvitar.setEnabled(false);

        // Search for user
        userRepository.getUserByEmail(email).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Check if user is not the current user
                if (user.getUserId() == currentUserId) {
                    showMessage("No puedes invitarte a ti mismo", false);
                    return;
                }

                // Check if user is already a member
                groupRepository.getMembersByGroupId(groupId).observe(getViewLifecycleOwner(), members -> {
                    if (members != null) {
                        boolean alreadyMember = false;
                        for (GroupMemberEntity member : members) {
                            if (member.getUserId() == user.getUserId()) {
                                alreadyMember = true;
                                break;
                            }
                        }

                        if (alreadyMember) {
                            showMessage("Este usuario ya es miembro del grupo", false);
                        } else {
                            // User found and can be invited
                            foundUser = user;
                            showUserInfo(user);
                        }
                    }
                });
            } else {
                showMessage("No se encontró ningún usuario con ese email", false);
            }
        });
    }

    private void showUserInfo(User user) {
        userRepository.getUserProfile(user.getUserId()).observe(getViewLifecycleOwner(), profile -> {
            String displayName = profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty()
                    ? profile.getFullName()
                    : user.getEmail();

            tvUserName.setText(displayName);
            tvUserEmail.setText(user.getEmail());
            cardUserInfo.setVisibility(View.VISIBLE);
            btnInvitar.setEnabled(true);
            tvMessage.setVisibility(View.GONE);

            // Load user photo if available
            if (profile != null && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                Bitmap bitmap = ImageHelper.loadBitmapFromPath(profile.getAvatarUrl());
                if (bitmap != null) {
                    ivUserPhoto.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void showMessage(String message, boolean isSuccess) {
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(
                isSuccess ? R.color.success_green : R.color.error_red,
                null
        ));
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void invitarUsuario() {
        if (foundUser == null) {
            showMessage("Por favor busca un usuario primero", false);
            return;
        }

        // Create notification for the invited user
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(foundUser.getUserId());
        notification.setType("GROUP_INVITATION");
        notification.setTitle("Invitación a grupo");
        notification.setMessage("Has sido invitado a unirte al grupo: " + groupName);
        notification.setRelatedEntityId(groupId); // Store group ID
        notification.setCreatedAt(Instant.now());
        notification.setRead(false);

        // Save notification (ajustar al método real existente en el repositorio)
        notificationRepository.insert(notification);

        showMessage("¡Invitación enviada exitosamente!", true);

        etEmail.postDelayed(this::dismiss, 1500);
    }
}
