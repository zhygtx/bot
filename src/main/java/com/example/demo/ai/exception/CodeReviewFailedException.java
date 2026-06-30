package com.example.demo.ai.exception;

import lombok.Getter;

/**
 * 编译前代码审查未通过。
 */
@Getter
public class CodeReviewFailedException extends RuntimeException {

    private final Object reviewResult;

    public CodeReviewFailedException(String message, Object reviewResult) {
        super(message);
        this.reviewResult = reviewResult;
    }
}
