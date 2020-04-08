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

package de.stem.stemSystem.taskManagment;

import de.stem.stemSystem.AppLogger;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.AZPlugin;
import de.stem.stemSystem.taskManagment.operations.AbstractOperation;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;

import java.util.HashMap;
import java.util.Iterator;

public class CallbackService {
    private HashMap<AbstractCallback, AZPlugin> callbackListeners;

    CallbackService() {
        this.callbackListeners = new HashMap<>();
    }

    public void registerCallbackListener(AbstractCallback abstractCallback, AZPlugin azPlugin) {
        this.callbackListeners.put(abstractCallback, azPlugin);
        this.enableCallbackListener(abstractCallback, azPlugin);
    }

    public void unregisterCallbackListener(AbstractCallback abstractCallback) {
        STEMSystemApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
        this.callbackListeners.remove(abstractCallback);
        AppLogger.debug("Callback unregister: " + abstractCallback.getClass().getSimpleName());
    }

    public void unregisterCallbackListeners(AZPlugin azPlugin) {
        for (Iterator<AbstractCallback> iterator = this.callbackListeners.keySet().iterator(); iterator.hasNext(); ) {
            AbstractCallback abstractCallback = iterator.next();
            AZPlugin azPlugin1 = this.callbackListeners.get(abstractCallback);
            if (azPlugin == azPlugin1) {
                STEMSystemApp.getInstance().getScheduler().cancelTask(abstractCallback.taskId);
                this.callbackListeners.remove(abstractCallback);
                AppLogger.debug("Callback unregister: " + abstractCallback.getClass().getSimpleName() + " from " + azPlugin.getPluginName());
            }
        }
    }

    public HashMap<AbstractCallback, AZPlugin> getCallbackListeners() {
        return this.callbackListeners;
    }

    private void enableCallbackListener(AbstractCallback abstractCallback, AZPlugin plugin) {
        CallbackTime callbackTime = abstractCallback.getTime();
        AZTask azTask;

        Runnable runnable = () -> callMethod(abstractCallback, plugin);
        if (callbackTime.fixedTask) {
            azTask = STEMSystemApp.getInstance().getScheduler().runFixedScheduler(plugin, runnable, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
        } else {
            azTask = STEMSystemApp.getInstance().getScheduler().runRepeatScheduler(plugin, runnable, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
        }
        AppLogger.debug("Callback register for " + plugin.getPluginName() + " with taskId :" + azTask.taskId);
        abstractCallback.setIDs(azTask.getTaskId());
    }

    private void callMethod(AbstractCallback abstractCallback, AZPlugin plugin) {
        abstractCallback.operation();

        while (!abstractCallback.operationData.isEmpty()) {
            AppLogger.debug("Callback operation for " + plugin.getPluginName());
            AbstractOperation abstractOperation = abstractCallback.operationData.removeFirst();

            STEMSystemApp.getInstance().getScheduler().runTask(plugin, () -> {
                OperationOutput operationOutput = abstractOperation.runOperation();
                abstractCallback.callback(operationOutput);
                if (!STEMSystemApp.getInstance().getScheduler().isTask(abstractCallback.taskId)) {
                    this.callbackListeners.remove(abstractCallback);
                    AppLogger.debug("Disable Callback from " + plugin.getPluginName() + " with taskId " + abstractCallback.taskId);
                }
            });
        }
    }
}