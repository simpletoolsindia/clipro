package com.clipro;

import com.clipro.logging.Logger;
import com.clipro.ui.Terminal;

public class App {
    private static final Logger LOG = new Logger("App");

    public static void main(String[] args) {
        LOG.info("CLIPRO - Java AI Coding CLI");
        LOG.info("Version: 0.1.0");
        LOG.info("Terminal: " + Terminal.getColumns() + "x" + Terminal.getRows());

        LOG.info("Ready for development!");
        System.out.println(Terminal.blue("Hello World!") + " - Basic output test");
    }
}
