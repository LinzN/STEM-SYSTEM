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

package de.azcore.azcoreRuntime.taskManagment;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.openJL.pairs.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

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
        abstractCallback.disable();
    }

    public void unregisterCallbackListeners(AZPlugin azPlugin) {
        for (Iterator<AbstractCallback> iterator = this.callbackListeners.keySet().iterator(); iterator.hasNext(); ) {
            AbstractCallback abstractCallback = iterator.next();
            AZPlugin azPlugin1 = this.callbackListeners.get(abstractCallback);
            if (azPlugin == azPlugin1) {
                abstractCallback.disable();
            }
        }
    }

    void removeFromList(AbstractCallback abstractCallback) {
        this.callbackListeners.remove(abstractCallback);
    }

    private void enableCallbackListener(AbstractCallback abstractCallback, AZPlugin plugin) {
        CallbackTime callbackTime = abstractCallback.getTime();
        AZTask azTask;

        Runnable task = () -> callMethod(abstractCallback);

        if (callbackTime.fixedTask) {
            azTask = AZCoreRuntimeApp.getInstance().getScheduler().runFixedScheduler(plugin, task, callbackTime.days, callbackTime.hours, callbackTime.minutes, callbackTime.daily);
        } else {
            azTask = AZCoreRuntimeApp.getInstance().getScheduler().runRepeatScheduler(plugin, task, callbackTime.delay, callbackTime.period, callbackTime.timeUnit);
        }

        AZTask callbackAzTask = AZCoreRuntimeApp.getInstance().getScheduler().runRepeatScheduler(plugin, () -> {
            if (azTask.isCanceled) {
                abstractCallback.disable();
            } else {
                try {
                    if (!abstractCallback.callbackData.isEmpty()) {
                        Object object = abstractCallback.callbackData.removeFirst();
                        abstractCallback.callback(object);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

        abstractCallback.setIDs(azTask.getTaskId(), callbackAzTask.getTaskId());

    }

    private void callMethod(AbstractCallback abstractCallback) {
        abstractCallback.methodToCall();

        while (!abstractCallback.operationData.isEmpty()) {
            Pair<TaskOperation, Object> pair = abstractCallback.operationData.removeFirst();

            Object object = pair.getKey().runOperation(pair.getValue());
            abstractCallback.callback(object);
        }
    }
}
