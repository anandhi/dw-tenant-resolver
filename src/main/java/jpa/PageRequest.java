package jpa;

/**
 * Created by anupama.agarwal on 09/01/17.
 */
import com.google.common.collect.Lists;

import java.util.List;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class PageRequest {

    int pageNumber = 0;
    int pageSize = 10;
    private List<String> orderBy = Lists.newArrayList();

    public int getOffset() {
        return pageNumber * pageSize;
    }
}
