package com.zhongjia.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "PageResponse", description = "控制层分页响应（字段与 MyBatis-Plus Page 保持兼容）")
public class PageResponse<T> {

    @Schema(description = "当前页码，对应 Page.current", example = "1")
    private Integer current;

    @Schema(description = "每页大小，对应 Page.size", example = "10")
    private Integer size;

    @Schema(description = "总条数，对应 Page.total", example = "100")
    private Long total;

    @Schema(description = "当前页记录，对应 Page.records")
    private List<T> records;

    public static <T> PageResponse<T> of(int current, int size, long total, List<T> records) {
        PageResponse<T> resp = new PageResponse<>();
        resp.setCurrent(current);
        resp.setSize(size);
        resp.setTotal(total);
        resp.setRecords(records);
        return resp;
    }
}


