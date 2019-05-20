package io.jpress.crawlergen;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import com.jfinal.plugin.activerecord.generator.TableMeta;
import io.jboot.app.JbootApplication;
import io.jboot.codegen.CodeGenHelpler;
import io.jboot.utils.StrUtil;
import io.jpress.codegen.generator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 功能代码生成
 *
 * @author Eric.Huang
 * @date 2019-04-29 10:25
 * @package io.jpress.systemgen
 **/

public class CrawlerSingleGenerator {

    public static void main(String[] args) {

        String dbTables = "c_schedule_task";

        JbootApplication.setBootArg("jboot.datasource.url", "jdbc:mysql://localhost:3306/jpress-2.0");
        JbootApplication.setBootArg("jboot.datasource.user", "root");
        JbootApplication.setBootArg("jboot.datasource.password", "123456");

        String modelPackage = "io.jpress.module.crawler.model";

        String baseModelPackage = modelPackage + ".base";

        String modelDir = PathKit.getWebRootPath() + "/../module-crawler/module-crawler-model/src/main/java/" + modelPackage.replace(".", "/");
        String baseModelDir = PathKit.getWebRootPath() + "/../module-crawler/module-crawler-model/src/main/java/" + baseModelPackage.replace(".", "/");

        System.out.println("start generate...dir:" + modelDir);

        List<TableMeta> tableMetaList = new ArrayList<>();
        Set<String> excludeTableSet = StrUtil.splitToSet(dbTables, ",");
        MetaBuilder mb = CodeGenHelpler.createMetaBuilder();
        mb.setGenerateRemarks(true);
        mb.setRemovedTableNamePrefixes("c");

        for (TableMeta tableMeta : mb.build()) {
            if (excludeTableSet.contains(tableMeta.name.toLowerCase())) {
                tableMetaList.add(tableMeta);
            }
        }

        new BaseModelGenerator(baseModelPackage, baseModelDir).generate(tableMetaList);
        new ModelGenerator(modelPackage, baseModelPackage, modelDir).generate(tableMetaList);

        String servicePackage = "io.jpress.module.crawler.service";
        String apiPath = PathKit.getWebRootPath() + "/../module-crawler/module-crawler-service-api/src/main/java/" + servicePackage.replace(".", "/");
        String providerPath = PathKit.getWebRootPath() + "/../module-crawler/module-crawler-service-provider/src/main/java/" + servicePackage.replace(".", "/") + "/provider";


        new ServiceApiGenerator(servicePackage, modelPackage, apiPath).generate(tableMetaList);
        new ServiceProviderGenerator(servicePackage, modelPackage, providerPath).generate(tableMetaList);

        new ModuleUIGenerator("crawler", modelPackage, tableMetaList).genListener().genControllers().genEdit().genList();
    }
}
