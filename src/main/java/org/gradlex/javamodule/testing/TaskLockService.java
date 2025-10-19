// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

/**
 * No-op service that can serve as 'lock' to prevent multiple test tasks from running in parallel:
 * usesService(gradle.sharedServices.registerIfAbsent(TaskLockService.NAME, TaskLockService::class) { maxParallelUsages = 1 })
 */
public abstract class TaskLockService implements BuildService<BuildServiceParameters.None> {
    public static String NAME = "taskLock";
}
