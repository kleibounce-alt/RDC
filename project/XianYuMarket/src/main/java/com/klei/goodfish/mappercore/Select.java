package com.klei.goodfish.mappercore;

import java.lang.annotation.*;

/**
 * @author klei
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {
    String value();
}