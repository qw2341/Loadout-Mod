package loadout.helper;

import loadout.helper.HanyuPinyinOutputFormat.*;

class PinyinFormatter {
    PinyinFormatter() {
    }

    static String formatHanyuPinyin(String var0, HanyuPinyinOutputFormat var1) {
        if (HanyuPinyinToneType.WITH_TONE_MARK != var1.getToneType() || HanyuPinyinVCharType.WITH_V != var1.getVCharType() && HanyuPinyinVCharType.WITH_U_AND_COLON != var1.getVCharType()) {
            if (HanyuPinyinToneType.WITHOUT_TONE == var1.getToneType()) {
                var0 = var0.replaceAll("[1-5]", "");
            } else if (HanyuPinyinToneType.WITH_TONE_MARK == var1.getToneType()) {
                var0 = var0.replaceAll("u:", "v");
                var0 = convertToneNumber2ToneMark(var0);
            }

            if (HanyuPinyinVCharType.WITH_V == var1.getVCharType()) {
                var0 = var0.replaceAll("u:", "v");
            } else if (HanyuPinyinVCharType.WITH_U_UNICODE == var1.getVCharType()) {
                var0 = var0.replaceAll("u:", "ü");
            }

            if (HanyuPinyinCaseType.UPPERCASE == var1.getCaseType()) {
                var0 = var0.toUpperCase();
            }

            return var0;
        } else return null;
    }

    private static String convertToneNumber2ToneMark(String var0) {
        String var1 = var0.toLowerCase();
        if (var1.matches("[a-z]*[1-5]?")) {
            char var4 = '$';
            int var5 = -1;
            if (!var1.matches("[a-z]*[1-5]")) {
                return var1.replaceAll("v", "ü");
            } else {
                int var11 = Character.getNumericValue(var1.charAt(var1.length() - 1));
                int var12 = var1.indexOf(97);
                int var13 = var1.indexOf(101);
                int var14 = var1.indexOf("ou");
                int var15;
                if (-1 != var12) {
                    var5 = var12;
                    var4 = 'a';
                } else if (-1 != var13) {
                    var5 = var13;
                    var4 = 'e';
                } else if (-1 != var14) {
                    var5 = var14;
                    var4 = "ou".charAt(0);
                } else {
                    for(var15 = var1.length() - 1; var15 >= 0; --var15) {
                        if (String.valueOf(var1.charAt(var15)).matches("[aeiouv]")) {
                            var5 = var15;
                            var4 = var1.charAt(var15);
                            break;
                        }
                    }
                }

                if ('$' != var4 && -1 != var5) {
                    var15 = "aeiouv".indexOf(var4);
                    int var16 = var11 - 1;
                    int var17 = var15 * 5 + var16;
                    char var18 = "āáăàaēéĕèeīíĭìiōóŏòoūúŭùuǖǘǚǜü".charAt(var17);
                    StringBuffer var19 = new StringBuffer();
                    var19.append(var1.substring(0, var5).replaceAll("v", "ü"));
                    var19.append(var18);
                    var19.append(var1.substring(var5 + 1, var1.length() - 1).replaceAll("v", "ü"));
                    return var19.toString();
                } else {
                    return var1;
                }
            }
        } else {
            return var1;
        }
    }
}
