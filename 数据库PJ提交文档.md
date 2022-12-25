# 数据库PJ提交文档

### 1.数据库ER图

![ER图.jpg](https://s2.loli.net/2022/12/25/x4Tf9aIUBzqPRQm.jpg)

#### 特殊设计注明

##### ①隔离

不仅仅是每个仓库互不相干，每个分支之间也互不相干，如果分支之间有相同的**commit**，会重复扫描匹配入库，从ER图可以看出，我们设计时把**commit**和**branch**设计为**多对一**，其实就体现了隔离性，这里其实是为增量做了一点铺垫，不同分支虽然有相同**commit**，但是增量变化情况是可能不一样的。

##### ②增量

数据入库时，除了第一个**commit**会把所有扫描结果全部入库，其他**commit**会把扫描结果和上一个**commit**进行对比。从而得到本次**commit**的所有**issue_instance**

根据匹配的情况，**issue_instance**一共有**APPEAR**、**UPDATE**、**DISAPPEAR**三种情况

##### ③没有在数据库层面维护commit的前后版本关系

我们的ER模型中并没有定义一个**parent/child**关系来刻画**commit**的前后版本关系。而是在应用层按时间排序得到某个**branch**的所有**commit**的前后关系。

### 2.数据库表结构

不带注释且每一句sql语句挤在一行的版本参见**/sqlfile/create_table.sql**

下面是美化格式后的建表语句，和ER图重复的部分不再注释

```sql
CREATE TABLE IF NOT EXISTS repository (
    repository_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(100) NOT NULL,
    url VARCHAR(200) NOT NULL
);
CREATE TABLE IF NOT EXISTS branch (
    branch_id INT PRIMARY KEY AUTO_INCREMENT,
    repository_id INT(100) NOT NULL, //体现了ER图中的Has关系，一对多，在多端维护一个外键
    name VARCHAR(30) NOT NULL,
    description VARCHAR(100) NOT NULL,
    foreign key(repository_id) references repository(repository_id) 
);
CREATE TABLE IF NOT EXISTS git_commit (
    branch_id INT NOT NULL,  //体现了ER图中的belong_to关系，一对多，在多端维护一个外键
    commit_id INT PRIMARY KEY AUTO_INCREMENT,
    hash_val VARCHAR(100) NOT NULL,
    commit_time DATETIME NOT NULL,
    committer VARCHAR(50) NOT NULL,
    foreign key(branch_id) references branch(branch_id), 
    INDEX commit_index_on_time (commit_time)   //索引，加快按时间查找commit的速度
);
CREATE TABLE IF NOT EXISTS issue_case (
    issue_case_id INT PRIMARY KEY AUTO_INCREMENT,
    appear_commit_id INT,                      //体现了ER图中的appear关系，一对多，在多端维护一个外键
    solve_commit_id INT,                       //体现了ER图中的solve_in关系，一对多，在多端维护一个外键
    case_status ENUM ('SOLVED','UNSOLVED'),
    type ENUM ('BUG','SMELL','VULN','SECHOT'),
    appear_time DATETIME,                      //冗余设计，为了便于业务实现
    appear_committer VARCHAR(50),              //冗余设计，为了便于业务实现
    solve_time DATETIME,                       //冗余设计，为了便于业务实现
    solve_committer VARCHAR(50),               //冗余设计，为了便于业务实现
    foreign key(appear_commit_id) references git_commit(commit_id),
    foreign key(solve_commit_id) references git_commit(commit_id),
    INDEX case_index_on_appear_id (appear_commit_id),  //索引，加快按出现commit查找缺陷的速度
    INDEX case_index_on_solve_id (solve_commit_id)     //索引，加快按解决commit查找缺陷的速度
);
CREATE TABLE IF NOT EXISTS issue_instance (
    issue_instance_id INT PRIMARY KEY AUTO_INCREMENT,
    issue_case_id INT,                       //体现了ER图中的about关系，一对多，在多端维护一个外键
    commit_id INT(20),                       //体现了ER图中的detect_in关系，一对多，在多端维护一个外键
    instance_status ENUM ('APPEAR','UPDATE','DISAPPEAR'),
    file_path VARCHAR(200) NOT NULL,
    message VARCHAR(300),
    foreign key(issue_case_id) references issue_case(issue_case_id),
    foreign key(commit_id) references git_commit(commit_id),
    INDEX instance_index_on_commit_id (commit_id)  
);
CREATE TABLE IF NOT EXISTS issue_location (
    issue_instance_id INT,                    //弱实体集中要记录其从属的实体集的主键
    sequence INT,start_line INT NOT NULL,
    end_line INT NOT NULL,
    primary key(issue_instance_id,sequence),  //弱实体集的主键应该是其从属的实体集的主键加上其序号
    foreign key(issue_instance_id) references issue_instance(issue_instance_id) 
);

```

#### 特殊设计注明

##### ①冗余

在**issue_case**表中，我们额外存储了四个信息：出现时间、引入者、解决时间、解决者。这些信息本来是可以通过**appear_commit_id**和**solve_commit_id**找到的对应的commit然后获取到的，但是这额外的一步会比较麻烦，需要使用**join**，降低性能，而且涉及的也是核心功能，所以为了业务实现的高效和便捷，设计了这里的冗余。

### 3.需求分析

具体参见==**需求文档**==，这里简单说明有哪些功能

#### ①按照commit查看缺陷情况（对应CommitService）

看某个**commit**缺陷的**累积情况**和**增量情况（引入和消除）**

最新版本中，静态缺陷数量的**分类统计**，以及相关信息列表 。

– 按类型统计 

– 详情按存续时长**排序** 

– 按类型统计存续时长的平均值和中位值

#### ②查看指定一段时间内缺陷情况（对应TimeService）

看这段时间的**增量情况（引入和消除）**

一样是分类统计

#### ③指定人员查看相关缺陷引入和解决情况（对应CommitterService）

给定开发人员，查看该人员引入缺陷、解决他人引入缺陷、自己引入且未解决缺陷、自己引入且被他人解决缺陷。该需要要求**既可以看总体也可以看具体某一类缺陷**的这些信息。

PS：业务处理过程中还会顺便计算一些统计信息，比如存续时间平均数，中位数之类的。



### 4.程序源码和可执行文件

源码参见**Project**文件夹，可以直接打开为**IDEA**项目

可执行文件为

### 5.测试数据准备说明

准备了两个仓。

一个是自己准备的，数据量较小，仅有百条issue，不到10个commit，两个分支。主要用于实现基本功能时进行测试，并且由于其体量小，导入速度快，故用于测试多分支的导入。（SonarTest）

一个是Github上的开源仓库，其数据量大，有10^4量级的issue，300多个commit，4个分支。用于最终系统级功能的测试。

### 6.对系统级功能的尝试

#### 6.1事务

主要是在导入时可以使用事务，大致可以分两种思路

①在导入每个commit的扫描结果的时候会开启事务，等这个commit的所有扫描结果都执行入库的sql语句后再commit

②把这个仓库(or分支)的整个扫描过程看作一个事务

我们在Connect类中封装了和数据库的连接（即JDBC中的Connection类的实体，主要目的是把它变成一个全局变量，方便各个地方使用），把事务的**开启**、**提交**、**回滚**进行了简单封装,并在导入数据的时候使用。

```Java
public static void startTransaction() throws SQLException {
    connection.setAutoCommit(false);
}

public static void commit() throws SQLException {
    connection.commit();
    connection.setAutoCommit(true);
}
public static void rollBack(){
    try {
        connection.rollback();
        connection.setAutoCommit(true);
    } catch (Exception e){
        //...
    }
}
```

#### 6.2 存储能力分析

我们的项目中给出了一个github里找的仓库，单扫描完这个仓库最后单表的数据量最大为**68891**条数据(是**issueInstance**，与此同时**issueCase**表中有**33434**条记录)。

#### 6.3 查询性能分析（已使用索引优化）

##### 6.3.1 针对各个业务的第一次运行的性能分析

**CommitService**: 从下图可以看出，非IO耗时为 493ms，主要是读取数据库耗时。(后面对使用和不使用索引进行比较的时候，去掉了一些应用层计算，只保留读取数据库，耗时为440ms)

<a href="https://sm.ms/image/NEojVKk1Pf8mbtl" target="_blank"><img src="https://s2.loli.net/2022/12/25/NEojVKk1Pf8mbtl.png" height = 400px></a>

**TimeService**: 从下图可以看出，非IO耗时为 418ms，主要是读取数据库耗时(后面对使用和不使用索引进行比较的时候，去掉了一些应用层计算，只保留读取数据库，耗时为333ms)

<a href="https://sm.ms/image/h9QNbB6kKay1dxL" target="_blank"><img src="https://s2.loli.net/2022/12/25/h9QNbB6kKay1dxL.png" height = 400px style="zoom: 150%;" ></a>

**CommitterService**: 从下图可以看出，非IO耗时为 187ms。标红的地方主要是体现了对应的缺陷需要**按照存续时长排序**。

<a href="https://sm.ms/image/HTnPUYXOMV8s1AI" target="_blank"><img src="https://s2.loli.net/2022/12/25/HTnPUYXOMV8s1AI.png" height=400px></a>

##### 6.3.2 数据库缓冲机制优化重复查询

查阅资料发现，MySQL有两个类似缓存的机制：缓冲池和查询缓存（Query Cache），其中后者在MySQL 8.0以上版本已经不再使用了，但是缓冲池还是在的，它会起到类似缓存的效果，当我们短期重复查询时，速度会加快，下面是一组简单的对比：

重启进程，第一次运行 CommitService，查看最新的commit的缺陷引入、消除和累积情况

<img src="https://s2.loli.net/2022/12/25/SzyqGab9tCVspDx.png" alt="第一次运行.png" style="zoom:50%;" />

第二次运行 CommitService，查看最新的commit的缺陷引入、消除和累积情况

<img src="https://s2.loli.net/2022/12/25/zEdIfWYoMw5clRV.png" alt="第二次运行.png" style="zoom:50%;" />

第三次运行 CommitService，查看最新的commit的缺陷引入、消除和累积情况

<img src="https://s2.loli.net/2022/12/25/oPF7WwnI1ChaOSA.png" alt="第三次运行.png" style="zoom:50%;" />

结论：MySQL缓冲池机制会优化重复查询的性能。并且多次运行会把优化加速比提升得更高。



#### 6.3尝试使用索引优化查询性能

首先是修改建表语句，在创建数据库表结构的时候，建立相关的索引。结合我们的业务，我们在：

**git_commit**的表中的**commit_time**属性上建立了索引。这样方便按照时间查找

**issue_case**的表中的**appear_commit_id**和**solve_commit_id**属性建立了索引

**issue_instance**的表中的**commit_id**属性建立了索引

为什么没有对**committer_service**的业务进行相应的对照，因为看了下这个业务的sql语句，and条件比较多，查询速度受到的限制因素比较多，不易于分析

然后进行对比，我们写了一个**IndexService**模拟对应的业务，不打印输出，尽量只运行数据库的sql语句，然后分别对比使用和不使用索引消耗的时间。

我们在代码中模拟**CommitService**和**TimeService**中的行为，分别使用和不使用索引，对比性能

**CommitService**：

<a href="https://sm.ms/image/5sgHJAxWLz3yctF" target="_blank"><img src="https://s2.loli.net/2022/12/25/5sgHJAxWLz3yctF.png" height= 350px style="zoom:150%;" ></a><a href="https://sm.ms/image/Qi9uvK47k2drgcE" target="_blank"><img src="https://s2.loli.net/2022/12/25/Qi9uvK47k2drgcE.png" height = 350px style="zoom:150%;" ></a>

440ms VS 4193ms，可见，使用索引所花费时间约是不使用索引花费时间的十分之一，效果十分好。

**TimeService**:

<a href="https://sm.ms/image/gfmcxoAlbN4rdB2" target="_blank"><img src="https://s2.loli.net/2022/12/25/gfmcxoAlbN4rdB2.png" height= 400px></a>

<a href="https://sm.ms/image/xRUBIgilaO7LNvJ" target="_blank"><img src="https://s2.loli.net/2022/12/25/xRUBIgilaO7LNvJ.png" height=400px></a>

333ms VS 4352ms，可见，使用索引所花费时间约是不使用索引花费时间的1/14，效果十分好。





### 7.对项目的思考

##### 功能的实现是灵活的，不一定非得通过SQL语句实现

有的功能可以放在应用层去实现，放在应用层的好处是：

1.设计数据库表结构的时候可以少考虑一点因素，表结构更自然

2.避免书写太复杂的SQL语句

3.对数据库具体实现不太清楚时，复杂SQL语句的性能可能不太好（可能有重复的扫描），交给应用层实现我们可以用最直接的逻辑实现业务。

放在应用层的主要坏处是要写更多的业务代码，整体代码量会更大，代码逻辑变得更复杂一点。

比如，对于我们的项目：

1.我们并没有在数据库中维护commit之间的前后关系，而是在应用层抽取出某个branch的所有commit之后，按提交时间排序得到commit顺序

2.比如“查询某个版本目前仍然存续的缺陷”这一功能，这个功能应该也是可以通过SQL语句实现的，但是会比较复杂，我们也交给了应用层实现，从第一个commit开始逐个分析缺陷的存续情况，因为我们使用的增量的形式，复杂度还是可以接受的。





