package cn.tsign.spark.broadcast;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.spark_project.guava.reflect.TypeToken;

import com.google.gson.Gson;

import cn.tsign.common.config.ConfigConstant;
import cn.tsign.common.util.ConfProperties;
import cn.tsign.spark.broadcast.entity.CoreConfig;
import cn.tsign.spark.broadcast.entity.CoreConfig.AggConfig;

public class ConfBroadcast {

    private static Broadcast<CoreConfig> instance = null;

    public static synchronized Broadcast<CoreConfig> getInstance(JavaSparkContext jsc, boolean refresh) {

        // 定时更新清空配置（更新）
        if (instance != null && refresh) {
            if (instance != null) {
                instance.unpersist();
                instance = null;
            }
        }

        if (instance == null) {
            System.out.println("从HDFS加载配置文件....");
            CoreConfig conf = new CoreConfig();

            try {
                System.out.println("加载trackTablename配置");
                JavaRDD<String> trackTablenameConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                                     + ConfProperties.getStringValue(ConfigConstant.track_tablename_conf_path));
                for (String item : trackTablenameConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putTrackTableNameSetting(item.substring(0, splitIndex), item.substring(splitIndex + 1));
                    }
                }
                System.out.println();
                System.out.println("加载binlogTablename配置");
                JavaRDD<String> binlogTablenameConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                                      + ConfProperties.getStringValue(ConfigConstant.binlog_tablename_conf_path));
                for (String item : binlogTablenameConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putBinlogTableNameSetting(item.substring(0, splitIndex), item.substring(splitIndex + 1));
                    }
                }
                System.out.println();
                System.out.println("加载rowkey配置");
                JavaRDD<String> rowkeyConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                             + ConfProperties.getStringValue(ConfigConstant.rowkey_conf_path));
                for (String item : rowkeyConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putRowkeySetting(item.substring(0, splitIndex), item.substring(splitIndex + 1));
                    }
                }
                System.out.println();
                System.out.println("加载聚合配置");
                JavaRDD<String> aggConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                          + ConfProperties.getStringValue(ConfigConstant.agg_conf_path));
                for (String item : aggConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putAggSetting(item.substring(0, splitIndex),
                                           new Gson().fromJson(item.substring(splitIndex + 1),
                                                               new TypeToken<List<AggConfig>>() {

                                                                   private static final long serialVersionUID = -2956278138002979076L;
                                                               }.getType()));

                    }
                }

                System.out.println();
                System.out.println("加载alarms配置");
                JavaRDD<String> alarmsConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                             + ConfProperties.getStringValue(ConfigConstant.alarms_conf_path));
                for (String item : alarmsConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putAlarmConfig(item.substring(0, splitIndex),
                                            Long.parseLong(item.substring(splitIndex + 1)));

                    }
                }

                System.out.println();
                System.out.println("加载alarmsNotification配置");
                JavaRDD<String> alarmsNotificationConfRdd = jsc.textFile(ConfProperties.getStringValue(ConfigConstant.hdfs_uri)
                                                                         + ConfProperties.getStringValue(ConfigConstant.alarms_notification_path));
                for (String item : alarmsNotificationConfRdd.collect()) {
                    if (item.indexOf("=") > 0) {
                        int splitIndex = item.indexOf("=");
                        conf.putAlarmNotificationConfig(item.substring(0, splitIndex), item.substring(splitIndex + 1));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("配置文件读取失败，可能文件不存在。请在监控UI上配置，文件会自动创建");
            }

            instance = jsc.broadcast(conf);
        }
        return instance;

    }
}
