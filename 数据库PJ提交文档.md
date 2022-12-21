# 数据库PJ提交文档





### 6.对系统级功能的尝试

#### 6.1事务

我们在导入每个commit的扫描结果的时候会开启事务，等这个commit的所有扫描结果都执行入库的sql语句后再commit

我们在Connect类中封装了和数据库的连接（即JDBC中的Connection类的实体，主要目的是把它变成一个全局变量，方便各个地方使用），把事务的开启、提交、回滚进行了简单封装。

```Java
try{
    Connect.startTransaction();
    //入库
    Connect.commit();
}catch(Exception e){
    e.printStackTrace();
    Connect.rollBack();
}

```

#### 6.2 存储能力分析

我们的项目中给出了一个demo仓库，扫描完这个仓库最后单表的数据量最大为==xxx==

#### 6.3 查询性能分析（已使用索引优化）

##### 6.3.1 针对各个业务的第一次运行的性能分析





##### 6.3.2 数据库缓冲机制优化重复查询

查阅资料发现，MySQL有两个类似缓存的机制：缓冲池和查询缓存（Query Cache），其中后者在MySQL 8.0以上版本已经不再使用了，但是缓冲池还是在的，它会起到类似缓存的效果，当我们短期重复查询时，速度会加快，下面是一组简单的对比：

第一次

第二次

第三次







#### 6.3尝试使用索引优化查询性能

首先是修改建表语句，在创建数据库表结构的时候，建立相关的索引。结合我们的业务，我们在：

git_commit的表中的==commit_time==属性上建立了索引。这样方便按照时间查找

issue_case的表中的==appear_commit_id==和==solve_commit_id==属性建立了索引

issue_instance的表中的==commit_id==属性建立了索引

为什么没有对committer_service进行响应的对照，因为看了下这里的sql语句，and条件比较多，查询速度受到的限制因素比较多，不易于分析

然后进行对比，我们写了一个IndexService模拟直接的业务，只是不打印输出，然后分别对比使用和不使用索引消耗的时间。

我们在代码中模拟CommitService和TimeService中的行为，分别使用和不使用索引，对比性能

#### 6.4是否可以优化SQL语句的写法







