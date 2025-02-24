package gr.ianic.repositories.daos;


import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import gr.ianic.model.rules.Rule;


@Dao
public interface RulesDao {
    @Insert
    void insert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);

    @Query("SELECT * FROM rules WHERE mode = :mode and tenant = :tenant")
    PagingIterable<Rule> getByTenantAndMode(String tenant, String mode);

    @Query("SELECT * FROM rules WHERE mode = :mode ALLOW FILTERING")
    PagingIterable<Rule> getByMode(String mode);
}
