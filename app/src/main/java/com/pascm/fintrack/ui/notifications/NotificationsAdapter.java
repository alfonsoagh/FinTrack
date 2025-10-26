package com.pascm.fintrack.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.NotificationEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_NOTIFICATION = 1;

    private final List<Object> items; // Can be String (date header) or NotificationEntity
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationEntity notification);
    }

    public NotificationsAdapter() {
        this.items = new ArrayList<>();
    }

    public void setNotifications(List<NotificationEntity> notifications) {
        items.clear();

        // Group notifications by date
        Map<LocalDate, List<NotificationEntity>> groupedByDate = new LinkedHashMap<>();

        for (NotificationEntity notification : notifications) {
            LocalDate date = notification.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (!groupedByDate.containsKey(date)) {
                groupedByDate.put(date, new ArrayList<>());
            }
            groupedByDate.get(date).add(notification);
        }

        // Build items list with headers
        for (Map.Entry<LocalDate, List<NotificationEntity>> entry : groupedByDate.entrySet()) {
            items.add(formatDate(entry.getKey())); // Add date header
            items.addAll(entry.getValue()); // Add notifications for that date
        }

        notifyDataSetChanged();
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? VIEW_TYPE_HEADER : VIEW_TYPE_NOTIFICATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String date = (String) items.get(position);
            ((HeaderViewHolder) holder).bind(date);
        } else if (holder instanceof NotificationViewHolder) {
            NotificationEntity notification = (NotificationEntity) items.get(position);
            ((NotificationViewHolder) holder).bind(notification, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (date.equals(today)) {
            return "Hoy";
        } else if (date.equals(yesterday)) {
            return "Ayer";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM");
            return date.format(formatter);
        }
    }

    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
        }

        public void bind(String date) {
            tvDate.setText(date);
        }
    }

    // Notification ViewHolder
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgIcon;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
        }

        public void bind(NotificationEntity notification, OnNotificationClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            // Format time
            Instant createdAt = notification.getCreatedAt();
            LocalTime time = createdAt.atZone(ZoneId.systemDefault()).toLocalTime();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            tvTime.setText(time.format(timeFormatter));

            // Show/hide unread indicator
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set icon based on notification type
            int iconRes = getIconForType(notification.getType());
            imgIcon.setImageResource(iconRes);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private int getIconForType(String type) {
            if (type == null) {
                return R.drawable.ic_notifications;
            }

            switch (type) {
                case "EXPENSE":
                    return R.drawable.ic_notifications;
                case "CARD":
                    return R.drawable.ic_credit_card;
                case "GROUP":
                    return R.drawable.ic_notifications;
                case "TRIP":
                    return R.drawable.ic_notifications;
                default:
                    return R.drawable.ic_notifications;
            }
        }
    }
}
