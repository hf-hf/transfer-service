服务器IP： xxx.xxx.xxx.xx

端口号： 6002

### 第一步 绑定服务器
02 <font color=red>31 32 33 34 35 36 37<font>

起始位02  红色的是MAC地址 随意绑定一个 

### 第二步 数据发送（用另外一个网络调试助手链接后发送）

03 07 00 03 31 32 33 34 35 36 37 01 02 03

07 MAC长度  31 32 33 34 35 36 37是目的Mac

00 03 数据长度

01 02 03 是发送的数据

采集设备绑定 36 36 36 36 37 37 37

服务器绑定 37 37 37 37 36 36 36 

### 状态校验
设备发送报文 20 20 20 20
检测成功回复 21 21 21 21


