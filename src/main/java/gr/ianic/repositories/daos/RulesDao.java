package gr.ianic.repositories.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import gr.ianic.model.rules.Rule;

/**
 * Data Access Object (DAO) for interacting with the `rules` table in Cassandra.
 * This interface provides methods for inserting, updating, deleting, and querying rules.
 */
@Dao
public interface RulesDao {

    /**
     * Inserts a new rule into the `rules` table.
     *
     * @param rule The rule to insert.
     */
    @Insert
    void insert(Rule rule);

    /**
     * Updates an existing rule in the `rules` table.
     *
     * @param rule The rule to update.
     */
    @Update
    void update(Rule rule);

    /**
     * Deletes a rule from the `rules` table.
     *
     * @param rule The rule to delete.
     */
    @Delete
    void delete(Rule rule);

    /**
     * Retrieves all rules for a specific tenant and mode.
     *
     * @param tenant The tenant identifier.
     * @param mode   The mode of the rules (e.g., "stream").
     * @return A {@link PagingIterable} containing the rules matching the tenant and mode.
     */
    @Query("SELECT * FROM rules WHERE mode = :mode and tenant = :tenant")
    PagingIterable<Rule> getByTenantAndMode(String tenant, String mode);

    /**
     * Retrieves all rules for a specific mode.
     *
     * @param mode The mode of the rules (e.g., "stream").
     * @return A {@link PagingIterable} containing the rules matching the mode.
     */
    @Query("SELECT * FROM rules WHERE mode = :mode ALLOW FILTERING")
    PagingIterable<Rule> getByMode(String mode);
}