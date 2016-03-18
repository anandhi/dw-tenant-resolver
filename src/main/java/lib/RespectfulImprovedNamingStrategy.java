package lib;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Created by anupama.agarwal on 18/03/16.
 */

public class RespectfulImprovedNamingStrategy extends ImprovedNamingStrategy {
    public static final RespectfulImprovedNamingStrategy INSTANCE = new RespectfulImprovedNamingStrategy();

    @Override
    public String columnName(String columnName) {
        return columnName;
    }

    @Override
    public String tableName(String tableName) {
        return tableName;
    }

}