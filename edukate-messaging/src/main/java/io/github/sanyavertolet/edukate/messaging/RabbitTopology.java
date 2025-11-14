package io.github.sanyavertolet.edukate.messaging;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RabbitTopology {
    public static final String EXCHANGE = "edukate.exchange";

    public static final String RK_SCHEDULE = "edukate.check.schedule.v1";
    public static final String Q_SCHEDULE_CHECKER = "checker.check.schedule.v1.q";

    public static final String RK_RESULT   = "edukate.check.result.v1";
    public static final String Q_RESULT_BACKEND   = "backend.check.result.v1.q";

    public static final String RK_NOTIFY = "edukate.notify.v1";
    public static final String Q_NOTIFY  = "notifier.notify.v1.q";
}
