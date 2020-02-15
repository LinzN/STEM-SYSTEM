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

package de.azcore.azcoreRuntime.internal.containers;

import de.azcore.azcoreRuntime.AZCoreRuntimeApp;

public abstract class Module {

    public Module() {
        AZCoreRuntimeApp.logger("Load module " + this.getClass().getSimpleName(), true, false);
    }
}
