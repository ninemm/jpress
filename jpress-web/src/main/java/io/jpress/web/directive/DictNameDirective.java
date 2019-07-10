package io.jpress.web.directive;

import com.jfinal.aop.Inject;
import com.jfinal.template.Env;
import com.jfinal.template.TemplateException;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;
import io.jboot.utils.StrUtil;
import io.jboot.web.directive.annotation.JFinalDirective;
import io.jboot.web.directive.base.JbootDirectiveBase;
import io.jpress.model.Dict;
import io.jpress.service.DictService;

import java.io.IOException;
import java.util.List;

/**
 * 字典名称标签
 *
 * @author Eric.Huang
 * @date 2019-04-29 17:55
 * @package io.jpress.web.directive
 **/

@JFinalDirective("dictName")
public class DictNameDirective extends JbootDirectiveBase {

    @Inject
    private DictService dictService;

    @Override
    public void onRender(Env env, Scope scope, Writer writer) {

        String value = getPara(0, scope);

        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException("paramter of dict value must not be null");
        }

        String name = dictService.findNameByValue(value);
        try {
            writer.write(name);
        } catch (IOException e) {
            throw new TemplateException(e.getMessage(), location, e);
        }
    }
}
