package gr.ianic.repositories;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import gr.ianic.repositories.daos.RulesDao;

/**
 * A Cassandra DataStax Mapper interface for creating DAO (Data Access Object) instances.
 * This interface is annotated with {@link Mapper} to enable the generation of the necessary
 * boilerplate code for interacting with the Cassandra database.
 */
@Mapper
public interface DaoMapper {

    /**
     * Factory method to create an instance of {@link RulesDao}.
     * This method is annotated with {@link DaoFactory} to indicate that it produces
     * a DAO instance for the `rules` table.
     *
     * @return An instance of {@link RulesDao} for interacting with the `rules` table.
     */
    @DaoFactory
    RulesDao rulesDao();
}