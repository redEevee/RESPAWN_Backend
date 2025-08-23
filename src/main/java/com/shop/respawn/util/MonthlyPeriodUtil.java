package com.shop.respawn.util;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@RequiredArgsConstructor
public class MonthlyPeriodUtil {

    /** 저번달 [start, end] 반환 */
    public static LocalDateTime[] previousMonthRange() {
        LocalDate today = LocalDate.now();
        YearMonth prevYm = YearMonth.from(today.minusMonths(1));
        LocalDateTime start = prevYm.atDay(1).atStartOfDay();                  // 저번달 1일 00:00:00.000
        LocalDateTime end   = prevYm.atEndOfMonth().atTime(23,59,59, 999_000_000); // 저번달 말일 23:59:59.999
        return new LocalDateTime[]{start, end};
    }
}
