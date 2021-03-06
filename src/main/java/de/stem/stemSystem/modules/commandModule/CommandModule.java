/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.stem.stemSystem.modules.commandModule;

import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.AbstractModule;
import de.stem.stemSystem.modules.commandModule.defaultCommands.CommandSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandModule extends AbstractModule implements Runnable {
    private final STEMSystemApp stemSystemApp;
    private final CommandSetup commandSetup;
    private boolean moduleAlive;

    public CommandModule(STEMSystemApp stemSystemApp) {
        this.stemSystemApp = stemSystemApp;
        this.moduleAlive = true;
        this.commandSetup = new CommandSetup(this.stemSystemApp);
        this.stemSystemApp.getScheduler().runTask(this.getModulePlugin(), this);
    }

    @Override
    public void run() {
        while (moduleAlive) {
            String input = System.console().readLine();

            String[] inputArray = input.split(" ");
            String command = inputArray[0];

            String[] args = Arrays.copyOfRange(inputArray, 1, inputArray.length);
            this.commandSetup.runCommand(command, args);
        }
    }

    public void registerCommand(String command, ICommand ICommand) {
        STEMSystemApp.LOGGER.INFO("Register new command #" + command);
        commandSetup.terminalExecutes.put(command, ICommand);
    }

    public void unregisterCommand(String command) {
        commandSetup.terminalExecutes.remove(command);
    }

    public List<String> getCommandList() {
        return new ArrayList<>(this.commandSetup.terminalExecutes.keySet());
    }

    @Override
    public void onShutdown() {
        this.moduleAlive = false;
    }
}
