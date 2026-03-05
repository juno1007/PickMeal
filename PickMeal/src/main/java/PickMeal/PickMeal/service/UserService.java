package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Food;
import PickMeal.PickMeal.domain.User;
import PickMeal.PickMeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // 서비스 빈 등록
@RequiredArgsConstructor // final 필드 생성자 주입
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용
public class UserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional // 쓰기 작업이므로 트랜잭션 별도 지정
    public void signUp(User user) {
        // 1. 아이디 형식 검증 (5~20자, 영문 소문자 및 숫자)
        if (user.getSocialId() == null && !user.getId().matches("^[a-z0-9]{5,20}$")) {
            throw new IllegalArgumentException("아이디는 5~20자의 영문 소문자 및 숫자여야 합니다.");
        }

        // 2. 아이디 중복 가입 체크
        validateDuplicateUser(user.getId());

        // 3. 이메일 중복 체크 및 가입 경로 확인
        User existingUser = userMapper.findByEmail(user.getEmail());
        if (existingUser != null) {
            if (existingUser.getSocialLoginSite() != null && !existingUser.getSocialLoginSite().isEmpty()) {
                throw new IllegalStateException("이미 " + existingUser.getSocialLoginSite() + " 계정으로 가입된 이메일입니다.");
            } else {
                throw new IllegalStateException("이미 가입된 이메일입니다. 아이디 찾기를 이용해주세요.");
            }
        }

        // 4. 비밀번호 복잡도 및 암호화 설정
        if (user.getSocialId() != null && (user.getPassword() == null || user.getPassword().isEmpty())) {
            // 소셜 가입자용 임시 비밀번호 생성
            String tempPassword = java.util.UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(tempPassword));
        } else {
            // 일반 가입자: 비밀번호 복잡도 검사 (영문+숫자+특수문자 포함 8자 이상)
            String pwPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
            if (!user.getPassword().matches(pwPattern)) {
                throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword())); // 비번 암호화
        }

        // [추가] 5. 전화번호 하이픈 보정 및 형식 검증
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            // 숫자 이외의 모든 문자 제거 (01011112222 추출)
            String cleanNumber = user.getPhoneNumber().replaceAll("[^0-9]", "");

            // 11자리(010) 또는 10자리(지역번호 등)일 때 하이픈 삽입
            if (cleanNumber.length() == 11) {
                user.setPhoneNumber(cleanNumber.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3"));
            } else if (cleanNumber.length() == 10) {
                user.setPhoneNumber(cleanNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3"));
            } else {
                // 형식이 너무 다르면 예외를 던져서 가입 차단
                throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
            }
        } else {
            throw new IllegalArgumentException("전화번호는 필수 입력 항목입니다.");
        }

        user.setRole("ROLE_USER");
        user.setStatus("ACTIVE");
        userMapper.save(user); // DB 저장
    }

    public boolean isIdDuplicate(String id) {
        // 1. 유저 테이블에서 아이디 존재 여부 확인
        boolean existsInUser = userMapper.findById(id) != null;

        return existsInUser;
    }

    /**
     * 닉네임 중복 확인
     *
     * @param nickname 검사할 닉네임
     * @return 중복이면 true, 사용 가능하면 false
     */
    public boolean existsByNickname(String nickname) {
        int count = userMapper.countByNickname(nickname);
        return count > 0; // 0보다 크면 이미 존재하는 닉네임입니다.
    }

    /**
     * 이메일로 사용자 찾기
     */
    public User findByEmail(String email) {
        // [비유] 매퍼라는 일꾼에게 이메일 장부를 뒤져서 주인을 찾아오라고 시킵니다.
        return userMapper.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userMapper.findById(loginId);
        if (user == null) throw new UsernameNotFoundException("사용자 없음");

        // 1. 권한 처리
        String roleName = user.getRole().replace("ROLE_", "");
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_" + roleName);

        // 2. CustomUserDetails 가방에 ID와 실명을 각각 제자리에 담아줌
        return new CustomUserDetails(
                user.getId(),       // 가방의 ID 칸에는 실제 ID(admin)를 넣음
                user.getPassword(),
                authorities,
                user.getName()      // 가방의 Name 칸에는 실명(관리자)을 넣음
        );
    }

    private void validateDuplicateUser(String id) {
        if (userMapper.findById(id) != null) {
            throw new IllegalStateException("이미 존재하는 아이디입니다."); // 예외 발생
        }
    }

    public User findById(String id) {
        return userMapper.findById(id); // 아이디로 유저 검색
    }

    /**
     * 사용자 아이디 마스킹 공통 로직
     * @param user 마스킹할 유저 객체
     * @return 마스킹 처리된 디스플레이용 아이디
     */
    /**
     * 사용자 아이디 마스킹 공통 로직
     * 구글(sub), 카카오(id), 네이버(id)의 모든 케이스를 대응합니다.
     */
    public String getMaskedDisplayId(User user) {
        if (user == null) {
            return "Unknown User";
        }

        // 1. 타겟 ID 결정 (소셜 ID가 있으면 그것을, 없으면 일반 ID 사용)
        String targetId = (user.getSocialLoginSite() != null && !user.getSocialLoginSite().isEmpty())
                ? user.getSocialId() : user.getId();

        // 2. 비정상적인 데이터 방어 로직 (null 체크)
        if (targetId == null || targetId.isEmpty()) return "Unknown";

        // 3. [핵심 수정] 마스킹 규칙 적용
        // 최소 5자 이상이므로 복잡한 if-else 없이 앞 3글자만 남기고 고정 마스킹
        // 소셜 ID는 매우 길 수 있으므로 substring 범위를 안전하게 가져갑니다.
        String displayId = targetId.substring(0, 3) + "******";

        // 4. 소셜 로그인인 경우 접두사 추가 (예: GOOGLE_abc****)
        if (user.getSocialLoginSite() != null && !user.getSocialLoginSite().isEmpty()) {
            displayId = user.getSocialLoginSite().toUpperCase() + "_" + displayId;
        }

        return displayId;
    }

    @Transactional
    public void edit(User user, boolean isNewPassword) {
        // 1. 비밀번호 암호화 처리
        if (isNewPassword) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 2. [추가] 전화번호 하이픈 보정 로직
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            // 숫자만 추출
            String cleanNumber = user.getPhoneNumber().replaceAll("[^0-9]", "");

            // 11자리인 경우 다시 010-0000-0000 형식으로 재조립
            if (cleanNumber.length() == 11) {
                String formatted = cleanNumber.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
                user.setPhoneNumber(formatted);
            }
        }

        // 3. 수정된 데이터 DB 업데이트
        userMapper.edit(user);
    }

    @Transactional
    public void remove(Long user_id) {
        userMapper.updateStatus(user_id, "WITHDRAWN"); // 탈퇴 시 상태값만 변경
    }

    public List<Food> getMixedFoods(List<String> types, int round) {
        return userMapper.getMixedFoods(types, round);
    }

    // 랭킹 페이지용 데이터 가져오기
    public List<Food> getTop10Foods() {
        return userMapper.getTop10Foods();
    }

    @Transactional
    public void updateFoodWinCount(Long foodId) {
        // [비유] 일꾼이 실제 장부(Mapper)를 들고 가서 숫자를 하나 올립니다.
        userMapper.updateWinCount(foodId);
    }

    public User findByUser_id(Long user_id) {
        return userMapper.findByUser_id(user_id);
    }
    /**
     * 이메일 즉시 변경
     */
    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        // MyBatis 매퍼를 통해 특정 컬럼만 업데이트하는 전용 메서드를 호출하거나,
        // 기존 edit을 재사용할 수 있도록 로직을 짭니다.
        User user = new User();
        user.setUser_id(userId);
        user.setEmail(newEmail);
        userMapper.updateEmail(user); // 매퍼에 새 메서드 추가 필요
    }

    /**
     * 비밀번호 즉시 변경
     */
    @Transactional
    public void updatePassword(Long userId, String rawPassword) {
        // 암호화하여 매퍼에 전달
        String encodedPassword = passwordEncoder.encode(rawPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    // 아이디 찾기 (마스킹 처리 포함)
    public String findId(String name, String email) {
        // 1. 매퍼를 통해 유저 정보 통째로 가져오기
        User user = userMapper.findUserByNameAndEmail(name, email);

        if (user == null) return null;

        // 2. 소셜 가입자인지 먼저 확인 (가장 우선순위)
        if (user.getSocialLoginSite() != null && !user.getSocialLoginSite().isEmpty()) {
            // "SOCIAL_GOOGLE" 등의 형태로 반환해서 프론트가 알게 함
            return "SOCIAL_" + user.getSocialLoginSite().toUpperCase();
        }

        // 3. 일반 가입자라면 기존 마스킹 로직 실행
        String fullId = user.getId();
        String prefix = fullId.substring(0, 3);
        String stars = "*".repeat(fullId.length() - 3);

        return prefix + stars;
    }

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        String loginId = null;

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            // CustomOAuth2UserService에서 넣은 "db_id"를 꺼냄
            Object dbIdValue = oAuth2User.getAttributes().get("db_id");
            if (dbIdValue != null) {
                loginId = String.valueOf(dbIdValue);
            } else {
                loginId = oAuth2User.getName();
            }
        } else {
            loginId = authentication.getName(); // 일반 로그인 aaa
        }

        if (loginId == null || loginId.isEmpty()) return null;

        try {
            return userMapper.findById(loginId);
        } catch (Exception e) {
            System.out.println(">>> [findById 에러 발생] : " + e.getMessage());
            return null;
        }
    }
}
