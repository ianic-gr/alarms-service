package gr.ianic.repositories;


import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import gr.ianic.model.rules.Rule;


@Dao
public interface RulesDao {
    @Insert
    void instert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);

    @Query("SELECT * FROM rules WHERE type = :type and tenant = :tenant")
    Rule findByTenantAndType(String tenant, String type);
}
