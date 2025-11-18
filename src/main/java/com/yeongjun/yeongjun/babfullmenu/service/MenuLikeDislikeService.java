package com.yeongjun.yeongjun.babfullmenu.service;

import com.yeongjun.yeongjun.babfullmenu.model.MenuLikeDislike;
import com.yeongjun.yeongjun.babfullmenu.model.MenuVote;
import com.yeongjun.yeongjun.babfullmenu.repository.MenuLikeDislikeDAO;
import com.yeongjun.yeongjun.babfullmenu.repository.MenuVoteDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuLikeDislikeService {

    private final MenuLikeDislikeDAO menuLikeDislikeDAO;
    private final MenuVoteDAO menuVoteDAO;

    /**
     * 메뉴명을 정규화합니다 (한글만 남김)
     * @param menuName 원본 메뉴명
     * @return 정규화된 메뉴명
     */
    public String normalizeMenuName(String menuName) {
        if (menuName == null || menuName.isEmpty()) {
            return "";
        }
        // 한글만 남기고 나머지 제거
        String normalized = menuName.replaceAll("[^가-힣]", "");
        log.debug("정규화 전: {} (바이트: {}), 정규화 후: {} (바이트: {})", 
            menuName, java.util.Arrays.toString(menuName.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            normalized, java.util.Arrays.toString(normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return normalized;
    }

    /**
     * 좋아요/싫어요 투표를 처리합니다
     * @param menuName 원본 메뉴명
     * @param voteType "LIKE" or "DISLIKE"
     * @param sessionId 세션 ID
     * @return 투표 결과 (likeCount, dislikeCount, currentVote)
     */
    @Transactional
    public Map<String, Object> vote(String menuName, String voteType, String sessionId) {
        String normalizedMenuName = normalizeMenuName(menuName);
        log.debug("투표 처리 - 원본 메뉴명: {}, 정규화된 메뉴명: {}, 투표 타입: {}", menuName, normalizedMenuName, voteType);
        
        if (normalizedMenuName.isEmpty()) {
            throw new IllegalArgumentException("메뉴명이 비어있습니다.");
        }

        // 기존 투표 확인
        MenuVote existingVote = menuVoteDAO.selectByNormalizedMenuNameAndSessionId(normalizedMenuName, sessionId);
        
        // 좋아요/싫어요 집계 조회 또는 생성
        MenuLikeDislike likeDislike = menuLikeDislikeDAO.selectByNormalizedMenuName(normalizedMenuName);
        log.debug("투표 처리 - 집계 조회 결과: {}", likeDislike);
        if (likeDislike == null) {
            log.debug("새로운 집계 생성: {}", normalizedMenuName);
            likeDislike = new MenuLikeDislike();
            likeDislike.setNormalizedMenuName(normalizedMenuName);
            likeDislike.setLikeCount(0);
            likeDislike.setDislikeCount(0);
            menuLikeDislikeDAO.insertMenuLikeDislike(likeDislike);
            log.debug("집계 생성 완료");
        } else {
            // null 체크 및 기본값 설정
            if (likeDislike.getLikeCount() == null) {
                likeDislike.setLikeCount(0);
            }
            if (likeDislike.getDislikeCount() == null) {
                likeDislike.setDislikeCount(0);
            }
            log.debug("기존 집계 사용 - 좋아요: {}, 싫어요: {}", likeDislike.getLikeCount(), likeDislike.getDislikeCount());
        }

        String currentVote = null;
        String existingVoteType = null;
        if (existingVote != null) {
            existingVoteType = existingVote.getVoteType();
            currentVote = existingVoteType;
        }

        // 투표 처리 로직
        if (existingVote == null || existingVoteType == null) {
            // 새로운 투표 또는 voteType이 null인 경우
            // 기존 투표가 있으면 삭제
            if (existingVote != null) {
                menuVoteDAO.deleteMenuVote(existingVote);
                // 삭제 후 다시 확인 (동시성 문제 방지)
                existingVote = menuVoteDAO.selectByNormalizedMenuNameAndSessionId(normalizedMenuName, sessionId);
            }
            
            // 삭제 후에도 여전히 레코드가 없으면 삽입
            if (existingVote == null) {
                MenuVote newVote = new MenuVote();
                newVote.setNormalizedMenuName(normalizedMenuName);
                newVote.setVoteType(voteType);
                newVote.setSessionId(sessionId);
                newVote.setCreatedAt(LocalDateTime.now());
                try {
                    menuVoteDAO.insertMenuVote(newVote);
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    // 동시성 문제로 인한 중복 삽입 시, 기존 레코드를 다시 조회
                    existingVote = menuVoteDAO.selectByNormalizedMenuNameAndSessionId(normalizedMenuName, sessionId);
                    if (existingVote != null) {
                        // 기존 레코드가 있으면 기존 로직으로 처리
                        existingVoteType = existingVote.getVoteType();
                        if (voteType.equals(existingVoteType)) {
                            // 같은 투표 취소
                            menuVoteDAO.deleteMenuVote(existingVote);
                            if ("LIKE".equals(voteType)) {
                                likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                                menuLikeDislikeDAO.updateLikeCount(likeDislike);
                            } else {
                                likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                            }
                            currentVote = null;
                        } else {
                            // 다른 투표로 변경
                            menuVoteDAO.deleteMenuVote(existingVote);
                            if ("LIKE".equals(existingVoteType)) {
                                likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                                menuLikeDislikeDAO.updateLikeCount(likeDislike);
                            } else {
                                likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                            }
                            MenuVote updateVote = new MenuVote();
                            updateVote.setNormalizedMenuName(normalizedMenuName);
                            updateVote.setVoteType(voteType);
                            updateVote.setSessionId(sessionId);
                            updateVote.setCreatedAt(LocalDateTime.now());
                            menuVoteDAO.insertMenuVote(updateVote);
                            if ("LIKE".equals(voteType)) {
                                likeDislike.setLikeCount(likeDislike.getLikeCount() + 1);
                                menuLikeDislikeDAO.updateLikeCount(likeDislike);
                            } else {
                                likeDislike.setDislikeCount(likeDislike.getDislikeCount() + 1);
                                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                            }
                            currentVote = voteType;
                        }
                        // 최신 집계 조회
                        likeDislike = menuLikeDislikeDAO.selectByNormalizedMenuName(normalizedMenuName);
                        
                        // null 체크 및 기본값 설정
                        if (likeDislike != null) {
                            if (likeDislike.getLikeCount() == null) {
                                likeDislike.setLikeCount(0);
                            }
                            if (likeDislike.getDislikeCount() == null) {
                                likeDislike.setDislikeCount(0);
                            }
                        } else {
                            likeDislike = new MenuLikeDislike();
                            likeDislike.setNormalizedMenuName(normalizedMenuName);
                            likeDislike.setLikeCount(0);
                            likeDislike.setDislikeCount(0);
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("likeCount", likeDislike.getLikeCount() != null ? likeDislike.getLikeCount() : 0);
                        result.put("dislikeCount", likeDislike.getDislikeCount() != null ? likeDislike.getDislikeCount() : 0);
                        result.put("currentVote", currentVote);
                        return result;
                    }
                    throw e; // 다른 경우는 예외 재발생
                }
                
                // 집계 업데이트
                if ("LIKE".equals(voteType)) {
                    likeDislike.setLikeCount(likeDislike.getLikeCount() + 1);
                    menuLikeDislikeDAO.updateLikeCount(likeDislike);
                } else {
                    likeDislike.setDislikeCount(likeDislike.getDislikeCount() + 1);
                    menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                }
                currentVote = voteType;
            } else {
                // 삭제 후 다시 레코드가 생긴 경우 (다른 요청이 먼저 처리됨)
                // 기존 로직으로 재처리
                existingVoteType = existingVote.getVoteType();
                if (voteType.equals(existingVoteType)) {
                    // 같은 투표 취소
                    menuVoteDAO.deleteMenuVote(existingVote);
                    if ("LIKE".equals(voteType)) {
                        likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                        menuLikeDislikeDAO.updateLikeCount(likeDislike);
                    } else {
                        likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                        menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                    }
                    currentVote = null;
                } else {
                    // 다른 투표로 변경
                    menuVoteDAO.deleteMenuVote(existingVote);
                    if ("LIKE".equals(existingVoteType)) {
                        likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                        menuLikeDislikeDAO.updateLikeCount(likeDislike);
                    } else {
                        likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                        menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                    }
                    MenuVote updateVote = new MenuVote();
                    updateVote.setNormalizedMenuName(normalizedMenuName);
                    updateVote.setVoteType(voteType);
                    updateVote.setSessionId(sessionId);
                    updateVote.setCreatedAt(LocalDateTime.now());
                    try {
                        menuVoteDAO.insertMenuVote(updateVote);
                    } catch (org.springframework.dao.DuplicateKeyException e) {
                        // 이미 다른 요청이 처리한 경우, 다시 조회
                        existingVote = menuVoteDAO.selectByNormalizedMenuNameAndSessionId(normalizedMenuName, sessionId);
                        if (existingVote != null) {
                            currentVote = existingVote.getVoteType();
                        }
                    }
                    if ("LIKE".equals(voteType)) {
                        likeDislike.setLikeCount(likeDislike.getLikeCount() + 1);
                        menuLikeDislikeDAO.updateLikeCount(likeDislike);
                    } else {
                        likeDislike.setDislikeCount(likeDislike.getDislikeCount() + 1);
                        menuLikeDislikeDAO.updateDislikeCount(likeDislike);
                    }
                    currentVote = voteType;
                }
            }
        } else if (voteType.equals(existingVoteType)) {
            // 같은 투표를 다시 누른 경우 - 취소
            menuVoteDAO.deleteMenuVote(existingVote);

            // 집계 업데이트
            if ("LIKE".equals(voteType)) {
                likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                menuLikeDislikeDAO.updateLikeCount(likeDislike);
            } else {
                likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
            }
            currentVote = null;
        } else {
            // 다른 투표로 변경 (좋아요 -> 싫어요 또는 싫어요 -> 좋아요)
            // 기존 투표 삭제
            menuVoteDAO.deleteMenuVote(existingVote);

            // 기존 투표 집계 감소
            if ("LIKE".equals(existingVoteType)) {
                likeDislike.setLikeCount(Math.max(0, likeDislike.getLikeCount() - 1));
                menuLikeDislikeDAO.updateLikeCount(likeDislike);
            } else {
                likeDislike.setDislikeCount(Math.max(0, likeDislike.getDislikeCount() - 1));
                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
            }

            // 새로운 투표 추가
            MenuVote newVote = new MenuVote();
            newVote.setNormalizedMenuName(normalizedMenuName);
            newVote.setVoteType(voteType);
            newVote.setSessionId(sessionId);
            newVote.setCreatedAt(LocalDateTime.now());
            menuVoteDAO.insertMenuVote(newVote);

            // 새로운 투표 집계 증가
            if ("LIKE".equals(voteType)) {
                likeDislike.setLikeCount(likeDislike.getLikeCount() + 1);
                menuLikeDislikeDAO.updateLikeCount(likeDislike);
            } else {
                likeDislike.setDislikeCount(likeDislike.getDislikeCount() + 1);
                menuLikeDislikeDAO.updateDislikeCount(likeDislike);
            }
            currentVote = voteType;
        }

        // 최신 집계 조회
        likeDislike = menuLikeDislikeDAO.selectByNormalizedMenuName(normalizedMenuName);
        
        // null 체크 및 기본값 설정
        if (likeDislike != null) {
            if (likeDislike.getLikeCount() == null) {
                likeDislike.setLikeCount(0);
            }
            if (likeDislike.getDislikeCount() == null) {
                likeDislike.setDislikeCount(0);
            }
        } else {
            // 조회 결과가 null인 경우 기본값으로 생성
            likeDislike = new MenuLikeDislike();
            likeDislike.setNormalizedMenuName(normalizedMenuName);
            likeDislike.setLikeCount(0);
            likeDislike.setDislikeCount(0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", likeDislike.getLikeCount() != null ? likeDislike.getLikeCount() : 0);
        result.put("dislikeCount", likeDislike.getDislikeCount() != null ? likeDislike.getDislikeCount() : 0);
        result.put("currentVote", currentVote);
        
        log.debug("투표 처리 완료 - 좋아요: {}, 싫어요: {}, 현재 투표: {}", 
            result.get("likeCount"), result.get("dislikeCount"), currentVote);

        return result;
    }

    /**
     * 메뉴명에 대한 좋아요/싫어요 집계를 조회합니다
     * @param menuName 원본 메뉴명
     * @return 좋아요/싫어요 집계
     */
    public MenuLikeDislike getLikeDislike(String menuName) {
        String normalizedMenuName = normalizeMenuName(menuName);
        if (normalizedMenuName.isEmpty()) {
            log.debug("정규화된 메뉴명이 비어있음: {}", menuName);
            return null;
        }
        log.debug("좋아요/싫어요 조회 - 원본: {}, 정규화: {} (바이트: {})", 
            menuName, normalizedMenuName, 
            java.util.Arrays.toString(normalizedMenuName.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        MenuLikeDislike result = menuLikeDislikeDAO.selectByNormalizedMenuName(normalizedMenuName);
        if (result != null) {
            log.debug("조회 결과 - normalized_menu_name: {} (바이트: {}), like_count: {}, dislike_count: {}", 
                result.getNormalizedMenuName(),
                result.getNormalizedMenuName() != null ? java.util.Arrays.toString(result.getNormalizedMenuName().getBytes(java.nio.charset.StandardCharsets.UTF_8)) : "null",
                result.getLikeCount(), result.getDislikeCount());
        } else {
            log.debug("조회 결과: null (데이터 없음)");
        }
        return result;
    }

    /**
     * 사용자의 현재 투표 상태를 조회합니다
     * @param menuName 원본 메뉴명
     * @param sessionId 세션 ID
     * @return 현재 투표 타입 (LIKE, DISLIKE, null)
     */
    public String getCurrentVote(String menuName, String sessionId) {
        String normalizedMenuName = normalizeMenuName(menuName);
        if (normalizedMenuName.isEmpty()) {
            return null;
        }
        MenuVote vote = menuVoteDAO.selectByNormalizedMenuNameAndSessionId(normalizedMenuName, sessionId);
        return vote != null ? vote.getVoteType() : null;
    }

    /**
     * 여러 메뉴명에 대한 좋아요/싫어요 집계를 일괄 조회합니다
     * @param menuNames 메뉴명 리스트
     * @return 메뉴명별 좋아요/싫어요 집계 맵
     */
    public Map<String, MenuLikeDislike> getLikeDislikeMap(List<String> menuNames) {
        Map<String, MenuLikeDislike> result = new HashMap<>();
        for (String menuName : menuNames) {
            if (menuName != null && !menuName.isEmpty()) {
                MenuLikeDislike likeDislike = getLikeDislike(menuName);
                result.put(menuName, likeDislike);
            }
        }
        return result;
    }

    /**
     * 여러 메뉴명에 대한 사용자의 투표 상태를 일괄 조회합니다
     * @param menuNames 메뉴명 리스트
     * @param sessionId 세션 ID
     * @return 메뉴명별 투표 상태 맵
     */
    public Map<String, String> getCurrentVoteMap(List<String> menuNames, String sessionId) {
        Map<String, String> result = new HashMap<>();
        for (String menuName : menuNames) {
            if (menuName != null && !menuName.isEmpty()) {
                String vote = getCurrentVote(menuName, sessionId);
                result.put(menuName, vote);
            }
        }
        return result;
    }

    /**
     * 디버깅용: 모든 좋아요/싫어요 데이터를 조회합니다
     * @return 모든 좋아요/싫어요 데이터 리스트
     */
    public List<MenuLikeDislike> getAllLikeDislike() {
        return menuLikeDislikeDAO.selectAll();
    }
}

