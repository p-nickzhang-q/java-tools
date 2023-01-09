package zh.tools.test.jpa;

import org.springframework.stereotype.Repository;
import zh.tools.jpa.BaseRepositorySupport;

@Repository
public interface UserRepository extends BaseRepositorySupport<User, Long> {

}