package loadout.helper;

public final class HanyuPinyinOutputFormat {
    public enum HanyuPinyinVCharType {
        WITH_U_AND_COLON,WITH_V,WITH_U_UNICODE
    }

    public enum HanyuPinyinCaseType {
        UPPERCASE,LOWERCASE
    }

    public enum HanyuPinyinToneType {
        WITH_TONE_NUMBER, WITHOUT_TONE, WITH_TONE_MARK
    }
    private HanyuPinyinVCharType vCharType;
    private HanyuPinyinCaseType caseType;
    private HanyuPinyinToneType toneType;

    public HanyuPinyinOutputFormat() {
        this.restoreDefault();
    }

    public void restoreDefault() {
        this.vCharType = HanyuPinyinVCharType.WITH_U_AND_COLON;
        this.caseType = HanyuPinyinCaseType.LOWERCASE;
        this.toneType = HanyuPinyinToneType.WITH_TONE_NUMBER;
    }

    public HanyuPinyinCaseType getCaseType() {
        return this.caseType;
    }

    public void setCaseType(HanyuPinyinCaseType var1) {
        this.caseType = var1;
    }

    public HanyuPinyinToneType getToneType() {
        return this.toneType;
    }

    public void setToneType(HanyuPinyinToneType var1) {
        this.toneType = var1;
    }

    public HanyuPinyinVCharType getVCharType() {
        return this.vCharType;
    }

    public void setVCharType(HanyuPinyinVCharType var1) {
        this.vCharType = var1;
    }
}
