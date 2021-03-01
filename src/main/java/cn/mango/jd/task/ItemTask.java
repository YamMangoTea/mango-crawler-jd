package cn.mango.jd.task;

import cn.mango.jd.pojo.Item;
import cn.mango.jd.service.ItemService;
import cn.mango.jd.util.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ItemTask {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ItemService itemService;

    //当下载任务完成后，间隔多长时间进行下一次的任务。
    @Scheduled(fixedDelay = 100 * 1000)
    public void itemTask() throws Exception {
        System.out.println("itemTask Start!!");
        String url = "https://list.jd.com/list.html?cat=9987%2C653%2C655&page=";

        // i<=10 为前五页
        for (int i = 1; i <= 1; i = i + 2) {
            String html = httpUtils.doGetHtml(url + i);
            this.parse(html);
        }

        System.out.println("itemTask End: 数据抓取完成!!");
    }

    //解析页面，获取商品数据并存储
    private void parse(String html) throws Exception {
        Document dom = Jsoup.parse(html);
        //获取spu信息
        Elements spuElements = dom.select("div#J_goodsList > ul > li");
        for (Element spuElement : spuElements) {
            //获取spu
            long spu = 0;
            try {
                spu = Long.parseLong(spuElement.attr("data-spu"));
            } catch (NumberFormatException e) {
                System.out.println("itemTask Tips: 广告机型获取不到spu!!");
                //跳过广告机型
                continue;
            }

            //获取sku
            Elements skuElements = spuElement.select("li.ps-item");
            for (Element skuElement : skuElements) {
                long sku = Long.parseLong(skuElement.select("[data-sku]").attr("data-sku"));
                //根据sku查询商品数据
                Item item = new Item();
                item.setSku(sku);
                List<Item> list = itemService.findAll(item);
                if (list.size() > 0) {
                    //如果商品存在，就进行下一个循环，该商品不保存
                    continue;
                }
                //设置商品的spu
                item.setSpu(spu);
                //获取商品的详情的url
                String itemUrl = "https://item.jd.com/" + sku + ".html";
                item.setUrl(itemUrl);

                //获取商品的图片
                String picUrl = "https:" + skuElement.select("img[data-sku]").first().attr("data-lazy-img");
                picUrl = picUrl.replace("/n7/", "/n1/");
                String picName = this.httpUtils.doGetImage(picUrl);
                item.setPic(picName);


                //获取商品的价格
                String priceJson = this.httpUtils.doGetHtml("https://p.3.cn/prices/mgets?skuIds=J_" + sku);
                double price = MAPPER.readTree(priceJson).get(0).get("p").asDouble();
                item.setPrice(price);

                //获取商品的标题
                String itemInfo = this.httpUtils.doGetHtml(item.getUrl());
                String title = Jsoup.parse(itemInfo).select("div.sku-name").first().text();
                item.setTitle(title);

                item.setCreated(new Date());
                item.setUpdated(item.getCreated());

                //保存商品数据到数据库中
                this.itemService.save(item);
            }
        }
    }
}
