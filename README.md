# fastur-plugin (fast your plugin development)
**不用重启idea, 直接右键运行你的代码**
**no need to restart you idea, just run your code when u select context menu of "run you code"**

# 内部执行流程
>* rebuild当前整个项目, 生成class文件
>* 右键运行, 会自动根据当前项目的类路径(包含jar文件, 以及编译src,test目录下的所有class文件
>* class文件是通过内部的代码直接执行的, 如果你了解开发idea的插件, 你的代码相当于是直接在自己的插件中运行的, 不会启动新的进程执行的
>* 如果代码中需要使用到idea中的类, 可以直接使用
>* 运行结果会直接显示在控制台中
>* 参数可有可无, 目前支持的参数如下, 没有顺序要求, 不在下面参数列表中的类型, 都会默认为null
 - AnActionEvent event: 当前事件对象
 - Project project: 当前的项目对象
 - String projectName: 当前项目名称
 - String projectPath: 当前项目的全路径
 - Editor editor: 当前的编辑器对象
 - String filepath: 当前文件的全路径
 - String filename: 当前运行的文件名称, 带有后缀
 - <任意类型> <任意名称>: 其他类型, 默认为nul,
 
**注意,不能设置原始类型, 如int, float等, 否则直接报错**
