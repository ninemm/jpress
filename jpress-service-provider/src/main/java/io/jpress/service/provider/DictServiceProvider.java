package io.jpress.service.provider;

import com.google.common.base.Splitter;
import com.jfinal.plugin.activerecord.Model;
import io.jboot.Jboot;
import io.jboot.aop.annotation.Bean;
import io.jboot.components.cache.annotation.Cacheable;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import io.jpress.model.Dict;
import io.jpress.service.DictService;

import java.util.List;

@Bean
public class DictServiceProvider extends JbootServiceBase<Dict> implements DictService {

    @Override
    @Cacheable(name = Dict.CACHE_NAME, key = "#(type)")
    public List<Dict> findByType(String type) {
        Columns columns = Columns.create();
        columns.eq("type", type);
        return DAO.findListByColumns(columns, "value asc");
    }

    @Override
    public boolean deleteByIds(Object ids) {

        List<String> list = Splitter.on(",").splitToList(ids.toString());
        list.stream().forEach(id -> {
            deleteById(id);
        });

        return true;
    }

    @Override
    public void shouldUpdateCache(int action, Object data) {

        switch (action) {
            case ACTION_UPDATE :
            case ACTION_DEL :
                if (data instanceof Model) {
                    Dict dict = (Dict) data;
                    Jboot.getCache().remove(Dict.CACHE_NAME, dict.getId());
                    Jboot.getCache().remove(Dict.CACHE_NAME, dict.getType());
                } else {
                    Dict dict = findById(data);
                    Jboot.getCache().remove(Dict.CACHE_NAME, data);
                    Jboot.getCache().remove(Dict.CACHE_NAME, dict.getType());
                }
                break;
            default :
                ;
        }
    }
}