package com.clipro.session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Remote session support for CLIPRO.
 * Manges remote SSH execution using native `ssh` binary.
 * 
 * L-10: Remote session support (SSH to remote CLIPRO)
 */
public class RemoteSession {

    private final String host;
    private final int port;
    private final String user;
    private final String identityFile;
    private boolean connected = false;

    public RemoteSession(String host, int port, String user, String identityFile) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.identityFile = identityFile;
    }

    /**
     * Test connection by simply calling ssh exit.
     */
    public boolean connect() {
        try {
            int exitCode = executeSshCommand("exit");
            connected = (exitCode == 0);
            return connected;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }

    /**
     * Verify if the session is currently connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Gracefully disconnect (stub for compat).
     */
    public void disconnect() {
        connected = false;
    }

    /**
     * Execute a command remotely via SSH and block for the result.
     * Returns string output directly.
     */
    public RemoteCommandResult executeCommand(String command) {
        if (!connected) {
            throw new IllegalStateException("Remote session is not connected to " + host);
        }

        try {
            List<String> args = buildBaseSshCmd();
            args.add(command);

            ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return new RemoteCommandResult(exitCode, output.toString().trim(), error.toString().trim());

        } catch (Exception e) {
            throw new RuntimeException("Failed to run remote command: " + command, e);
        }
    }

    private int executeSshCommand(String command) throws Exception {
        List<String> args = buildBaseSshCmd();
        args.add(command);
        
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();
        return process.waitFor();
    }

    private List<String> buildBaseSshCmd() {
        List<String> args = new ArrayList<>();
        args.add("ssh");
        args.add("-p");
        args.add(String.valueOf(port));
        
        if (identityFile != null && !identityFile.isEmpty()) {
            args.add("-i");
            args.add(identityFile);
        }

        // Add auto host key checking ignore to avoid terminal prompts
        args.add("-o");
        args.add("StrictHostKeyChecking=accept-new");
        args.add("-o");
        args.add("BatchMode=yes");

        String target = host;
        if (user != null && !user.isEmpty()) {
            target = user + "@" + host;
        }
        args.add(target);
        return args;
    }

    public static class RemoteCommandResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        public RemoteCommandResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
