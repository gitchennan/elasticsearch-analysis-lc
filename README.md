# elasticsearch-analysis-lc
Lc Analyzer for ElasticSearch

1.中文分词
2.支持自定义词库,提供elasticsearch rest api: /_lc/reload 用于重加载自定义词库
3.支持中文分词词性标注
4.支持命名实体识别,包括:中国人名、地名、机构名、音译英文人名、日本人名
5.支持停用词
6.支持同义词,支持同义词通过es rest api重加载

插件安装好启动ES后会创建一个 .custom-dictionary 的索引用于动态PUT自定义词。
PUT完成后通过rest api调用 /_lc/reload 后即可生效

