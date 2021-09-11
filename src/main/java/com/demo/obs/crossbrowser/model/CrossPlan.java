package com.demo.obs.crossbrowser.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum CrossPlan {

    LITE("lite-plan", "ab5870f8-c1d5-11ea-b3de-0242ac130004", 1, 1, "chrome"),
    STANDARD("standard-plan", "7c00b60f-babf-4311-9f13-7fb10c11d13e", 1, 1, "chrome", "firefox"),
    EXTENDED("extended-plan", "a816180e-0d10-4de8-b69f-d6ab8f0de995", 3, 3, "chrome", "firefox", "opera");

    private final String planName;
    private final String planId;
    private final int versionsCount;
    private final int activeBrowsersCount;
    private final String[] browsersName;

    CrossPlan(String planName, String planId, int versionsCount, int activeBrowsersCount, String... browsersName) {
        this.planName = planName;
        this.planId = planId;
        this.versionsCount = versionsCount;
        this.activeBrowsersCount = activeBrowsersCount;
        this.browsersName = browsersName;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPlanId() {
        return planId;
    }

    public int getVersionsCount() {
        return versionsCount;
    }

    public int getActiveBrowsersCount() {
        return activeBrowsersCount;
    }

    public String[] getBrowsersName() {
        return browsersName;
    }

    public String getPlanDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Модуль кросс-браузерного тестирования c доступными браузерами ")
                .append(Arrays.stream(browsersName)
                        .collect(Collectors.joining(", ")))
                .append(", с кол-вом последних версий браузеров = ")
                .append(versionsCount)
                .append(", кол-вом одновременно активных сессий = ")
                .append(activeBrowsersCount);
        return sb.toString();
    }
}
