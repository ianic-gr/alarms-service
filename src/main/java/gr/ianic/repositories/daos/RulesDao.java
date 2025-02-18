package gr.ianic.repositories.daos;


import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import gr.ianic.model.rules.Rule;


@Dao
public interface RulesDao {
    @Insert
    void instert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);

    @Query("SELECT * FROM rules WHERE type = :type and tenant = :tenant and source = :source")
    PagingIterable<Rule> getByTenantTypeAndSource(String tenant, String type, String source);

    @Query("SELECT * FROM rules WHERE type = :type ALLOW FILTERING")
    PagingIterable<Rule> getByType(String type);
}
