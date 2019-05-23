package io.jpress.module.crawler.listener;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.module.crawler.model.Keyword;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.service.KeywordService;

import java.util.Date;
import java.util.List;

/**
 * 保存相关关键词
 *
 * @author Eric.Huang
 * @date 2019-05-20 18:28
 * @package io.jpress.module.crawler.listener
 **/

@EventConfig(action = CrawlerConsts.ADD_KEYWORD_EVENT_NAME)
public class AddKeywordEventListener implements JbootEventListener {

    private static final Log _LOG = Log.getLog(AddKeywordEventListener.class);

    @Override
    public void onEvent(JbootEvent event) {

        Ret ret = event.getData();
        Integer parentId = ret.getInt("parentId");
        List<String> list = (List<String>) ret.get("relWordList");
        Date created = new Date();

        List<String> sqlList = Lists.newArrayList();
        KeywordService keywordService = Aop.get(KeywordService.class);
        Keyword persist = keywordService.findById(parentId);

        list.stream().forEach(title -> {

            List<Pinyin> pinyinList = HanLP.convertToPinyinList(title);
            String head = pinyinList.get(0).getHeadString();
            String pinyin = "none".equals(head) ? "-" : head;
            int level = persist.getLevel() + 1;

            Object categoryId = persist.getCategoryId();
            String categoryName = persist.getCategoryName();
            String sql = getDistinctInsertSQL(title, parentId, categoryId, categoryName, level, pinyin, created);
            sqlList.add(sql);

        });

        int[] ids = Db.batch(sqlList, sqlList.size());
        /** 更新分类中关键词数量 */
        if (ids.length > 0) {
            String sql = "update c_keyword_category set total_num = total_num + ? where id = ?";
            Db.update(sql, ids.length, persist.getCategoryId());
        }
        _LOG.info("---------相关关键词保存完成---------");
    }

    private String getDistinctInsertSQL(String title, Object parentId, Object categoryId, String categoryName,
            Integer level, String pinyin, Date createDate) {

        StringBuilder sqlBuilder = new StringBuilder("insert ignore into c_keyword(`title`, `parent_id`,");
        sqlBuilder.append(" `category_id`, `category_name`, `num`, `pinyin`, `level`, `status`, `created`) values(");

        sqlBuilder.append("'").append(title).append("'").append(", ");
        sqlBuilder.append(parentId).append(", ");
        sqlBuilder.append(categoryId).append(", ");
        sqlBuilder.append("'").append(categoryName).append("'").append(", ");

        sqlBuilder.append(title.length()).append(", ");
        sqlBuilder.append("'").append(pinyin).append("'").append(", ");
        sqlBuilder.append(level).append(", ");
        sqlBuilder.append(true).append(", ");

        sqlBuilder.append("'").append(createDate).append("'");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }
}
