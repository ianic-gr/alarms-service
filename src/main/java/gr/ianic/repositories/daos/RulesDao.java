package gr.ianic.repositories.daos;


import com.datastax.oss.driver.api.mapper.annotations.*;
import gr.ianic.model.rules.Rule;

import java.util.List;


@Dao
public interface RulesDao {
    @Insert
    void instert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);

    @Query("SELECT * FROM rules WHERE type = :type and tenant = :tenant")
    List<Rule> getByTenantAndType(String tenant, String type);

}
