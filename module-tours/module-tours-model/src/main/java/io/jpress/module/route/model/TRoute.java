package io.jpress.module.route.model;

import com.jfinal.core.JFinal;
import io.jboot.db.annotation.Table;
import io.jboot.utils.StrUtil;
import io.jpress.JPressConsts;
import io.jpress.JPressOptions;
import io.jpress.commons.utils.CommonsUtils;
import io.jpress.commons.utils.JsoupUtils;
import io.jpress.commons.utils.MarkdownUtils;
import io.jpress.module.route.model.base.BaseTRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated by Jboot.
 * @author eric
 */
@Table(tableName = "t_route", primaryKey = "id")
public class TRoute extends BaseTRoute<TRoute> {

    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_TRASH = "trash";

    public boolean isNormal() {
        return STATUS_NORMAL.equals(getStatus());
    }

    public boolean isDraft() {
        return STATUS_DRAFT.equals(getStatus());
    }

    public boolean isTrash() {
        return STATUS_TRASH.equals(getStatus());
    }

    public String getHtmlView() {
        return StrUtil.isBlank(getStyle()) ? "route.html" : "route_" + getStyle().trim() + ".html";
    }

    public String getUrl() {
        String link = getLinkTo();
        if (StrUtil.isNotBlank(link)) {
            return link;
        }

        if (StrUtil.isBlank(getSlug())) {
            return JFinal.me().getContextPath() + "/route/" + getId() + JPressOptions.getAppUrlSuffix();
        } else {
            return JFinal.me().getContextPath() + "/route/" + getSlug() + JPressOptions.getAppUrlSuffix();
        }
    }

    public boolean _isMarkdownMode() {
        return JPressConsts.EDIT_MODE_MARKDOWN.equals(getEditMode());
    }


    public String _getEditContent() {

        String originalContent = super.getContent();
        if (StrUtil.isBlank(originalContent) || _isMarkdownMode()) {
            return originalContent;
        }

        //ckeditor 编辑器有个bug，自动把 &lt; 转化为 < 和 把 &gt; 转化为 >
        //因此，此处需要 把 "&lt;" 替换为 "&amp;lt;" 和 把 "&gt;" 替换为 "&amp;gt;"
        //方案：http://komlenic.com/246/encoding-entities-to-work-with-ckeditor-3/
        return originalContent.replace("&lt;", "&amp;lt;")
                .replace("&gt;", "&amp;gt;");

    }

    public boolean isCommentEnable() {
        Boolean cs = getCommentStatus();
        return cs != null && cs == true;
    }

    public boolean isTopEnable() {
        Boolean cs = getIsTop();
        return cs != null && cs == true;
    }

    public String getText() {
        return JsoupUtils.getText(getContent());
    }

    @Override
    public String getContent() {
        String content = super.getContent();
        if (StrUtil.isBlank(content)) {
            return null;
        }
        return JsoupUtils.makeImageSrcToAbsolutePath(content, JPressOptions.getResDomain());
    }

    /**
     * 获取文章的所有图片
     *
     * @return
     */
    public List<String> getImages() {
        return JsoupUtils.getImageSrcs(getContent());
    }

    /**
     * 获取前面几张图片
     *
     * @param count
     * @return
     */
    public List<String> getImages(int count) {
        List<String> list = getImages();
        if (list == null || list.size() <= count) {
            return list;
        }

        List<String> newList = new ArrayList<>();
        for (int i = 0; 0 < count; i++) {
            newList.add(list.get(i));
        }
        return newList;
    }

    public boolean hasImage() {
        return getFirstImage() != null;
    }

    public boolean hasVideo() {
        return getFirstVideo() != null;
    }

    public boolean hasAudio() {
        return getFirstAudio() != null;
    }

    public String getFirstImage() {
        return JsoupUtils.getFirstImageSrc(getContent());
    }

    public String getFirstVideo() {
        return JsoupUtils.getFirstVideoSrc(getContent());
    }

    public String getFirstAudio() {
        return JsoupUtils.getFirstAudioSrc(getContent());
    }

    public String getShowImage() {
        String thumbnail = getThumbnail();
        return StrUtil.isNotBlank(thumbnail) ? thumbnail : getFirstImage();
    }

    @Override
    public boolean save() {
        CommonsUtils.escapeHtmlForAllAttrs(this, "content");
        return super.save();
    }

    @Override
    public boolean update() {
        CommonsUtils.escapeHtmlForAllAttrs(this, "content");
        return super.update();
    }

    @Override
    public Integer getOrderList() {
        Integer order = super.getOrderList();
        return order == null ? 0 : order;
    }
}
