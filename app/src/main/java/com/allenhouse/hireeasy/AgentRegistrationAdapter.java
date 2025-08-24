package com.allenhouse.hireeasy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AgentRegistrationAdapter extends RecyclerView.Adapter<AgentRegistrationAdapter.AgentViewHolder> {

    private final List<AgentRegistrationModel> agentList;
    private final OnAgentEditListener editListener;
    private final OnAgentDeleteListener deleteListener;

    public interface OnAgentEditListener {
        void onEdit(AgentRegistrationModel model, int position);
    }

    public interface OnAgentDeleteListener {
        void onDelete(int position);
    }

    public AgentRegistrationAdapter(List<AgentRegistrationModel> agentList,
                                    OnAgentEditListener editListener,
                                    OnAgentDeleteListener deleteListener) {
        this.agentList = agentList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AgentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agent_list, parent, false);
        return new AgentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgentViewHolder holder, int position) {
        AgentRegistrationModel model = agentList.get(position);

        holder.agentId.setText(model.getAgentId());
        holder.name.setText(model.getName());
        holder.email.setText(model.getEmail());
        holder.mobile.setText(model.getMobile());
        holder.password.setText("••••••••");

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(model, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return agentList.size();
    }

    public static class AgentViewHolder extends RecyclerView.ViewHolder {
        TextView agentId, name, mobile, email, password;
        ImageButton btnEdit, btnDelete;

        public AgentViewHolder(@NonNull View itemView) {
            super(itemView);
            agentId = itemView.findViewById(R.id.tvAgentId);
            name = itemView.findViewById(R.id.tvName);
            mobile = itemView.findViewById(R.id.tvMobile);
            email = itemView.findViewById(R.id.tvEmail);
            password = itemView.findViewById(R.id.tvPassword);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}