package com.bht.ludonova.dto.rawg;

import lombok.Data;
import java.util.List;

@Data
public class RawgSearchResponseDTO {
    private Integer count;
    private String next;
    private String previous;
    private List<RawgGameDTO> results;
}