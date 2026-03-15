package com.sky.service.user;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
