package io.github.sanyavertolet.edukate.messaging

object RabbitTopology {
    const val EXCHANGE: String = "edukate.exchange"

    object Rk {
        const val SCHEDULE: String = "edukate.check.schedule.v1"
        const val RESULT: String = "edukate.check.result.v1"
        const val NOTIFY: String = "edukate.notify.v1"
    }

    object Q {
        const val SCHEDULE_CHECKER: String = "checker.check.schedule.v1.q"
        const val RESULT_BACKEND: String = "backend.check.result.v1.q"
        const val NOTIFY: String = "notifier.notify.v1.q"
    }
}
