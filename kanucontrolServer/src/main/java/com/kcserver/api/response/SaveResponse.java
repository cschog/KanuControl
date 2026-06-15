package com.kcserver.api.response;

import java.util.List;

public record SaveResponse<T>(
        T data,
        List<String> warnings
) {
}
