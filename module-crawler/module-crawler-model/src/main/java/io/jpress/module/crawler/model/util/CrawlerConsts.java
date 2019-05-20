package io.jpress.module.crawler.model.util;

/**
 * 爬虫常量定义
 *
 * @author Eric.Huang
 * @date 2019-05-18 13:34
 * @package io.jpress.module.crawler.util
 **/

public class CrawlerConsts {

    /** 增加关键词数量 */
    public static final String PLUS_KEYWORD_NUM_EVENT_NAME = "plusKeywordNum";
    /** 减少关键词数量 */
    public static final String MINUS_KEYWORD_NUM_EVENT_NAME = "minusKeywordNum";

    /** 新增关键词 */
    public static final String ADD = "add";
    /** 删除关键词 */
    public static final String MINUS = "minus";

    /** 备份关键词文件路径 */
    public static final String BACKUP_KEYWORDS_PATH = "/backup";

    public static final String SEARCH_ENGINE_BAIDU = "baidu";
    public static final String SEARCH_ENGINE_SOGO = "sogo";
    public static final String SEARCH_ENGINE_360 = "soso";
    public static final String SEARCH_ENGINE_SHENMA = "shenma";

    /** 更新关键词状态(相关词) */
    public static final String UPDATE_REL_WORD_EVENT_NAME = "updateRelWordStatus";

    /** 添加相关关键词 */
    public static final String ADD_REL_WORD_EVENT_NAME = "addRelWordEvent";

    /** 更新关键词状态(下拉词) */
    public static final String UPDATE_SUG_WORD_EVENT_NAME = "updateSugWordStatus";


}
