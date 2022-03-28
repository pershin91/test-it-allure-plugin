package com.github.clientcyc.testit.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Project {
    private String id;
    private String globalId;
    private String name;

}
