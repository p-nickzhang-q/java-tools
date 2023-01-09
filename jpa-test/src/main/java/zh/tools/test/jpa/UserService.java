package zh.tools.test.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zh.tools.jpa.BaseFilterService;
import zh.tools.jpa.BaseRepositorySupport;

@Service
public class UserService extends BaseFilterService<User, Long> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public BaseRepositorySupport<User, Long> repository() {
        return userRepository;
    }
}