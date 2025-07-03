package com.allenhouse.hireeasy;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AgentMain extends AppCompatActivity {

    EditText editUsername, editEmail, editMobile, editPassword;
    ImageView eyeIcon, warnUsername, warnEmail, warnMobile;
    Button btnSubmit;
    RecyclerView recyclerView;
    TextView textTotalAgents;

    List<AgentRegistrationModel> agentList;
    AgentRegistrationAdapter adapter;

    int editPosition = -1;
    boolean isEditing = false;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);

        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editMobile = findViewById(R.id.editMobile);
        editPassword = findViewById(R.id.editPassword);
        eyeIcon = findViewById(R.id.eyeIcon);
        btnSubmit = findViewById(R.id.btnSubmit);
        recyclerView = findViewById(R.id.recyclerView);
        textTotalAgents = findViewById(R.id.textTotalAgents);

        // Add warning icons
        warnUsername = findViewById(R.id.warnUsername);
        warnEmail = new ImageView(this);
        warnMobile = new ImageView(this);

        warnEmail.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.warning));
        warnMobile.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.warning));

        warnEmail.setVisibility(View.GONE);
        warnMobile.setVisibility(View.GONE);

        ((ViewGroup) editEmail.getParent()).addView(warnEmail);
        ((ViewGroup) editMobile.getParent()).addView(warnMobile);

        editUsername.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().matches("^[a-zA-Z\\s]+") && !s.toString().isEmpty()) {
                    warnUsername.setVisibility(View.GONE);
                    editEmail.setEnabled(true);
                } else {
                    warnUsername.setVisibility(View.VISIBLE);
                    editEmail.setEnabled(false);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("@")) {
                    warnEmail.setVisibility(View.GONE);
                    editMobile.setEnabled(true);
                } else {
                    warnEmail.setVisibility(View.VISIBLE);
                    editMobile.setEnabled(false);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        editMobile.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (mobile.matches("^[5-9][0-9]{9}$")) {
                    warnMobile.setVisibility(View.GONE);
                } else {
                    warnMobile.setVisibility(View.VISIBLE);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        agentList = new ArrayList<>();

        adapter = new AgentRegistrationAdapter(agentList,
                (model, position) -> {
                    editUsername.setText(model.getName());
                    editEmail.setText(model.getEmail());
                    editMobile.setText(model.getMobile());
                    editPassword.setText(model.getPassword());
                    isEditing = true;
                    editPosition = position;
                    btnSubmit.setText("Update");
                    btnSubmit.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                },
                position -> {
                    agentList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateTotalAgents();
                    Toast.makeText(this, "Agent deleted", Toast.LENGTH_SHORT).show();
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        eyeIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.hide);
            } else {
                editPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.open_eye);
            }
            editPassword.setSelection(editPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        btnSubmit.setOnClickListener(v -> {
            String name = editUsername.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            boolean isValid = true;

            if (!name.matches("^[a-zA-Z\\s]+")) {
                warnUsername.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!email.contains("@")) {
                warnEmail.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!mobile.matches("^[5-9][0-9]{9}$")) {
                warnMobile.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValid) {
                Toast.makeText(this, "Please fix input errors", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing && editPosition >= 0) {
                AgentRegistrationModel updatedAgent = new AgentRegistrationModel(
                        name,
                        agentList.get(editPosition).getAgentId(),
                        email,
                        mobile,
                        password
                );
                agentList.set(editPosition, updatedAgent);
                adapter.notifyItemChanged(editPosition);
                Toast.makeText(this, "Agent updated", Toast.LENGTH_SHORT).show();
            } else {
                String id = UUID.randomUUID().toString().substring(0, 8);
                AgentRegistrationModel newAgent = new AgentRegistrationModel(name, id, email, mobile, password);
                agentList.add(newAgent);
                adapter.notifyItemInserted(agentList.size() - 1);
            }

            editUsername.setText("");
            editEmail.setText("");
            editMobile.setText("");
            editPassword.setText("");

            updateTotalAgents();
            isEditing = false;
            editPosition = -1;
            btnSubmit.setText("Submit");
            btnSubmit.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
        });

        updateTotalAgents();
    }

    private void updateTotalAgents() {
        textTotalAgents.setText(String.valueOf(agentList.size()));
    }
}
