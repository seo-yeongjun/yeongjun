package com.yeongjun.yeongjun.home.widget.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WidgetHoliday {
    private LocalDate holidayDate;
    private String holidayName;
}
