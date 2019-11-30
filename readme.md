
# 基于*百度云推送*的android端cordova推送插件说明

-----

1. 编译环境
  * android8.0 
  * sdk 
  * ndk 项目内部使用了基于C语言的动态so库 需要ndk编译

2. 使用说明
    
 1. 安装插件
    `ionic cordova plugin add [插件相对地址] --variable API_KEY=xxxx --variable PACKAGE_NAME=xxx`
 2. 使用插件   
    ionic 使用外部cordova插件有两种方式 *ionic-native* *直接使用*
    这里介绍直接使用方式 需在app deviceready 回调中使用。
    可以用index.txt 生成ts文件集成进项目使用

    ```
    declare var cordova: any;
    declare var window: any;

    cordova.plugins.cordovaNotify.init({
        group: 'domainId',
        unique: 'userId'
    }, function(data){
        // 初始化成功会返回字符串 init success

    }, function(){
        // 失败调用
    });

    window.addEventListener('onNoticeArrived', function(data) {

        // data 数据格式为 {title,content, extra} title 标题，Content 内容，extra 额外信息跟后端custom_content对应

    }, function(){
        // 失败调用
    });

    ```

    3. plugin.xml 配置 [百度云推送](http://push.baidu.com/doc/android/api '百度云推送') 
    百度云推送控制台现在不允许创建新应用，大家可以在百度地图中创建应用，创建后自动回出现在百度云推送.

