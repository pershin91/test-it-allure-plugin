package com.github.clientcyc.testit.plugin.settings;

import lombok.Data;

import java.util.List;

@Data
public class AutotestDialogSettings {
    private String autotestExternalId;
    private List<String> lecsManualTestsIds;
    private List<String> pmolManualTestsIds;
}
