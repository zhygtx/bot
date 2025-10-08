package com.news.web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserTpl {
    private Long userId;
    private Long groupId;
    private String templateName;
}
