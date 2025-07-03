package com.allenhouse.hireeasy;

public class AgentRegistrationModel {
    private String name, agentId, email, mobile, password;

    public AgentRegistrationModel(String name, String agentId, String email, String mobile, String password) {
        this.name = name;
        this.agentId = agentId;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
    }

    public String getName() { return name; }
    public String getAgentId() { return agentId; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPassword() { return password; }
}
