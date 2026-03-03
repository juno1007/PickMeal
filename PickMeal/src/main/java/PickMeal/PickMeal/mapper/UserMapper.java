package PickMeal.PickMeal.mapper;

import PickMeal.PickMeal.domain.Food;
import PickMeal.PickMeal.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(String id);

    /**
     * 이메일로 유저 정보 조회
     * @param email 찾을 이메일 주소
     * @return 일치하는 User 객체 (없으면 null)
     */
    User findByEmail(@Param("email") String email);

    int countByEmail(@Param("email") String email);

    void save(User user);

    void edit(User user);

    void updateEmail(User user);

    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    void updateStatus(@Param("user_id") Long user_id, @Param("status") String status);

    int countByNickname(String nickname);

    // ★ 추가: 음식 번호(id)를 주면 DB에서 우승 횟수를 1 증가시키는 주문서입니다.
    void incrementFoodWinCount(Long foodId);

    // [월드컵용] 여러 카테고리를 섞어서 랜덤으로 가져오는 메서드
    List<Food> getMixedFoods(@Param("types") List<String> types, @Param("round") int round);

    // ★ 추가: 우승 횟수가 높은 순서대로 10개를 가져오는 주문서입니다.
    List<Food> getTop10Foods();

    void updateWinCount(Long id);

    User findByUser_id(Long userId);

    /**
     * 이름과 이메일로 아이디 조회 (아이디 찾기용)
     */
    User findUserByNameAndEmail(@Param("name") String name, @Param("email") String email);;

    /**
     * 아이디, 이메일로 일치하는 회원 확인 (비밀번호 찾기 전 본인 인증용)
     */
    User findUserByIdAndEmail(@Param("id") String id, @Param("email") String email);

    String getLikedMenuString(Long userId);    // 선호 음식 가져오기

    String getDislikedMenuString(Long userId); // 비선호 음식 가져오기
}
