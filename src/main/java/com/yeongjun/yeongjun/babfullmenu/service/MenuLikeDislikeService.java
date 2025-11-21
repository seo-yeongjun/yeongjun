package com.yeongjun.yeongjun.babfullmenu.service;

import com.yeongjun.yeongjun.babfullmenu.model.MenuLikeDislike;
import com.yeongjun.yeongjun.babfullmenu.model.MenuVote;
import com.yeongjun.yeongjun.babfullmenu.repository.MenuLikeDislikeDAO;
import com.yeongjun.yeongjun.babfullmenu.repository.MenuVoteDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    private MenuLikeDislike upsertAggregate(LocalDate menuDate, String menuName) {
        MenuLikeDislike param = new MenuLikeDislike();
        param.setMenuDate(menuDate);
        param.setMenuName(menuName);

        MenuLikeDislike aggregate = menuLikeDislikeDAO.selectByMenuDateAndName(param);
        if (aggregate == null) {
            aggregate = new MenuLikeDislike(null, menuDate, menuName, 0, 0);
            menuLikeDislikeDAO.insertMenuLikeDislike(aggregate);
        } else {
            if (aggregate.getLikeCount() == null) {
                aggregate.setLikeCount(0);
            }
            if (aggregate.getDislikeCount() == null) {
                aggregate.setDislikeCount(0);
            }
        }
        return aggregate;
    }

    private MenuVote findExistingVote(LocalDate menuDate, String menuName, String sessionId) {
        MenuVote voteParam = new MenuVote();
        voteParam.setMenuDate(menuDate);
        voteParam.setMenuName(menuName);
        voteParam.setSessionId(sessionId);
        return menuVoteDAO.selectByMenuDateNameAndSessionId(voteParam);
    }

    @Transactional
    public Map<String, Object> vote(String menuName, LocalDate menuDate, String voteType, String sessionId) {
        if (menuName == null || menuName.isBlank()) {
            throw new IllegalArgumentException("메뉴명이 비어 있습니다.");
        }
        if (menuDate == null) {
            throw new IllegalArgumentException("메뉴 날짜가 비어 있습니다.");
        }
        if (!"LIKE".equals(voteType) && !"DISLIKE".equals(voteType)) {
            throw new IllegalArgumentException("잘못된 투표 타입입니다.");
        }

        log.debug("투표 처리 - 메뉴명: {}, 날짜: {}, 타입: {}", menuName, menuDate, voteType);

        MenuLikeDislike aggregate = upsertAggregate(menuDate, menuName);
        MenuVote existingVote = findExistingVote(menuDate, menuName, sessionId);

        String currentVote = existingVote != null ? existingVote.getVoteType() : null;

        if (existingVote == null) {
            // 신규 투표
            MenuVote newVote = new MenuVote(null, menuDate, menuName, voteType, sessionId, LocalDateTime.now());
            menuVoteDAO.insertMenuVote(newVote);

            if ("LIKE".equals(voteType)) {
                aggregate.setLikeCount(aggregate.getLikeCount() + 1);
                menuLikeDislikeDAO.updateLikeCount(aggregate);
            } else {
                aggregate.setDislikeCount(aggregate.getDislikeCount() + 1);
                menuLikeDislikeDAO.updateDislikeCount(aggregate);
            }
            currentVote = voteType;
        } else if (voteType.equals(existingVote.getVoteType())) {
            // 같은 투표 재입력 -> 취소
            menuVoteDAO.deleteMenuVote(existingVote);
            if ("LIKE".equals(voteType)) {
                aggregate.setLikeCount(Math.max(0, aggregate.getLikeCount() - 1));
                menuLikeDislikeDAO.updateLikeCount(aggregate);
            } else {
                aggregate.setDislikeCount(Math.max(0, aggregate.getDislikeCount() - 1));
                menuLikeDislikeDAO.updateDislikeCount(aggregate);
            }
            currentVote = null;
        } else {
            // 투표 타입 변경
            menuVoteDAO.deleteMenuVote(existingVote);
            if ("LIKE".equals(existingVote.getVoteType())) {
                aggregate.setLikeCount(Math.max(0, aggregate.getLikeCount() - 1));
                menuLikeDislikeDAO.updateLikeCount(aggregate);
            } else {
                aggregate.setDislikeCount(Math.max(0, aggregate.getDislikeCount() - 1));
                menuLikeDislikeDAO.updateDislikeCount(aggregate);
            }

            MenuVote newVote = new MenuVote(null, menuDate, menuName, voteType, sessionId, LocalDateTime.now());
            menuVoteDAO.insertMenuVote(newVote);

            if ("LIKE".equals(voteType)) {
                aggregate.setLikeCount(aggregate.getLikeCount() + 1);
                menuLikeDislikeDAO.updateLikeCount(aggregate);
            } else {
                aggregate.setDislikeCount(aggregate.getDislikeCount() + 1);
                menuLikeDislikeDAO.updateDislikeCount(aggregate);
            }
            currentVote = voteType;
        }

        MenuLikeDislike latest = menuLikeDislikeDAO.selectByMenuDateAndName(aggregate);
        if (latest == null) {
            latest = new MenuLikeDislike(null, menuDate, menuName, 0, 0);
        } else {
            if (latest.getLikeCount() == null) {
                latest.setLikeCount(0);
            }
            if (latest.getDislikeCount() == null) {
                latest.setDislikeCount(0);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", latest.getLikeCount());
        result.put("dislikeCount", latest.getDislikeCount());
        result.put("currentVote", currentVote);
        return result;
    }

    public MenuLikeDislike getLikeDislike(String menuName, LocalDate menuDate) {
        if (menuName == null || menuName.isBlank() || menuDate == null) {
            return null;
        }
        MenuLikeDislike param = new MenuLikeDislike();
        param.setMenuDate(menuDate);
        param.setMenuName(menuName);
        MenuLikeDislike result = menuLikeDislikeDAO.selectByMenuDateAndName(param);
        if (result != null) {
            if (result.getLikeCount() == null) {
                result.setLikeCount(0);
            }
            if (result.getDislikeCount() == null) {
                result.setDislikeCount(0);
            }
        }
        return result;
    }

    public String getCurrentVote(String menuName, LocalDate menuDate, String sessionId) {
        if (menuName == null || menuName.isBlank() || menuDate == null) {
            return null;
        }
        MenuVote existing = findExistingVote(menuDate, menuName, sessionId);
        return existing != null ? existing.getVoteType() : null;
    }

    public Map<String, MenuLikeDislike> getLikeDislikeMap(Map<String, MenuIdentifier> menuIdentifierMap) {
        Map<String, MenuLikeDislike> result = new HashMap<>();
        for (Map.Entry<String, MenuIdentifier> entry : menuIdentifierMap.entrySet()) {
            MenuIdentifier identifier = entry.getValue();
            MenuLikeDislike likeDislike = getLikeDislike(identifier.menuName(), identifier.menuDate());
            result.put(entry.getKey(), likeDislike);
        }
        return result;
    }

    public Map<String, String> getCurrentVoteMap(Map<String, MenuIdentifier> menuIdentifierMap, String sessionId) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, MenuIdentifier> entry : menuIdentifierMap.entrySet()) {
            MenuIdentifier identifier = entry.getValue();
            String vote = getCurrentVote(identifier.menuName(), identifier.menuDate(), sessionId);
            result.put(entry.getKey(), vote);
        }
        return result;
    }

    public List<MenuLikeDislike> getAllLikeDislike() {
        return menuLikeDislikeDAO.selectAll();
    }

    public record MenuIdentifier(LocalDate menuDate, String menuName) {
    }
}
