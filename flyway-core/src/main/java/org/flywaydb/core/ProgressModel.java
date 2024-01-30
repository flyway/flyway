package org.flywaydb.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class ProgressModel {
    private String operation;
    private int step = 1;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private Integer totalSteps;
    private String message;
    private String tag = "progress";

    public void setStepAndTotal(int step) {
        this.step = step;
        if(totalSteps != null && step > totalSteps) {
            totalSteps = step;
        }
    }
}