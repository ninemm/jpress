package io.jpress.module.crawler.model.status;

import io.jpress.commons.status.BaseStatus;

/**
 * 关键词来源状态
 *
 * @author Eric.Huang
 * @date 2019-05-23 23:30
 * @package io.jpress.module.crawler.model.status
 **/

public class KeywordSourceStatus extends BaseStatus {

    public final static String INPUT = "input";
    public final static String IMPORT = "import";
    public final static String SUG_COLLECTOR = "sug";
    public final static String REL_COLLECTOR = "relate";

    public KeywordSourceStatus() {
        add(INPUT, "录入");
        add(IMPORT, "导入");
        add(SUG_COLLECTOR, "下拉采集");
        add(REL_COLLECTOR, "相关词采集");
    }

    private static KeywordSourceStatus me;

    public static KeywordSourceStatus me() {
        if (me == null) {
            me = new KeywordSourceStatus();
        }
        return me;
    }

}
