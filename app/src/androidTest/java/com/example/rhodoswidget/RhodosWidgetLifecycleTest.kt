package com.example.rhodoswidget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RhodosWidgetLifecycleTest {

    @Test
    fun activeWidgetRepairsPeriodicWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(RhodosWidgetWorker.PERIODIC_WORK).result.get(5, TimeUnit.SECONDS)
        workManager.pruneWork().result.get(5, TimeUnit.SECONDS)

        RhodosWidgetWorker.ensureScheduled(context, hasWidgets = true)

        waitForPeriodicWork(workManager) { it.state == WorkInfo.State.ENQUEUED }
        RhodosWidgetWorker.cancelAll(context)
    }

    @Test
    fun disablingLastWidgetCancelsPeriodicWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(RhodosWidgetWorker.PERIODIC_WORK).result.get(5, TimeUnit.SECONDS)
        workManager.pruneWork().result.get(5, TimeUnit.SECONDS)

        RhodosWidgetWorker.schedulePeriodic(context)
        waitForPeriodicWork(workManager) { it.state == WorkInfo.State.ENQUEUED }

        RhodosCountdownLargeWidgetProvider().onDisabled(context)

        waitForPeriodicWork(workManager) { it.state == WorkInfo.State.CANCELLED }
    }

    @Test
    fun missingWidgetCancelsRetainedPeriodicWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(RhodosWidgetWorker.PERIODIC_WORK).result.get(5, TimeUnit.SECONDS)
        workManager.pruneWork().result.get(5, TimeUnit.SECONDS)

        RhodosWidgetWorker.schedulePeriodic(context)
        waitForPeriodicWork(workManager) { it.state == WorkInfo.State.ENQUEUED }

        RhodosWidgetWorker.ensureScheduled(context, hasWidgets = false)

        waitForPeriodicWork(workManager) { it.state == WorkInfo.State.CANCELLED }
    }

    private fun waitForPeriodicWork(
        workManager: WorkManager,
        predicate: (WorkInfo) -> Boolean
    ) {
        repeat(50) {
            val work = workManager
                .getWorkInfosForUniqueWork(RhodosWidgetWorker.PERIODIC_WORK)
                .get(5, TimeUnit.SECONDS)
                .lastOrNull()
            if (work != null && predicate(work)) return
            Thread.sleep(100)
        }
        fail("Periodic widget work did not reach the expected state")
    }
}
