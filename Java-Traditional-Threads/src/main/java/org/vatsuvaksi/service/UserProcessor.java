package org.vatsuvaksi.service;


import org.springframework.transaction.annotation.Transactional;
import org.vatsuvaksi.components.ApplicationContextHolder;
import org.vatsuvaksi.model.User;
import org.vatsuvaksi.repository.UserRepository;

import java.util.function.Supplier;

public class UserProcessor implements Supplier<Boolean> {
    private final User user;
    private UserRepository repository;

    public UserProcessor(User user) {
        this.user = user;
        this.repository = ApplicationContextHolder.getContext().getBean(UserRepository.class);
    }

    @Transactional
    @Override
    public Boolean get() {
        try {
            this.user.setName(this.user.getName() +
                    this.user.getEmail().substring(this.user.getName().length() - 1));

           this.repository.save(this.user);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
