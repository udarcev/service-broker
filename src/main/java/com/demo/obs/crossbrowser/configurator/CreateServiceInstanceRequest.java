package com.demo.obs.crossbrowser.configurator;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;

public class CreateServiceInstanceRequest {
    @NotEmpty
    @JsonProperty("versions_count")
    private String versionsCount;
    @NotEmpty
    @JsonProperty("active_browsers_count")
    private String activeBrowsersCount;
    @NotEmpty
    @JsonProperty("browsers_name")
    private String[] browsersName;

    public CreateServiceInstanceRequest(@NotEmpty String versionsCount, @NotEmpty String activeBrowsersCount, @NotEmpty String[] browsersName) {
        this.versionsCount = versionsCount;
        this.activeBrowsersCount = activeBrowsersCount;
        this.browsersName = browsersName;
    }

    public String getVersionsCount() {
        return versionsCount;
    }

    public String getActiveBrowsersCount() {
        return activeBrowsersCount;
    }

    public String[] getBrowsersName() {
        return browsersName;
    }

    public static CreateServiceInstanceRequest.CreateServiceInstanceRequestBuilder builder() {
        return new CreateServiceInstanceRequest.CreateServiceInstanceRequestBuilder();
    }

    public static class CreateServiceInstanceRequestBuilder {
        private String versionsCount;
        private String activeBrowsersCount;
        private String[] browsersName;

        public CreateServiceInstanceRequestBuilder setVersionsCount(String versionsCount) {
            this.versionsCount = versionsCount;
            return this;
        }

        public CreateServiceInstanceRequestBuilder setActiveBrowsersCount(String activeBrowsersCount) {
            this.activeBrowsersCount = activeBrowsersCount;
            return this;
        }

        public CreateServiceInstanceRequestBuilder setBrowsersName(String[] browsersName) {
            this.browsersName = browsersName;
            return this;
        }

        public CreateServiceInstanceRequest build() {
            return new CreateServiceInstanceRequest(this.versionsCount, this.activeBrowsersCount, this.browsersName);
        }
    }
}

