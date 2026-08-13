package cn.wolfcode.wolf2w.business.util;

import java.util.Calendar;
import java.util.Date;

// 计算过期时间工具类
public class DateUtil {

    // 给定日期的最后一分一秒
    public static Date getEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    // 与当前时间的差值毫秒数
    public static long getBetweenDate(Date date1, Date date2) {

        // 转换为秒
        return Math.abs(date1.getTime() - date2.getTime()) / 1000;

    }

}
