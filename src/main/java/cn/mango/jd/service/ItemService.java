package cn.mango.jd.service;

import cn.mango.jd.pojo.Item;

import java.util.List;

public interface ItemService {

    /**
     * 保存商品
     * @param item
     */
    public void save(Item item);

    /**
     * 根据条件查询商品
     * @param item
     * @return
     */
    public List<Item> findAll(Item item);
}
