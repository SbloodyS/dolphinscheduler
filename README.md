后端本地启动:
启动类 org.apache.dolphinscheduler.server.StandaloneServer


前端本地启动:
npm install
npm start


整体编译命令:
mvn -Dmaven.test.skip=true -DskipTests clean install -Prelease


使用到的 Maven 仓库(需要翻墙):
https://maven.aliyun.com/repository/central
https://maven.aliyun.com/repository/apache-snapshots
https://repository.cloudera.com/artifactory/cloudera-repos/
https://repo1.maven.org/maven2/

