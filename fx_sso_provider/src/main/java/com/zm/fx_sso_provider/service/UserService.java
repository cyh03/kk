package com.zm.fx_sso_provider.service;

import com.zm.fx_dao_common.bean.User;
import com.zm.fx_dao_common.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Describle This Class Is
 * @Author ZengMin
 * @Date 2018/9/16 20:22
 */
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${MSGPREFIX}")
    private String MSGPREFIX;


    public Map<String,Object> checkUsername(String usernmae) {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> result = new HashMap<>();
        map.put("username",usernmae);
        List<User> users = userMapper.selectByMap(map);
        if(users.size()>0){
            result.put("code","500");
            result.put("msg","用户名已经存在！");
        }else{
            result.put("code","200");
            result.put("msg","用户名可用！");
        }
        return result;
    }

    public Map<String,Object> regUser(com.zm.fx_util_common.bean.User user) {
        Map<String,Object> map = new HashMap<>();
        //先检查验证码是否正确
        ValueOperations valueOperations =  redisTemplate.opsForValue();
        Object code = valueOperations.get(MSGPREFIX + user.getPhone());
        if(code == null){
            map.put("code","500");
            map.put("msg","验证码失效！");
            return map;
        }
        System.out.println("手机验证码：" + code.toString());
        if(user.getRegcode().equals(code.toString())){
            //验证码正确
            int i = userMapper.insertSelective(this.convert(user));
            if(i > 0){
                map.put("code","200");
                map.put("msg","注册成功！");
                //清空redis中的手机号验证码
                redisTemplate.delete(MSGPREFIX + user.getPhone());
            }else{
                map.put("code","500");
                map.put("msg","注册失败！");
            }
        }else{
            map.put("code","500");
            map.put("msg","验证码失效或不正确！");
        }
        return map;
    }

    private User convert(com.zm.fx_util_common.bean.User user){
        User user1 = new User();
        user1.setUsername(user.getUsername());
        user1.setPassword(user.getPassword());
        user1.setId(user.getId());
        user1.setAddress(user.getAddress());
        user1.setEmail(user.getEmail());
        user1.setEmailispass(user.getEmailispass());
        user1.setPhone(user.getPhone());
        user1.setPhoneispass(user.getPhoneispass());
        user1.setRealname(user.getRealname());
        return user1;
    }

}