# Elasticsearch-practice  
  
## Elasticsearch学习实践  
[Elasticsearch + Logstash + Kibana + Nginx 搭建网站实时监控平台](https://github.com/YufeizhangRay/ELK-website-realtime-monitor)  
  
## 包含：  
Elasticsearch Java API 练习源码 (实现模拟查看附近的人)   

### 1.Elasticsearch分布式环境搭建  
#### 1.1 下载  
下载地址：https://www.elastic.co/downloads/past-releases (建议下载统一的ELK版本以确保最高的稳定性，此时的最新版本6.5.1)  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/downloades.jpeg)  
  
下载完毕后直接解压即可。  
  
#### 1.2 参数配置  
找到config文件夹中的jvm.options文件，我们可以设置参数  
 -Xms4g  
 -Xmx4g  
 ![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/JVM.jpeg)  
   
Elasticsearch非常的吃内存，我们可以将这两个参数设置的大一些。  
  
#### 1.3 分布式环境准备  
将下载好的Elasticsearch拷贝为一共三份(为集群)，一个作为master节点，两个作为slave节点(如上图)  
修改主节点的配置，打开 elasticsearch-6.5.1-master\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/master.jpeg)  
  
配置 slave-1 节点，打开 elasticsearch-6.5.1-slave-1\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/slave1.jpeg)  
  
配置 slave-2 节点，打开 elasticsearch-5.5.1-slave-2\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/slave2.jpeg)  
  
使用 ./elasticsearch分别启动三个节点。

### 2.实现可视化   
#### 2.1 可视化插件安装  
下载 NodeJS 环境，打开官网 https://nodejs.org/en/download/  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/NodeJS.jpeg)  
  
根据系统选择相应版本，mac下载后直接安装即可。可以输入 node -v 检查 node 是否安装成功。  
  
#### 2.2 下载 elasticsearch-head  
打开 https://github.com 搜索 elasticsearch-head 关键字，选择 mobz/elasticsearch-head，并下载 elasticsearch-head-master.zip 包解压。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/eshead.jpeg)  
  
修改 master 节点的跨域配置，在 elasticsearch.yml 中追加以下内容。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E8%B7%A8%E5%9F%9F.jpeg)  
  
启动elasticsearch-head  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/starteshead.jpeg)  
  
### 结果展示  
附近的人分片展示  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E5%88%86%E7%89%87%E5%B1%95%E7%A4%BA.jpeg)  
  
附近的人数据展示  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E6%95%B0%E6%8D%AE%E5%B1%95%E7%A4%BA.jpeg)  
  
附近的人查询结果
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E6%9F%A5%E8%AF%A2%E7%BB%93%E6%9E%9C.png)  


