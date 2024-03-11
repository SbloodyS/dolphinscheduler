#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Supported values: ``postgresql``, ``mysql`, `h2``.
DATABASE_TYPE=mysql

# Spring datasource url, following <HOST>:<PORT>/<database>?<parameter> format, If you using mysql, you could use jdbc
# string jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8 as example
SPRING_DATASOURCE_URL="jdbc:mysql://172.16.92.179:15506/ds_test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8"

# Spring datasource username
SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-"ds"}

# Spring datasource password
SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-"j54KN6q64riUdBzR"}

export HADOOP_HOME=/opt/cloudera/parcels/CDH/lib/hadoop
export HADOOP_CONF_DIR=/opt/cloudera/parcels/CDH/lib/hadoop/etc/hadoop
#export SPARK_HOME1=/opt/soft/spark1
#export SPARK_HOME2=/opt/soft/spark2
export PYTHON_HOME=/usr/bin/python3
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.392.b08-2.el7_9.x86_64/jre
export HIVE_HOME=/opt/cloudera/parcels/CDH/lib/hive
export HIVE_CLI_HOME=/usr/bin/hive
#export FLINK_HOME=/opt/soft/flink
#export DATAX_HOME=/opt/soft/datax
export TRINO_HOME=/apps/dolphinscheduler/task_plugin/trino/lvshou_task_plugin-1.0.jar
export DATASYNC_HOME=${TRINO_HOME}
export TOCK_HOME=/apps/dolphinscheduler/task_plugin/clickhouseexport/startMain.sh
export CLICKHOUSE_HOME=/apps/dolphinscheduler/task_plugin/clickhouse/lvshou_task_plugin-1.0.jar
export ST_HOME=/apps/seatunnel/current/bin/start-seatunnel-spark-2-connector-v2.sh

#export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
