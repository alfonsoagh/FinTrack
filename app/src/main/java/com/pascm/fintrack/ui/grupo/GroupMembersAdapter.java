package com.pascm.fintrack.ui.grupo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder> {

    private List<GroupMemberWithStats> members;
    private boolean isCurrentUserAdmin;
    private OnMemberActionListener listener;
    private final NumberFormat currencyFormat;

    public interface OnMemberActionListener {
        void onDeleteMember(GroupMemberWithStats member);
    }

    public GroupMembersAdapter(boolean isCurrentUserAdmin) {
        this.members = new ArrayList<>();
        this.isCurrentUserAdmin = isCurrentUserAdmin;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }

    public void setMembers(List<GroupMemberWithStats> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        GroupMemberWithStats member = members.get(position);
        holder.bind(member, isCurrentUserAdmin, listener, currencyFormat);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgAvatar;
        private final TextView tvMemberName;
        private final TextView tvMemberStats;
        private final ImageButton btnDeleteMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberStats = itemView.findViewById(R.id.tv_member_stats);
            btnDeleteMember = itemView.findViewById(R.id.btn_delete_member);
        }

        public void bind(GroupMemberWithStats member, boolean isCurrentUserAdmin,
                         OnMemberActionListener listener, NumberFormat currencyFormat) {
            tvMemberName.setText(member.getUserName());

            String stats = "Gastos totales: " + currencyFormat.format(member.getTotalExpenses()) +
                    ", # transacciones: " + member.getTransactionCount();
            tvMemberStats.setText(stats);

            // Show delete button only if current user is admin and this member is not admin
            if (isCurrentUserAdmin && !member.isAdmin()) {
                btnDeleteMember.setVisibility(View.VISIBLE);
                btnDeleteMember.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteMember(member);
                    }
                });
            } else {
                btnDeleteMember.setVisibility(View.GONE);
            }

            // TODO: Load avatar image if available
            // For now, use default avatar background
        }
    }
}
