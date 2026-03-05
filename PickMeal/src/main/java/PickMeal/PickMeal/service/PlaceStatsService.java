package PickMeal.PickMeal.service;

import PickMeal.PickMeal.dto.PlaceStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceStatsService {

    private final JdbcTemplate jdbcTemplate;

    // 1. 메인 리스트용 통계 합산 조회 (평균 평점 및 내 좋아요 여부 포함)
    public List<PlaceStatsDto> getStatsByKakaoIds(List<String> placeIds, String userId) {
        if (placeIds == null || placeIds.isEmpty()) return Collections.emptyList();

        List<Long> longIds = placeIds.stream().map(Long::parseLong).collect(Collectors.toList());
        String inParams = String.join(",", Collections.nCopies(longIds.size(), "?"));

        String sql = "SELECT res_id, " +
                "SUM(IFNULL(view_count, 0)) as view_cnt, " +
                "SUM(CASE WHEN is_wish = 1 THEN 1 ELSE 0 END) as wish_cnt, " +
                "COUNT(content) as rev_cnt, " +
                "IFNULL(AVG(CASE WHEN content IS NOT NULL THEN rating END), 0) as avg_rate, " +
                "MAX(CASE WHEN user_id = ? AND is_wish = 1 THEN 1 ELSE 0 END) as my_wish " +
                "FROM place_stats " +
                "WHERE res_id IN (" + inParams + ") " +
                "GROUP BY res_id";

        // 파라미터 배열 준비 (첫 번째는 userId, 나머지는 placeIds)
        Object[] params = new Object[longIds.size() + 1];
        params[0] = (userId != null) ? userId : "";
        for (int i = 0; i < longIds.size(); i++) {
            params[i + 1] = longIds.get(i);
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PlaceStatsDto dto = new PlaceStatsDto();
            dto.setKakaoPlaceId(String.valueOf(rs.getLong("res_id")));
            dto.setViewCount(rs.getInt("view_cnt"));
            dto.setHeartCount(rs.getInt("wish_cnt"));
            dto.setReviewCount(rs.getInt("rev_cnt"));
            // 평점 소수점 한자리 반올림
            dto.setAvgRating(Math.round(rs.getDouble("avg_rate") * 10) / 10.0);
            dto.setLiked(rs.getInt("my_wish") == 1);
            return dto;
        }, params);
    }

    public List<PlaceStatsDto> getReviews(String kakaoPlaceId) {
        Long resId = Long.parseLong(kakaoPlaceId);
        // [수정] SQL문에 rating 컬럼을 추가해야 합니다.
        String sql = "SELECT p.review_id, p.user_id, u.nickname, p.content, p.rating " +
                "FROM place_stats p " +
                "LEFT JOIN user u ON p.user_id = u.id " + // 사용자 테이블 JOIN
                "WHERE p.res_id = ? AND p.content IS NOT NULL " +
                "ORDER BY p.created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PlaceStatsDto dto = new PlaceStatsDto();
            dto.setReviewId(rs.getLong("review_id"));
            dto.setUserId(rs.getString("user_id"));
            String nickname = rs.getString("nickname");
            dto.setNickname(nickname != null ? nickname : rs.getString("user_id"));
            dto.setContent(rs.getString("content"));
            // [추가] DB에서 가져온 rating 값을 DTO에 담아줘야 프론트에서 읽을 수 있습니다.
            dto.setRating(rs.getInt("rating"));
            return dto;
        }, resId);
    }

    // 3. 조회수 증가
    public void addViewLog(String kakaoPlaceId, String userId) {
        Long resId = Long.parseLong(kakaoPlaceId);
        String sql = "INSERT INTO place_stats (res_id, user_id, view_count, is_wish, created_at, updated_at) " +
                "VALUES (?, ?, 1, 0, NOW(), NOW()) " +
                "ON DUPLICATE KEY UPDATE view_count = view_count + 1, updated_at = NOW()";
        jdbcTemplate.update(sql, resId, userId);
    }

    // 4. 찜 토글
    public boolean toggleHeart(String kakaoPlaceId, String userId, String placeName, String categoryName, String addressName) {
        Long resId = Long.parseLong(kakaoPlaceId);
        String checkSql = "SELECT review_id FROM place_stats WHERE res_id = ? AND user_id = ? LIMIT 1";
        List<Long> ids = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getLong("review_id"), resId, userId);

        boolean isNowWished = false;

        if (ids.isEmpty()) {
            String insertSql = "INSERT INTO place_stats (res_id, user_id, view_count, is_wish, created_at, updated_at, place_name, category, address) " +
                    "VALUES (?, ?, 0, 1, NOW(), NOW(), ?, ?, ?)";
            jdbcTemplate.update(insertSql, resId, userId, placeName, categoryName, addressName);
            isNowWished = true;
        } else {
            String updateSql = "UPDATE place_stats SET is_wish = IF(is_wish = 1, 0, 1), updated_at = NOW() " +
                    "WHERE review_id = ?";
            jdbcTemplate.update(updateSql, ids.get(0));
            String statusSql = "SELECT is_wish FROM place_stats WHERE review_id = ?";
            Integer currentWish = jdbcTemplate.queryForObject(statusSql, Integer.class, ids.get(0));
            isNowWished = (currentWish != null && currentWish == 1);
        }

        return isNowWished;
    }

    // 5. 신규 댓글 추가 (평점 포함)
    public void addReview(String kakaoPlaceId, String userId, String content, int rating, String placeName, String categoryName, String addressName) {
        Long resId = Long.parseLong(kakaoPlaceId);
        String sql = "INSERT INTO place_stats (res_id, user_id, content, rating, category, address, place_name, created_at, updated_at ) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        jdbcTemplate.update(sql, resId, userId, content, rating, categoryName, addressName, placeName);
    }

    // 6. 댓글 수정
    public void updateReview(Long reviewId, String content) {
        String findIdSql = "SELECT res_id FROM place_stats WHERE review_id = ?";
        Long resId = jdbcTemplate.queryForObject(findIdSql, Long.class, reviewId);

        String updateSql = "UPDATE place_stats SET content = ?, updated_at = NOW() WHERE review_id = ?";
        jdbcTemplate.update(updateSql, content, reviewId);

    }

    // 7. 댓글 삭제
    public void deleteReview(Long reviewId) {
        String findIdSql = "SELECT res_id FROM place_stats WHERE review_id = ?";
        Long resId = jdbcTemplate.queryForObject(findIdSql, Long.class, reviewId);

        String deleteSql = "DELETE FROM place_stats WHERE review_id = ?";
        jdbcTemplate.update(deleteSql, reviewId);

    }

    // 8. 댓글 수 동기화
    private void updateReviewCount(Long resId) {
        String countSql = "SELECT COUNT(*) FROM place_stats WHERE res_id = ? AND content IS NOT NULL";
        Integer currentCount = jdbcTemplate.queryForObject(countSql, Integer.class, resId);

        String syncSql = "UPDATE place_stats SET review_count = ? WHERE res_id = ?";
        jdbcTemplate.update(syncSql, currentCount, resId);
    }

    // 댓글 수정 (내용 + 별점 업데이트 및 본인 확인)
    public boolean updateReview(Long reviewId, String userId, String content, int rating, String placeName) {
        // 1. 작성자 본인 확인
        String checkSql = "SELECT user_id FROM place_stats WHERE review_id = ?";
        String authorId = jdbcTemplate.queryForObject(checkSql, String.class, reviewId);

        if (!authorId.equals(userId)) {
            return false; // 본인이 아니면 실패 반환
        }

        // 2. 내용과 별점 함께 수정
        String updateSql = "UPDATE place_stats SET content = ?, rating = ?, updated_at = NOW(), place_name = ? WHERE review_id = ?";
        jdbcTemplate.update(updateSql, content, rating, placeName, reviewId);

        // 3. 해당 가게의 평균 평점 및 댓글 수 동기화
        String findResIdSql = "SELECT res_id FROM place_stats WHERE review_id = ?";
        Long resId = jdbcTemplate.queryForObject(findResIdSql, Long.class, reviewId);


        return true;
    }

    public List<PlaceStatsDto> getPopularPlace() {

        String sql = "SELECT CAST(res_id AS CHAR) as kakaoPlaceId, " +
                "MAX(place_name) as placeName, " +
                "MAX(category) as category, " +
                "MAX(address) as address, " +
                "SUM(CASE WHEN is_wish = 1 THEN 1 ELSE 0 END) as heartCount, " +
                "SUM(IFNULL(view_count, 0)) as viewCount, " +
                "COUNT(content) as reviewCount, " +
                "IFNULL(AVG(rating), 0) as avgRating " +
                "FROM place_stats " +
                "GROUP BY res_id " +
                "ORDER BY heartCount DESC, viewCount DESC " +
                "LIMIT 10";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PlaceStatsDto dto = new PlaceStatsDto();
            dto.setKakaoPlaceId(rs.getString("kakaoPlaceId"));
            dto.setPlaceName(rs.getString("placeName"));
            dto.setCategory(rs.getString("category"));
            dto.setAddress(rs.getString("address"));
            dto.setHeartCount(rs.getInt("heartCount"));
            dto.setViewCount(rs.getInt("viewCount"));
            dto.setReviewCount(rs.getInt("reviewCount"));
            return dto;
        });
    }

    public PlaceStatsDto getPlaceStatByKakaoIds(String kakaoPlaceId) {
        Long resId = Long.parseLong(kakaoPlaceId);

        // 1. 단일 식당의 찜, 리뷰, 조회수 통계를 하나로 합쳐서(GROUP BY) 가져오는 쿼리
        String sql = "SELECT " +
                "CAST(res_id AS CHAR) as kakaoPlaceId, " +
                "MAX(CASE WHEN place_name = '이름 없음' THEN NULL ELSE place_name END) as placeName, " +
                "MAX(category) as category, " +
                "MAX(address) as address, " +
                "SUM(IFNULL(view_count, 0)) as viewCount, " +
                "SUM(CASE WHEN is_wish = 1 THEN 1 ELSE 0 END) as heartCount, " +
                "COUNT(content) as reviewCount, " +
                "IFNULL(AVG(CASE WHEN content IS NOT NULL THEN rating END), 0) as avgRating " +
                "FROM place_stats " +
                "WHERE res_id = ? " +
                "GROUP BY res_id";

        try {
            // 2. update() 대신 queryForObject()를 사용해서 DTO로 변환하여 리턴!
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                PlaceStatsDto dto = new PlaceStatsDto();
                dto.setKakaoPlaceId(rs.getString("kakaoPlaceId"));
                dto.setPlaceName(rs.getString("placeName"));
                dto.setCategory(rs.getString("category"));
                dto.setAddress(rs.getString("address"));
                dto.setViewCount(rs.getInt("viewCount"));
                dto.setHeartCount(rs.getInt("heartCount"));
                dto.setReviewCount(rs.getInt("reviewCount"));
                dto.setAvgRating(Math.round(rs.getDouble("avgRating") * 10) / 10.0);
                return dto;
            }, resId); // 👈 물음표(?) 자리에 들어갈 resId를 여기에 넘겨줍니다.

        } catch (EmptyResultDataAccessException e) {
            // 3. 만약 DB에 아직 아무도 찜/리뷰를 안 남겨서 데이터가 아예 없다면?
            // 에러를 내지 않고 텅 빈 DTO를 반환하게 안전장치를 걸어줍니다.
            return null;
        }
    }

    public int countHeart(String kakaoPlaceId) {
        Long resId = Long.parseLong(kakaoPlaceId);


        String countSql = "SELECT SUM(CASE WHEN is_wish = 1 THEN 1 ELSE 0 END) FROM place_stats WHERE res_id = ?";
        int totalHearts = jdbcTemplate.queryForObject(countSql, Integer.class, resId);

        return totalHearts;
    }

    public boolean isCurrentUserHearted(String userId, long resId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM place_stats WHERE user_id = ? AND res_id = ? AND is_wish = 1";

        Integer wishCount = jdbcTemplate.queryForObject(sql, Integer.class, userId, resId);

        return wishCount != null && wishCount > 0;
    }
}