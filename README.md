cache-control基于maven插件的缓存控制工具，通过修改资源url的请求参数，比如在url后面添加版本号或者时间戳的形式，来有效的防止浏览器缓存。

目前该功能可用于避免js、css、image 三种文件类型缓存



用法：

1.添加插件asset-cache-control 到pom文件中：



<build>
    <plugins>
           <plugin>
                <groupId>org.wangyl</groupId>
                <artifactId>cache-control</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>cache</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <edition>${project.version}</edition>
                </configuration>
            </plugin>
    </plugins>
</build>

其他配置：


suffixs：动态模板后缀，可选，允许多个，用于指定哪些后缀的动态模板可以做打版本号操作，默认支持jsp、html、htm、ftl，如果填写则覆盖默认文件后缀，只会处理填写的文件后缀

version：版本号，可选，用于指定给静态资源url添加的版本号值，如果为空，则打上当前时间戳，也可以指定${project.version}即可

webappDir:webapp根目录,可选,默认为src/main/webapp，如果不是当前目录，则需要指定

pageDirs:webapp目录下，动态模板的目录。可选 ，允许多个 。默认：/ 即webapp目录

resourcesDirs：资源目录，允许多个。如果指定，则只打webappDir/resourcesDir目录下面的文件到静态资源包中

2.执行命令： 执行maven命令，用来替换工程中所有的动态文件中引用的静态资源URL路径。



打版本命令： mvn clean package



打静态资源到独立war包命令：mvn asset-cache-control:package

该命令会自动添加版本号或者时间戳到静态资源URL后面，自动添加静态资源域名在url前面（如果有配置静态资源域名），例如 ：



原始：

<script type="text/javascript" src="/javascripts/jquery-1.10.2.min.js"></script>

<link href="/css/bootstrap.min.css" rel="stylesheet">

执行后效果：



版本号模式(在版本号不变更的情况下发版，需要刷新浏览器端缓存，所以版本号的规则是"${project.version}"，每次构建出的v参数值都是不一样的)


<script type="text/javascript" src="http://res.github.com/javascripts/jquery-1.10.2.min.js?v=1.1.0-2543d"></script>
<link href="http://res.github.com/css/bootstrap.min.css?v=1.1.0-2543d" rel="stylesheet">
