package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.User;
import com.zhongjia.web.vo.UserVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserVO toVO(User user);
    List<UserVO> toVOList(List<User> users);
}


