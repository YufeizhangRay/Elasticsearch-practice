# Elasticsearch-practice  
  
## Elasticsearch学习实践  
[Elasticsearch + Logstash + Kibana + Nginx 搭建网站实时监控平台](https://github.com/YufeizhangRay/ELK-website-realtime-monitor)  
  
- [什么是Elasticsearch](#什么是elasticsearch)  
  - [Elasticsearch的优势](#elasticsearch的优势)  
  - [与solr对比](#与solr对比)  
- [Lucene的工作原理](#lucene的工作原理)  
  - [Lucene的基本原理](#lucene的基本原理)  
- [关系型数据库和Elasticsearch的操作对比](#关系型数据库和elasticsearch的操作对比)  
- [Elasticsearch分布式特性](#elasticsearch分布式特性)  
  - [节点启动](#节点启动)  
  - [单点问题](#单点问题)  
- [副本与分片](#副本与分片)  
  - [数据扩容](#数据扩容)  
  - [分片](#分片)  
- [集群状态](#集群状态)  
  - [故障转移](#故障转移)  
  - [脑裂问题](#脑裂问题)    
- [分布式存储](#分布式存储)  
  - [文档创建的流程](#文档创建的流程)
  - [文档读取的流程](#文档读取的流程)  
- [倒排索引](#倒排索引)  
  - [倒排索引不可更改](#倒排索引不可更改)  
  - [倒排索引的实时性](#倒排索引的实时性)  
- [Search的运行机制](#search的运行机制)  
  - [Query阶段](#query阶段)  
  - [Fetch阶段](#fetch阶段)  
- [相关性算分](#相关性算分)  
- [附近的人demo](#附近的人demo)  
  - [Elasticsearch下载](#elasticsearch下载)    
  - [参数配置](#参数配置)  
  - [分布式环境准备](#分布式环境准备)
  - [实现可视化](#实现可视化)  
  - [结果展示](#结果展示)  
  
### 什么是Elasticsearch  
  
ElasticSearch是一个基于Lucene的搜索服务器。它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful web接口。Elasticsearch是用Java开发的，并作为Apache许可条款下的开放源码发布，是当前流行的企业级搜索引擎。设计用于云计算中，能够达到实时搜索，稳定，可靠，快速，安装使用方便。  
  
#### Elasticsearch的优势  
>学习门槛低，开发周期短，上线快  
性能好，查询快，实时展示结果  
迅速扩容，快速支撑增长迅猛的数据量  
  
#### 与solr对比  
分布式搜索引擎，基于Lucene进行开发，同类产品还有Solr。  
当单纯的对已有数据进行搜索时，Solr更快。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%AF%B9%E6%AF%941.jpeg)  
  
当实时建立索引时，Solr会产生io阻塞，查询性能较差，Elasticsearch具有明显的优势。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%AF%B9%E6%AF%942.jpeg)  
  
随着数据量的增加，Solr的搜索效率会变得更低，而Elasticsearch却没有明显的变化。
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%AF%B9%E6%AF%943.jpeg)  
  
将搜索引擎从Solr转到Elasticsearch以后的平均查询速度有了50倍的提升。
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%AF%B9%E6%AF%944.jpeg)  
  
### Lucene的工作原理  
  
>1.Lucene 是一个 JAVA 搜索类库，它本身并不是一个完整的解决方案，需要额外的开发工作。  
2.Document文档存储、文本搜索。  
3.Index索引，聚合检索。(在Elasticsearch中Index类似数据库的概念)  
4.Analyzer分词器，如IKAnalyzer、word分词、Ansj、Stanford等。  
5.大数据搜索引擎解决方案原理  
6.NoSQL的兴起(Redis、MongoDB、Memecache)  
  
#### Lucene的基本原理  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%9F%BA%E6%9C%AC%E5%8E%9F%E7%90%86.jpeg)  
  
### 关系型数据库和Elasticsearch的操作对比  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%95%B0%E6%8D%AE%E5%BA%93%E5%AF%B9%E6%AF%94.jpeg)  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%93%8D%E4%BD%9C%E5%AF%B9%E6%AF%94.jpeg)  
  
### Elasticsearch分布式特性  
  
es支持集群模式，是一个分布式系统，其好处主要有两个:   
>增大系统容量，如内存，磁盘，使得es集群可以支持PB级的数据。  
提高系统可用性，即使部分节点停止服务，整个集群依然可以正常服务。  
  
es集群由多个es实例组成  
>不同集群通过集群名字来区分，可通过cluster.name来进行修改，默认为elasticsearch。  
每个es实例本质上是一个JVM进程，且有自己的名字，可以通过node.name来进行修改。  
  
#### 节点启动  
运行如下命令可以快速启动一个es节点的实例。
```
bin/elasticsearch
-Ecluster.name=my_cluster -Epath.data=my_cluster_node1 -Enode.name=node1 -Ehttp.port=5100 -d  
```
  
Cluster State  
es 集群相关的数据称为 cluster state,主要记录如下信息:  
>节点信息，比如节点名称、链接地址等。  
索引信息，比如索引名称、配置等。  
  
Master Node  
>1.可以修改cluster state的节点称为master节点，一个集群只能有一个。  
2.cluster state存储在每个节点上，master维护最新版本并同步给其他节点。  
3.master 节点是通过集群中所有节点选取产生的，可以被被选举的节点称为master-eligible节点 ，相关配置:node.master:true。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/master%E8%8A%82%E7%82%B9.jpeg)  
  
创建一个索引   
```
PUT test_index
```
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/coordinatingj%E8%8A%82%E7%82%B9.jpeg)  
  
coordinating节点  
处理请求的节点称为coordinating节点，该节点为所有节点的默认角色，不能取消。  
路由请求到正确的节点处理，比如创建索引的请求到master节点。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/coordinating%E8%8A%82%E7%82%B9.jpeg)  
  
Data Node  
存储数据的节点称为 data节点，默认节点都是data类型，相关配置如下:  
```
node.data:true
```
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%95%B0%E6%8D%AE%E8%8A%82%E7%82%B9.jpeg)  
  
#### 单点问题  
从上面的图中可知，如果node1停止服务，集群就停止服务。  
  
新增节点  
```
bin/elasticsearch
-Ecluster.name=my_cluster -Epath.data=my_cluster_node2 -Enode.name=node2 -Ehttp.port=5200 -d
```
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%9B%86%E7%BE%A4.jpeg)  
  
### 副本与分片  
  
提高可用性  
如下图所示，node2上是test_index的副本。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%89%AF%E6%9C%AC.jpeg)  
  
#### 数据扩容  
如何将数据分布到所有节点上？引入分片(Shard)解决问题。  
分片是es支持PB级数据的基石。  
>分片存储了部分数据，可以分布于任意节点上。  
分片数在索引创建时指定且后续不允许再更改，默认为5个。  
分片有主分片和副本分片之分，以实现数据的高可用。  
副本分片的数据由主分片同步，可以有多个，从而提高读取的吞吐量。 
  
#### 分片   
  
增加节点是否能够提高test_index的数据容量吗?  
不能，因为只有3个分片，已经分布到3个节点上，新增的节点无法利用。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%A4%9A%E8%8A%82%E7%82%B9.jpeg)  
  
增加副本数是否能提高test_index的读取吞吐量呢?  
不能，因为新增的副本是分布在3个节点上，还是利用了同样的资源， 如果要增加吞吐量，还需要增加节点。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%A4%9A%E5%88%86%E7%89%87.jpeg)  
  
分片数的设定非常重要，需要提前规划好
>分片数太少，导致后续无法通过增加节点实现水平扩容。  
分片数过大，导致一个节点上分布多个分片，造成资源浪费，同时会影响查询性能。  
  
### 集群状态  
通过如下api可以查看集群健康状况，包括以下三种:  
>green 健康状态，指所有主副分片都正常分配。  
yellow 指所有主分片都正常分配，但是有副本分片未正常分配。  
red 有主分片未分配。  
  
#### 故障转移  
集群由3个节点组成，如下所示，此时集群状态是green。
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E7%BB%BF%E8%89%B2.jpeg)  
  
node1所在机器宕机导致服务终止，此时集群会如何处理?  
1.node2和node3发现node1无法响应一段时间后会发起master选举，比如这里选举node2为master节点，此时由于主分片P0下线，集群状态变为red。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E7%BA%A2%E8%89%B2.jpeg)  
  
2.node2发现主分片P0未分配，将R0提升为主分片。此时由于所有主分片都正常分配，集群状态变为yellow。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%BB%84%E8%89%B2.jpeg)  
  
3.node2发现主分片P0和P1生成新的副本，集群状态变为green。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%81%A2%E5%A4%8D.jpeg)  
  
#### 脑裂问题  
脑裂问题，英文为split-brain，是分布式系统中的经典网络问题，如下图所示:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E8%84%91%E8%A3%82.jpeg)  
  
node2与node3会重新选举master，比如node2成为了新master，此时会更新cluster state node1自己组成集群后，也会更新cluster state。  
同一个集群有两个master，而维护不同的cluster state，网络恢复后无法选择正确的master。  
  
解决方案为仅在可选举master-eligible节点数据大于等于quorum时才可以进行master选举。  
quorum = master-eligible 节点数/2 + 1，例如 3个master-eligible节点时，quorum为2。  
discovery.zen.mininum_master_nodes为quorum即可避免脑裂。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E8%84%91%E8%A3%82%E8%A7%A3%E5%86%B3.jpeg)  
  
### 分布式存储  
  
文档最终会存储在分片上，如图所示:  
假设Doc1最终存储在分片P1上。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%88%86%E5%B8%83%E5%BC%8F%E5%AD%98%E5%82%A8.jpeg)  
  
Document1是如何存储到分片P1的呢？选择P1的依据是什么呢？  
需要文档到分片的映射算法，使得文档均匀分布在所有分片上，以充分利用资源。  
但是随机选择或者round-robin算法会导致维护文档到分片的映射关系，成本巨大，所以不可取。  
  
es通过如下公式计算文档对应的分片  
```
shard = hash(routing) % number_of_primary_shards 
```
>hash算法保证可以将数据均匀地分散在分片中。  
routing是一个关键参数，默认是文档id，也可以自行指定。  
number_of_primary_shards 是主分片数。  
  
该算法与主分片数相关，这也是分片数一旦确定后便不能更改的根本原因。  
  
#### 文档创建的流程  
>1.Clinet向node3发起创建文档的请求  
2.node3通过routing计算该文档应该存储在Shard1上，查询cluster state后确认主分片P1在node2上，然后转发创建文档的请求到node2  
3.P1接收并执行创建文档的请求后，将同样的请求发送到副本分片R1  
4.R1接收并执行创建文档请求后，通知P1成功的结果  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%86%99%E5%85%A5%E5%8E%9F%E7%90%86.jpeg)  
  
#### 文档读取的流程  
>1.Clinet向node3发起创建文档的请求。  
2.node3通过routing计算该文档应该存储在Shard1上，查询cluster state后获取 Shard 1的主副分片列表，然后以轮询的机制获取一个shard，比如这里是R1，然后转发读取文档的请求到node1。  
3.R1接收并执行创建文档的请求后，将结果返回给node3。  
4.node3返回结果给Client。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E8%AF%BB%E5%8F%96%E5%8E%9F%E7%90%86.jpeg)  
  
### 倒排索引  
  
#### 倒排索引不可更改  
倒排索引一旦生成，不能更改。  
  
好处:  
>1.不用考虑并发写文件的问题，杜绝了锁机制带来的性能问题。  
2.由于文件不再更改，可以充分利用文件系统缓存，只需要载入一次，只要内存足够，对该文件的读取都会从内存读取，性能高。  
3.利于生成缓存数据。  
4.利于对文件进行压缩存储，节省磁盘和内存存储空间。  
  
坏处:  
>写入新文档时，必须重新构建倒排索引文件，然后替换老文件后，新文档才能被检索，导致文档实时性受到影响。  
  
解决方案:
>新文档直接生成新的倒排索引文件，查询的时候同时查询所有的倒排文件，然后对查询结果做汇总计算即可。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E5%80%92%E6%8E%92%E7%B4%A2%E5%BC%95.jpeg)  
  
#### 倒排索引的实时性  
Lucene采用了这种方案，它构建的单个倒排索引称为segment，合在一起称为Index，与ES中的Index概念不同。ES中的一个Shard对应一个Lucene Index。  
Lucene会有一个专门的文件来记录所有的segment信息，称为Commit Point。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/segment.jpeg)  
  
refresh  
segment写入磁盘的过程依然很耗时，可以借助文件系统缓存的特性，先将segment在缓存中创建并开放查询来进一步提升实时性，该过程在es中被称为refresh。  
在refresh之前文档会先存储在一个buffer中，refresh时将buffer中的所有文档清空并生成segment。  
es默认每1秒执行一次refresh，因此文档的实时性被提高到1秒，这也是es被称为近实时(Near Real Time)的真正原因。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/buffer.jpeg)  
  
translog  
如果在内存中的segment还没有写入磁盘前发生了宕机，那么内存中的文档就无法恢复了。那么如何解决这个问题呢?  
es引入translog机制。写入文档到buffer时，同时将该操作写入translog。  
translog 文件会即时写入磁盘(fsync)，6.x默认每个请求都会落盘，可以修改为每5秒写一次，这样风险便是丢失5秒内的数据，相关配置为index.translog.*   
es重新启动时会自动检查translog文件，并从中恢复数据。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/translog.jpeg)  
  
flush  
flush负责将内存中的segment写入磁盘，主要做如下的工作:  
>将translog写入磁盘。  
将index buffer清空，其中的文档生成一个新的segment，相当于一个refrsh操作。  
更新commit point并写入磁盘。  
执行fsync操作，将内存中的segment写入磁盘。  
删除旧的translog文件。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%9B%B4%E6%96%B0%E6%95%B0%E6%8D%AE.jpeg)  
  
refresh发生的时机主要有以下几种情况:  
>间隔时间达到时，通过index.settings.refresh_interval来设定，默认是1秒。  
index.buffer占满时，其大小通过indices.memory.index_buffer_size设置，默认为jvm heap的10%，所有shard共享。  
flush发生时也会发生refresh。  
  
segment的删除与更新  
segment一旦生成就不能更改，那么如果要删除文档该如何操作?  
lucene会专门维护一个.del的文件，记录所有已经删除的文档，注意.del上记录的是文档在Lucene的内部id。  
在查询结果返回前会过滤掉.del中所有的文档。  
  
更新文档如何进行呢?  
首先删除文档，然后再创建新的文档。  
  
ES Index 与 Lucene Index的术语对照如下所示:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E6%80%BB%E8%A7%88.jpeg)  
  
segment merge  
随着segment的增多，由于一次查询的segment数增多，查询速度会变慢。  
es会定时在后台进行segment merge的操作，减少segment的数量。  
通过force_merge api可以手动强制做segment merge的操作。  
  
### Search的运行机制  
  
Search执行的时候实际分两个步骤运行的  
>Query阶段  
Fetch阶段  
  
#### Query阶段  
>1.node3在接收到用户的search请求后，先会进行Query阶段(此时Coordinating Node角色)。  
2.node3在6个主副分片中随机选择3个分片，发送search request。  
3.被选中的3个分片会分别执行查询并排序，返回from+size个文档Id和排序值。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/Query.jpeg)  
  
#### Fetch阶段  
node3根据Query阶段获取到文档Id列表对应的shard上获取文档详情数据  
>1.node3向相关的分片发送multi_get请求。  
2.3个分片返回文档详细数据。  
3.node3拼接返回的结果并返回给用户。  
  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/Fetch.jpeg)  
  
### 相关性算分  
  
es采用TF/IDF算法进行相关性分数的计算，同时term的长度也会影响到分数的计算。  
  
相关性算分在shard与shard间是相互独立的，也就意味着同一个term的IDF等值在不同shard上是不同的。文档的相关性算法和它所处的shard相关在文档数量不多时，会导致相关性算分严重不准的情况发生。  
解决思路有两个:   
>1.设置分片数为1个，从根本上排除问题，在文档数量不多的时候可以考虑该方案，比如百万到千万级别的文档数量。  
2.采用DFS Query-then-Fetch的查询方式。  
  
### 附近的人demo  
  
#### Elasticsearch下载  
下载地址：https://www.elastic.co/downloads/past-releases (建议下载统一的ELK版本以确保最高的稳定性，此时的最新版本6.5.1)  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/downloades.jpeg)  
  
下载完毕后直接解压即可。  
  
#### 参数配置  
找到config文件夹中的jvm.options文件，我们可以设置参数  
```
 -Xms4g  
 -Xmx4g  
 ```
 ![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/JVM.jpeg)  
   
Elasticsearch非常的吃内存，可以将这两个参数设置的大一些。  
  
#### 分布式环境准备  
将下载好的Elasticsearch拷贝为一共三份(为集群)，一个作为master节点，两个作为slave节点(如上图)  
修改主节点的配置，打开 elasticsearch-6.5.1-master\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/master.jpeg)  
  
配置 slave-1 节点，打开 elasticsearch-6.5.1-slave-1\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/slave1.jpeg)  
  
配置 slave-2 节点，打开 elasticsearch-5.5.1-slave-2\config 下的 elasticsearch.yml 文件，在底部追加如下内容:  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/slave2.jpeg)  
  
使用 ./elasticsearch分别启动三个节点。

#### 实现可视化   
可视化插件安装  
下载 NodeJS 环境，打开官网 https://nodejs.org/en/download/  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/NodeJS.jpeg)  
  
根据系统选择相应版本，mac下载后直接安装即可。可以输入 node -v 检查 node 是否安装成功。  
  
下载 elasticsearch-head  
打开 https://github.com 搜索 elasticsearch-head 关键字，选择 mobz/elasticsearch-head，并下载 elasticsearch-head-master.zip 包解压。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/eshead.jpeg)  
  
修改 master 节点的跨域配置，在 elasticsearch.yml 中追加以下内容。  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E8%B7%A8%E5%9F%9F.jpeg)  
  
启动elasticsearch-head  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/starteshead.jpeg)  
  
#### 结果展示  
附近的人分片展示  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E5%88%86%E7%89%87%E5%B1%95%E7%A4%BA.jpeg)  
  
附近的人数据展示  
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E6%95%B0%E6%8D%AE%E5%B1%95%E7%A4%BA.jpeg)  
  
附近的人查询结果
![](https://github.com/YufeizhangRay/image/blob/master/elasticsearch/%E9%99%84%E8%BF%91%E7%9A%84%E4%BA%BA%E6%9F%A5%E8%AF%A2%E7%BB%93%E6%9E%9C.png)  
[返回顶部](#elasticsearch-practice)
