- 2017-3-16 19:28：

1、将BlockCanary改造成Bfc的一个子库；
2、将blockcanary-analyzer的代码合并到BfcBlockCanarySdk，方便对代码的管理；
3、代码取自leakcanary的1.5.0版本；
4、解决API版本必须要在21以以上才能引用该库的问题；
5、解决跑monkey过程中因为误点击BlockCanary界面的Delete按钮删掉内存泄露详情的问题；
6、将内存泄露信息保存在磁盘的“blockcanary/应用包名/卡顿时间.txt"文件下，方便查看详细的内存泄露信息。

- 2017-3-17 08:34：

1、版本号改为3.0.0;