package com.generalbot.bot.metadata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldMetadata {

    private String fieldName;

    private String fieldType;

    private String description;

    private int order;

    private boolean inherited;

    private boolean required;

    private String example;
}
