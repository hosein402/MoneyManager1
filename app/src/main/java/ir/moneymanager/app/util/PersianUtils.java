package ir.moneymanager.app.util;

import java.text.NumberFormat;
import java.util.Locale;

public class PersianUtils {

    private static final char[] PERSIAN_DIGITS = {
            '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'
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
}
