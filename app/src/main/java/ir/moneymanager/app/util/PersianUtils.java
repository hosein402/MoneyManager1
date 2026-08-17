package ir.moneymanager.app.util;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class PersianUtils {

    private static final char[] PERSIAN_DIGITS = {
            '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'
    };

    private static final String[] JALALI_MONTHS = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };

    public static String formatAmount(long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        String withCommas = nf.format(Math.abs(amount));
        String result = toPersianDigits(withCommas);
        return (amount < 0 ? "-" : "") + result;
    }

    public static String toPersianDigits(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(PERSIAN_DIGITS[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int jy;
        int gy2;
        if (gy > 1600) {
            jy = 979;
            gy -= 1600;
        } else {
            jy = 0;
            gy -= 621;
        }
        gy2 = (gm > 2) ? (gy + 1) : gy;
        int days = (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gd + g_d_m[gm - 1];
        jy += 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm, jd;
        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + (days % 31);
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + ((days - 186) % 30);
        }
        return new int[]{jy, jm, jd};
    }

    public static int[] jalaliToGregorian(int jy, int jm, int jd) {
        int gy;
        if (jy > 979) {
            gy = 1600;
            jy -= 979;
        } else {
            gy = 621;
        }
        int days = (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + 78 + jd
                + ((jm < 7) ? (jm - 1) * 31 : ((jm - 7) * 30) + 186);
        gy += 400 * (days / 146097);
        days %= 146097;
        if (days > 36524) {
            days--;
            gy += 100 * (days / 36524);
            days %= 36524;
            if (days >= 365) days++;
        }
        gy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            gy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int gd = days + 1;
        boolean leap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0);
        int[] sal_a = {0, 31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int gm;
        for (gm = 1; gm <= 12; gm++) {
            int v = sal_a[gm];
            if (gd <= v) break;
            gd -= v;
        }
        return new int[]{gy, gm, gd};
    }

    public static int[] millisToJalali(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static long jalaliToMillis(int jy, int jm, int jd, int hour, int minute) {
        int[] g = jalaliToGregorian(jy, jm, jd);
        Calendar cal = Calendar.getInstance();
        cal.set(g[0], g[1] - 1, g[2], hour, minute, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String toJalaliDateString(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int[] j = gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String result = String.format(Locale.US, "%d/%02d/%02d %02d:%02d", j[0], j[1], j[2], hour, minute);
        return toPersianDigits(result);
    }

    public static String jalaliMonthName(int month) {
        if (month < 1 || month > 12) return "";
        return JALALI_MONTHS[month - 1];
    }

    public static int[] todayJalali() {
        return millisToJalali(System.currentTimeMillis());
    }

    public static long[] getDayRange() {
        int[] j = todayJalali();
        long start = jalaliToMillis(j[0], j[1], j[2], 0, 0);
        long dayMillis = 24L * 60 * 60 * 1000;
        return new long[]{start, start + dayMillis - 1};
    }

    public static long[] getMonthRange() {
        int[] j = todayJalali();
        long start = jalaliToMillis(j[0], j[1], 1, 0, 0);
        int nextY = (j[1] == 12) ? j[0] + 1 : j[0];
        int nextM = (j[1] == 12) ? 1 : j[1] + 1;
        long end = jalaliToMillis(nextY, nextM, 1, 0, 0) - 1;
        return new long[]{start, end};
    }

    public static long[] getYearRange() {
        int[] j = todayJalali();
        long start = jalaliToMillis(j[0], 1, 1, 0, 0);
        long end = jalaliToMillis(j[0] + 1, 1, 1, 0, 0) - 1;
        return new long[]{start, end};
    }
}
