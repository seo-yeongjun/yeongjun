package com.yeongjun.yeongjun.global.DTO;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Getter
public class FileResponse {
    private final StreamingResponseBody streamingResponseBody;
    private final String contentType;

    public FileResponse(StreamingResponseBody streamingResponseBody, String contentType) {
        this.streamingResponseBody = streamingResponseBody;
        this.contentType = contentType;
    }

}

