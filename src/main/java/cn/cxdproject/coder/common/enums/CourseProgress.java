package cn.cxdproject.coder.common.enums;

import lombok.Getter;

/**
 * 课程进度表
 *
 * @author heathcetide
 */
@Getter
public enum CourseProgress {

    NOT_STARTED("NOT_STARTED"),

    IN_PROGRESS("IN_PROGRESS"),

    COMPLETED("COMPLETED");

    private final String value;

    CourseProgress(String value) {
        this.value = value;
    }
}
