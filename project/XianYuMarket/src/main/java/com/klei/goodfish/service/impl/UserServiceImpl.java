package com.klei.goodfish.service.impl;

import com.klei.goodfish.dto.*;
import com.klei.goodfish.entity.Favorite;
import com.klei.goodfish.entity.Follow;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.FavoriteService;
import com.klei.goodfish.service.FollowService;
import com.klei.goodfish.service.GoodService;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.vo.*;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author klei
 */
public class UserServiceImpl implements UserService {

    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);

    private FavoriteService favoriteService = new FavoriteServiceImpl();
    private FollowService followService = new FollowServiceImpl();
    private GoodService goodService = new GoodServiceImpl();

    @Override
    public UserRegisterVO register(UserRegisterDTO dto) {
        try {
            System.out.println("注册开始: " + dto.getUserName());

            String error = dto.validate();
            if (error != null) {
                return UserRegisterVO.fail(error);
            }

            User user = userMapper.findByName(dto.getUserName());
            if (user != null) {
                return UserRegisterVO.fail("用户名重复！");
            }

            user = new User();
            user.setUserName(dto.getUserName());
            user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
            user.setAvatar("");
            user.setRole(dto.getRole());
            user.setStatus(1);
            user.setWallet(BigDecimal.ZERO);
            user.setCreateTime(LocalDateTime.now());

            System.out.println("准备插入数据...");
            userMapper.insert(
                    user.getUserName(),
                    user.getPassword(),
                    user.getRole(),
                    user.getStatus(),
                    user.getWallet(),
                    user.getCreateTime(),
                    user.getAvatar()
            );
            System.out.println("插入成功");

            User savedUser = userMapper.findByName(dto.getUserName());
            return UserRegisterVO.success(savedUser);

        } catch (Exception e) {
            e.printStackTrace();  // 关键：打印红色错误堆栈
            return UserRegisterVO.fail("系统错误: " + e.getMessage());
        }
    }

    //登录
    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        String userName = dto.getUserName();
        User user = userMapper.findByName(userName);
        if (user == null) {
            return UserLoginVO.fail("用户名或密码错误");
        }
        String password = dto.getPassword();
        boolean check = BCrypt.checkpw(password, user.getPassword());
        if (!check) {
            return UserLoginVO.fail("用户名或密码错误");
        }
        return UserLoginVO.success(user);
    }

    @Override
    //查看个人信息 - 修复：添加头像返回
    public UserProfileVO getUser (Integer userId) {
        User user = userMapper.findById(userId);

        if (user == null) {
            return null;
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setAvatar(user.getAvatar());  // 修复：添加头像赋值
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setWallet(user.getWallet());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    //查看个人收藏（关键修复：增加异常处理，避免单个商品错误导致整个列表失败）
    public UserFavoriteVO getUserFavorite (Integer userId) {
        List<Favorite> favoriteList = favoriteService.getUserFavorites(userId);

        // 即使为空也返回 VO 对象，不要返回 null
        UserFavoriteVO vo = new UserFavoriteVO();
        vo.setUserId(userId);
        List<UserFavoriteDTO> dtoList = new ArrayList<>();

        if (favoriteList == null || favoriteList.isEmpty()) {
            vo.setFavorites(dtoList); // 空列表
            return vo;
        }

        for (Favorite favorite : favoriteList) {
            UserFavoriteDTO dto = new UserFavoriteDTO();
            try {
                // 查商品详情，如果商品被删除或查询失败，跳过或标记为空
                Good good = goodService.getGoodDetail(favorite.getGoodId());
                dto.setGood(good);
            } catch (Exception e) {
                // 商品查询失败（如已删除），设置空对象或占位符，不中断整个列表
                System.err.println("查询收藏商品失败, goodId=" + favorite.getGoodId() + ", error=" + e.getMessage());
                Good emptyGood = new Good();
                emptyGood.setId(favorite.getGoodId());
                emptyGood.setGoodName("商品已下架或不存在");
                dto.setGood(emptyGood);
            }
            dto.setFavoriteTime(favorite.getCreateTime());
            dtoList.add(dto);
        }

        vo.setFavorites(dtoList);
        return vo;
    }

    //查看我的关注
    @Override
    public UserFollowVO getUserFollow (Integer userId) {
        List<Follow> followList = followService.getUserFollows(userId);

        UserFollowVO vo = new UserFollowVO();
        vo.setUserId(userId);
        List<UserFollowDTO> dtoList = new ArrayList<>();

        if (followList == null || followList.isEmpty()) {
            vo.setFollowings(dtoList);
            return vo;
        }

        for (Follow follow : followList) {
            UserFollowDTO dto = new UserFollowDTO();
            try {
                User user = userMapper.findById(follow.getFollowingId());
                dto.setFollowingUser(user);
            } catch (Exception e) {
                System.err.println("查询关注用户失败, userId=" + follow.getFollowingId());
                User emptyUser = new User();
                emptyUser.setId(follow.getFollowingId());
                emptyUser.setUserName("用户不存在");
                dto.setFollowingUser(emptyUser);
            }
            dto.setFollowTime(follow.getCreateTime());
            dtoList.add(dto);
        }
        vo.setFollowings(dtoList);
        return vo;
    }

    //查看我发布的商品
    @Override
    public UserGoodVO getUserGood (Integer userId) {

        List<Good> goodList = goodService.getGoodsBySellerId(userId);

        UserGoodVO vo = new UserGoodVO();
        vo.setUserId(userId);
        List<UserGoodDTO> dtoList = new ArrayList<>();

        if (goodList == null || goodList.isEmpty()) {
            vo.setGoods(dtoList);
            return vo;
        }

        for (Good good : goodList) {
            UserGoodDTO dto = new UserGoodDTO();
            dto.setGood(good);
            dto.setReleaseTime(good.getCreateTime());
            dtoList.add(dto);
        }
        vo.setGoods(dtoList);
        return vo;
    }

    @Override
    public boolean updateAvatar(Integer userId, String avatarUrl) {
        if (userId == null || avatarUrl == null) {
            throw new BusinessException(400, "参数不能为空");
        }

        // 检查用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新头像
        userMapper.updateAvatar(avatarUrl, userId);
        return true;
    }
}