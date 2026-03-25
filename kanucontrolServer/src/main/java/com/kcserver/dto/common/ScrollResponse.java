package com.kcserver.dto.common;

import java.util.List;

public record ScrollResponse<T>(
        List<T> content,
        long total
) {}