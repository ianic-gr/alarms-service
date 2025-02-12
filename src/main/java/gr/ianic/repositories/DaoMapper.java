package gr.ianic.repositories;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import gr.ianic.repositories.daos.RulesDao;

@Mapper
public interface DaoMapper {

    @DaoFactory
    RulesDao rulesDao();
}
