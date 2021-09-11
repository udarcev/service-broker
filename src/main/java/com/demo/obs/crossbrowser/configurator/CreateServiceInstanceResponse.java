package com.demo.obs.crossbrowser.configurator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceInstanceResponse {

    @JsonCreator
    public CreateServiceInstanceResponse(@JsonProperty("dashboardUrl") String dashboardUrl, @JsonProperty("ggrUrl") String ggrUrl) {
        this.dashboardUrl = dashboardUrl;
        this.ggrUrl = ggrUrl;
    }

    @JsonProperty("dashboardUrl")
    private final String dashboardUrl;
    @JsonProperty("ggrUrl")
    private final String ggrUrl;

    public static CreateServiceInstanceResponse.CreateServiceInstanceResponseBuilder builder() {
        return new CreateServiceInstanceResponse.CreateServiceInstanceResponseBuilder();
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public String getGgrUrl() {
        return ggrUrl;
    }

    public static class CreateServiceInstanceResponseBuilder {
        private String dashboardUrl;
        private String ggrUrl;

        public CreateServiceInstanceResponseBuilder setDashboardUrl(String dashboardUrl) {
            this.dashboardUrl = dashboardUrl;
            return this;
        }

        public CreateServiceInstanceResponseBuilder setGgrUrl(String ggrUrl) {
            this.ggrUrl = ggrUrl;
            return this;
        }

        public CreateServiceInstanceResponse build() {
            return new CreateServiceInstanceResponse(this.dashboardUrl, this.ggrUrl);
        }
    }
}
