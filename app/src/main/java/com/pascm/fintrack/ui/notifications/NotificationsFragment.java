package com.pascm.fintrack.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.GroupMemberEntity;
import com.pascm.fintrack.data.local.entity.NotificationEntity;
import com.pascm.fintrack.data.repository.GroupRepository;
import com.pascm.fintrack.data.repository.NotificationRepository;
import com.pascm.fintrack.util.SessionManager;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmptyState;
    private TextView btnMarkAllRead;
    private NotificationsAdapter adapter;
    private NotificationRepository notificationRepository;
    private GroupRepository groupRepository;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notificaciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        notificationRepository = new NotificationRepository(requireContext());
        groupRepository = new GroupRepository(requireContext());
        userId = SessionManager.getUserId(requireContext());

        initViews(view);
        setupListeners(view);
        loadNotifications();
    }

    private void initViews(View view) {
        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // RecyclerView
        rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationsAdapter();
        rvNotifications.setAdapter(adapter);

        // Empty state
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        // Mark all as read button
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
    }

    private void setupListeners(View view) {
        // Mark all as read
        btnMarkAllRead.setOnClickListener(v -> {
            notificationRepository.markAllAsRead(userId);
            Toast.makeText(requireContext(), "Todas las notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show();
        });

        // Notification click listener
        adapter.setOnNotificationClickListener(notification -> {
            // Mark as read when clicked
            if (!notification.isRead()) {
                notificationRepository.markAsRead(notification.getNotificationId());
            }

            // Handle navigation based on notification type
            handleNotificationAction(notification);
        });

        // Invitation action listener
        adapter.setOnInvitationActionListener(new NotificationsAdapter.OnInvitationActionListener() {
            @Override
            public void onAcceptInvitation(NotificationEntity notification) {
                handleAcceptInvitation(notification);
            }

            @Override
            public void onRejectInvitation(NotificationEntity notification) {
                handleRejectInvitation(notification);
            }
        });
    }

    private void loadNotifications() {
        notificationRepository.getAllNotifications(userId).observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                adapter.setNotifications(notifications);
                rvNotifications.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            } else {
                rvNotifications.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleNotificationAction(NotificationEntity notification) {
        String type = notification.getType();
        if (type == null) return;
        switch (type) {
            case "EXPENSE":
                Toast.makeText(requireContext(), "Ver detalle de gasto", Toast.LENGTH_SHORT).show();
                break;
            case "CARD":
                Toast.makeText(requireContext(), "Ver tarjetas", Toast.LENGTH_SHORT).show();
                break;
            case "GROUP":
                Toast.makeText(requireContext(), "Ver grupo", Toast.LENGTH_SHORT).show();
                break;
            case "TRIP":
                Toast.makeText(requireContext(), "Ver viaje", Toast.LENGTH_SHORT).show();
                break;
            case "GROUP_INVITATION":
                // Invitation notifications are handled via accept/reject buttons, no direct navigation.
                break;
            default:
                break;
        }
    }

    private void handleAcceptInvitation(NotificationEntity notification) {
        // Asegurar que sea realmente una invitación
        if (!"GROUP_INVITATION".equals(notification.getType())) {
            Toast.makeText(requireContext(), "Tipo de notificación inválido para esta acción", Toast.LENGTH_SHORT).show();
            return;
        }
        Long groupId = notification.getRelatedId();
        if (groupId == null) {
            Toast.makeText(requireContext(), "Invitación sin grupo asociado", Toast.LENGTH_SHORT).show();
            return;
        }
        GroupMemberEntity newMember = new GroupMemberEntity();
        newMember.setGroupId(groupId);
        newMember.setUserId(userId);
        newMember.setAdmin(false);
        // No existe campo 'active' en GroupMemberEntity; se asume activo al crear.
        groupRepository.addMember(newMember);
        notificationRepository.deleteNotification(notification);
        Toast.makeText(requireContext(), "¡Te has unido al grupo exitosamente!", Toast.LENGTH_SHORT).show();
    }

    private void handleRejectInvitation(NotificationEntity notification) {
        if (!"GROUP_INVITATION".equals(notification.getType())) {
            Toast.makeText(requireContext(), "Tipo de notificación inválido para esta acción", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationRepository.deleteNotification(notification);
        Toast.makeText(requireContext(), "Invitación rechazada", Toast.LENGTH_SHORT).show();
    }
}
