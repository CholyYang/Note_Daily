2021.4.7

1. 环境：Python3.6
2. 元数据库：PG
   - create user airflow with password 'airflowscheduler';
   - create database airflow owner airflow; 
   - grant all on database airflow to airflow;
3. 配置Airflow环境变量
   - export SLUGIFY_USES_TEXT_UNIDECODE=yes
   - export AIRFLOW_HOME=/data/apps/lib/airflow
4. 将配置文件airflow.cfg放到AIRFLOW_HOME下
    * 配置文件见https://git.growingio.cn/growing/data/scheduler-airflow/-/tree/gdp
5. airflow.cfg中配置项修改：
    * PG配置(ip)：sql_alchemy_conn=postgresql://airflow:airflowscheduler@ip:5432/airflow
    * Redis配置(password/ip/port):        broker_url = redis://:password@ip:port/0        result_backend = redis://:password@ip:port/1
    * 端口(port)：     endpoint_url=http://localhost:port     base_url=http://localhost:port     web_server_port=port
6. 在AIRFLOW_HOME下创建目录dags,将offline_daily_dag.py和offline_hourly_dag.py(见附件)放在dags下,需要修改文件中offline项目rules库配置。如：engine = create_engine('postgresql://postgres:postgres@10.42.147.187:5432/rules')
    * dag见https://git.growingio.cn/growing/data/scheduler-airflow/-/tree/gdp下dags目录
7. 安装Airflow及其依赖python3 -m pip install apache-airflow[celery,postgres,redis]==1.10.12 --constraint "https://raw.githubusercontent.com/apache/airflow/constraints-1.10.12/constraints-3.6.txt"
8. 初始化数据库(或者重新初始化数据库)airflow db init (airflow resetdb)
9. 启动服务启动Webserver: airflow webserver -D
   启动Scheduler: airflow scheduler -D
10.  airflow users create --username admin --firstname data --lastname gio --role Admin --email 2021@gio.com --password admin
11. 访问Webserver UI
    - IP:web_server_port



python3 -m pip install apache-airflow[celery,postgres,redis]==2.0.1 --constraint "https://raw.githubusercontent.com/apache/airflow/constraints-2.0.1/constraints-3.6.txt"