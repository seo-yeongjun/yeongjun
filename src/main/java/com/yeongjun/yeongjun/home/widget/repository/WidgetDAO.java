package com.yeongjun.yeongjun.home.widget.repository;

import com.yeongjun.yeongjun.home.widget.model.BalanceGame;
import com.yeongjun.yeongjun.home.widget.model.BalanceGameVote;
import com.yeongjun.yeongjun.home.widget.model.WidgetConfig;
import com.yeongjun.yeongjun.home.widget.model.WidgetHoliday;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WidgetDAO {
    // 0. DB 스키마 교정
    @Update("ALTER TABLE widget_config MODIFY COLUMN config_value TEXT")
    void alterConfigValueColumnType();
    // 1. 위젯 기본 설정 관리
    String selectConfig(@Param("configKey") String configKey);
    int updateConfig(WidgetConfig config);
    List<WidgetConfig> selectConfigList();

    // 2. 공휴일 관리
    List<WidgetHoliday> selectHolidayList();
    int insertHoliday(WidgetHoliday holiday);
    int deleteHoliday(@Param("holidayDate") LocalDate holidayDate);

    // 3. 밸런스 게임 관리
    List<BalanceGame> selectAllBalanceGames();
    int insertBalanceGame(BalanceGame game);
    int selectVoteCount(@Param("questionId") Long questionId, @Param("selection") String selection);
    int checkIpVoted(@Param("questionId") Long questionId, @Param("ipAddress") String ipAddress);
    int insertVote(BalanceGameVote vote);
}
