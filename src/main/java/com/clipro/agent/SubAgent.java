package com.clipro.agent;

import java.util.*;
import java.util.concurrent.*;

/**
 * Sub-agent for parallel task execution.
 * Enables multi-agent coordination.
 */
public class SubAgent {

    private final String id;
    private final String name;
    private final String model;
    private AgentState state = AgentState.IDLE;
    private String currentTask = "";
    private String result = "";
    private final CompletableFuture<String> future = new CompletableFuture<>();

    public enum AgentState { IDLE, RUNNING, THINKING, ACTING, COMPLETED, FAILED, ERROR }

    public SubAgent(String id, String name, String model) {
        this.id = id;
        this.name = name;
        this.model = model;
    }

    public void assignTask(String task) {
        this.currentTask = task;
        this.state = AgentState.RUNNING;
    }

    public void complete(String result) {
        this.result = result;
        this.state = AgentState.COMPLETED;
        future.complete(result);
    }

    public void fail(String error) {
        this.result = "ERROR: " + error;
        this.state = AgentState.FAILED;
        future.completeExceptionally(new RuntimeException(error));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public AgentState getState() { return state; }
    public String getCurrentTask() { return currentTask; }
    public String getResult() { return result; }
    public CompletableFuture<String> getFuture() { return future; }

    public boolean isRunning() {
        return state == AgentState.RUNNING;
    }

    public void interrupt() {
        if (state == AgentState.RUNNING) {
            future.cancel(true);
            state = AgentState.FAILED;
        }
    }

    public void forceKill() {
        future.cancel(true);
        state = AgentState.FAILED;
    }

    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            future.get(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }
}
