package cn.zyf.elasticsearch.service;

import cn.zyf.elasticsearch.model.People;
import cn.zyf.elasticsearch.util.RandomUtil;
import cn.zyf.elasticsearch.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NearbyService {
    @Autowired
    private TransportClient client;

    private String indexName = "nearby"; //相当于数据库名称
    private String indexType = "wechat";	//相当于数据表名称

    //建库建表建约束

    /**
     * 建库建表建约束的方法
     */
    public void recreateIndex() throws IOException {
        try{
            //后台级的操作，关乎到删除跑路的危险
            if(!client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()){ return;}
            //先清除原来已有的数据库
            client.admin().indices().prepareDelete(indexName).execute().actionGet();
        }catch(Exception e){
            e.printStackTrace();
        }
        createIndex();
    }


    /**
     * 创建索引
     * @throws IOException
     */
    private void createIndex() throws IOException {
        //表结构(建约束)
        XContentBuilder mapping = createMapping();

        //建库
        //建库建表建约束
        CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(indexName).execute().actionGet();
        if(!createIndexResponse.isAcknowledged()){
            log.info("无法创建索引[" + indexName + "]");
        }else{
            log.info("创建索引["+indexName+"]成功");
        }
        //建表
        PutMappingRequest putMapping = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        AcknowledgedResponse response = client.admin().indices().putMapping(putMapping).actionGet();

        if(!response.isAcknowledged()){
            log.info("无法创建[" + indexName + "] [" + indexType + "]的Mapping");
        }else{
            log.info("创建[" + indexName + "] [" + indexType + "]的Mapping成功");
        }

    }

    /**
     * 准备模拟数据，这些数值会随机生成
     * @param myLat 维度
     * @param myLon 经度
     * @param count 生成多少个
     */
    public Integer addDataToIndex(double myLat,double myLon,int count) {
        List<XContentBuilder> contents = new ArrayList<XContentBuilder>();

        //开启重复校验的缓存区
        RandomUtil.openCache();

        //一个循环跑下来就产生了10W条模拟记录，也得要具有一定的真实性
        for (long i = 0; i < count; i++) {
            People people = randomPeople(myLat,myLon);
            contents.add(obj2XContent(people));
        }

        //清空重复校验的缓存区
        RandomUtil.clearCache();

        //把数据批量写入到数据库表中
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (XContentBuilder content : contents) {
            IndexRequest request = client.prepareIndex(indexName, indexType).setSource(content).request();
            bulkRequest.add(request);
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            log.info("创建索引出错！");
        }
        return bulkRequest.numberOfActions();
    }

    /**
     * 检索附近的人
     * @param lat
     * @param lon
     * @param radius
     * @param size
     */
    public SearchResult search(double lat, double lon, int radius, int size, String sex){
        SearchResult result = new SearchResult();

        //统一单位为米
        String unit = DistanceUnit.METERS.toString();//坐标范围计量单位

        //获取一个查询规则构造器
        //查是哪个库哪个表
        //完成了相当于 select * from 数据库.表名
        SearchRequestBuilder srb = client.prepareSearch(indexName).setTypes(indexType);

        //实现分页操作
        //相当于MySQL中的  limit 0,size
        srb.setFrom(0).setSize(size);//取出优先级最高的size条数据

        //拼接查询条件
        //性别、昵称，坐标

        //构建查询条件

        //地理坐标，方圆多少米以内都要找出来
        QueryBuilder qb = QueryBuilders.geoDistanceQuery("location")
                .point(lat, lon)
                .distance(radius,DistanceUnit.METERS)
                .geoDistance(GeoDistance.PLANE); //设置计算规则，是平面还是立体 (方圆多少米)

        //相对于 where location > 0 and location < radius
        srb.setPostFilter(qb);

        //继续拼接where条件
        //and sex = ?
        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        if(!(sex == null || "".equals(sex.trim()))){
            bq.must(QueryBuilders.matchQuery("sex", sex));
        }
        srb.setQuery(bq);

        //设置排序规则

        GeoDistanceSortBuilder geoSort = SortBuilders.geoDistanceSort("location",lat, lon);
        geoSort.unit(DistanceUnit.METERS);
        geoSort.order(SortOrder.ASC);//按距离升序排序，最近的要排在最前面

        //order by location asc 升序排序
        srb.addSort(geoSort);

        //到此为止，就相当于SQL语句构建完毕

        //开始执行查询
        //调用  execute()方法
        //Response
        SearchResponse response = srb.execute().actionGet();

        //高亮分词
        SearchHits hits = response.getHits();
        SearchHit[] searchHists = hits.getHits();

        //搜索的耗时
        Float usetime = response.getTook().getMillis() / 1000f;

        result.setTotal(hits.getTotalHits());
        result.setUseTime(usetime);
        result.setDistance(DistanceUnit.METERS.toString());
        result.setData(new ArrayList<Map<String,Object>>());
        for (SearchHit hit : searchHists) {
            // 获取距离值，并保留两位小数点
            BigDecimal geoDis = new BigDecimal((Double) hit.getSortValues()[0]);
            Map<String, Object> hitMap = hit.getSourceAsMap();
            // 在创建MAPPING的时候，属性名不可为geoDistance。
            hitMap.put("geoDistance", geoDis.setScale(0, BigDecimal.ROUND_HALF_DOWN));
            result.getData().add(hitMap);
        }

        return result;
    }

    /**
     * 创建mapping，相当于创建表结构
     */
    private XContentBuilder createMapping() {
        XContentBuilder mapping = null;
        try {
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                        // 索引库名（类似数据库中的表）
                        .startObject(indexType)
                            .startObject("properties")
                                //微信号（唯一的索引）  keyword  text
                                .startObject("wxNo").field("type", "keyword").endObject()
                                //昵称
                                .startObject("nickName").field("type", "keyword").endObject()
                                //性别
                                .startObject("sex").field("type","keyword").endObject()
                                //位置，专门用来存储地理坐标的类型，包含了经度和纬度
                                .startObject("location").field("type", "geo_point").endObject()
                            .endObject()
                        .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapping;
    }

    /**
     * 将Java对象转换为JSON字符串（所谓的全文检索，玩的就是字符串）
     */
    private XContentBuilder obj2XContent(People people) {
        XContentBuilder jsonBuild = null;
        try {
            // 使用XContentBuilder创建json数据
            jsonBuild = XContentFactory.jsonBuilder();
            jsonBuild.startObject()
                    .field("wxNo", people.getWxNo())
                    .field("nickName", people.getNickName())
                    .field("sex",people.getSex())
                    .startObject("location")
                    .field("lat",people.getLat())
                    .field("lon",people.getLon())
                    .endObject()
                    .endObject();
            log.info(Strings.toString(jsonBuild));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonBuild;
    }

    /**
     * 模拟一个用户
     * @param myLat 所在的纬度
     * @param myLon 所在的经度
     */
    public People randomPeople(double myLat,double myLon){
        //随机生成微信号
        String wxNo = RandomUtil.randomWxNo();
        //造人计划，性别随机
        String sex = RandomUtil.randomSex();
        //随机生成昵称
        String nickName = RandomUtil.randomNickName(sex);
        //随机生成坐标
        double [] point = RandomUtil.randomPoint(myLat,myLon);

        return new People(point[0],point[1],wxNo,nickName,sex);
    }

}
