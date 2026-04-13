package com.clipro.llm;

import java.util.List;

/**
 * Chat completion request model.
 */
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private double temperature = 0.7;
    private int maxTokens = 4000;
    private boolean stream = false;
    private List<Tool> tools;

    public ChatRequest() {}

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }

    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools; }

    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Tool {
        private String name;
        private String description;
        private ToolParameters parameters;

        public Tool() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ToolParameters getParameters() { return parameters; }
        public void setParameters(ToolParameters parameters) { this.parameters = parameters; }
    }

    public static class ToolParameters {
        private String type = "object";
        private java.util.Map<String, Property> properties = new java.util.HashMap<>();
        private List<String> required;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public java.util.Map<String, Property> getProperties() { return properties; }
        public void setProperties(java.util.Map<String, Property> properties) { this.properties = properties; }

        public List<String> getRequired() { return required; }
        public void setRequired(List<String> required) { this.required = required; }
    }

    public static class Property {
        private String type;
        private String description;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
