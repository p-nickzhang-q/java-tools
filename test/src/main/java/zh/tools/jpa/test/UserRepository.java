package zh.tools.jpa.test;

import org.springframework.stereotype.Repository;
import zh.tools.jpa.BaseRepositorySupport;

@Repository
public interface UserRepository extends BaseRepositorySupport<User, Long> {

}