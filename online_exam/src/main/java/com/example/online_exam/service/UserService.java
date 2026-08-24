package com.example.online_exam.service;

import com.example.online_exam.entity.User;
import com.example.online_exam.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 注册
    public void register(User user) {
        userRepository.save(user);
    }

    // 根据用户名查询
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    // 登录校验
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
}

}
